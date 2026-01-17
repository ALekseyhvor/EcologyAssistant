package space.hvoal.ecologyassistant.ui.tabs;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;
import space.hvoal.ecologyassistant.ui.tabs.projects.ProjectsAdapter;
import space.hvoal.ecologyassistant.ui.tabs.projects.ProjectsViewModel;

public class DiscussionFragment extends Fragment {

    private RecyclerView recyclerView;
    private Switch switchDate;
    private Switch switchSubscribersCnt;

    private FirebaseAuth auth;
    private DatabaseReference usersref;
    private ValueEventListener userListener;

    private String username;

    private ProjectsViewModel vm;
    private ProjectsAdapter adapter;

    public DiscussionFragment() {
        super(R.layout.fragment_tab_discussion);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");

        ImageView backBtn = view.findViewById(R.id.back_button);
        switchDate = view.findViewById(R.id.switchDate);
        switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        backBtn.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ProjectsAdapter(new ProjectsAdapter.Listener() {
            @Override
            public void onOpenDetails(Project project) {
                Bundle args = new Bundle();
                args.putString("projectId", project.getId());
                androidx.navigation.fragment.NavHostFragment
                        .findNavController(DiscussionFragment.this)
                        .navigate(R.id.action_projects_to_details, args);
            }

            @Override
            public void onSubscribe(Project project) {
                if (username == null || username.trim().isEmpty()) return;
                vm.subscribe(project.getId(), username);
            }
        });

        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.fabCreateProject).setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.action_projects_to_create)
        );

        vm = new ViewModelProvider(this).get(ProjectsViewModel.class);

        switchDate.setOnClickListener(v -> {
            if (switchDate.isChecked()) switchSubscribersCnt.setChecked(false);
            vm.setSortMode(switchDate.isChecked()
                    ? ProjectsViewModel.SortMode.DATE_DESC
                    : ProjectsViewModel.SortMode.NAME_ASC
            );
        });

        switchSubscribersCnt.setOnClickListener(v -> {
            if (switchSubscribersCnt.isChecked()) switchDate.setChecked(false);
            vm.setSortMode(switchSubscribersCnt.isChecked()
                    ? ProjectsViewModel.SortMode.SUBSCRIBERS_DESC
                    : ProjectsViewModel.SortMode.NAME_ASC
            );
        });

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Object nameVal = snapshot.child("name").getValue();
                    username = nameVal != null ? nameVal.toString() : null;
                    adapter.setUsername(username);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        usersref.child(uid).addValueEventListener(userListener);

        vm.state().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.status == UiState.Status.ERROR) {
                Snackbar.make(view, "Ошибка загрузки проектов: " + state.error, Snackbar.LENGTH_LONG).show();
                return;
            }
            if (state.status == UiState.Status.SUCCESS) {
                adapter.submitList(state.data);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null && auth.getCurrentUser() != null) {
            usersref.child(auth.getCurrentUser().getUid()).removeEventListener(userListener);
        }
        userListener = null;
    }
}
