package space.hvoal.ecologyassistant.ui.tabs;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.List;

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

    private ProjectsViewModel vm;
    private ProjectsAdapter adapter;

    public DiscussionFragment() {
        super(R.layout.fragment_tab_discussion);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        ImageView backBtn = view.findViewById(R.id.back_button);
        switchDate = view.findViewById(R.id.switchDate);
        switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        backBtn.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        vm = new ViewModelProvider(this).get(ProjectsViewModel.class);

        adapter = new ProjectsAdapter(new ProjectsAdapter.Listener() {
            @Override
            public void onOpenDetails(@NonNull Project project) {
                Bundle args = new Bundle();
                args.putString("projectId", project.getId());
                androidx.navigation.fragment.NavHostFragment
                        .findNavController(DiscussionFragment.this)
                        .navigate(R.id.action_projects_to_details, args);
            }

            @Override
            public void onToggleLike(@NonNull Project project) {
                if (project.getId() == null) return;
                vm.toggleLike(project.getId());
            }
        });

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        adapter.setCurrentUid(uid);
        adapter.setShowLikeButton(true);

        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.fabCreateProject).setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.action_projects_to_create)
        );

        switchDate.setOnClickListener(v -> {
            if (switchDate.isChecked()) switchSubscribersCnt.setChecked(false);
            updateSortMode();
        });

        switchSubscribersCnt.setOnClickListener(v -> {
            if (switchSubscribersCnt.isChecked()) switchDate.setChecked(false);
            updateSortMode();
        });

        updateSortMode();

        vm.state().observe(getViewLifecycleOwner(), s -> {
            if (s == null) return;

            if (s.status == UiState.Status.LOADING) {
                return;
            }

            if (s.status == UiState.Status.ERROR) {
                Toast.makeText(requireContext(),
                        s.error != null ? s.error : "Ошибка загрузки проектов",
                        Toast.LENGTH_SHORT
                ).show();
                adapter.submitList(Collections.emptyList());
                return;
            }

            List<Project> list = s.data != null ? s.data : Collections.emptyList();
            adapter.submitList(list);
        });
    }

    private void updateSortMode() {
        if (switchDate != null && switchDate.isChecked()) {
            vm.setSortMode(ProjectsViewModel.SortMode.DATE_DESC);
        } else if (switchSubscribersCnt != null && switchSubscribersCnt.isChecked()) {
            vm.setSortMode(ProjectsViewModel.SortMode.LIKES_DESC);
        } else {
            vm.setSortMode(ProjectsViewModel.SortMode.DEFAULT);
        }
    }
}
