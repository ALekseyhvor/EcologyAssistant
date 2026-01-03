package space.hvoal.ecologyassistant.ui.tabs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseArray;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class MyProjectsFragment extends Fragment {

    private RecyclerView recyclerMyView;
    private Switch switchDate;
    private Switch switchSubscribersCnt;
    private ImageView backbtn;

    private FirebaseDatabase db;
    private DatabaseReference refProject;
    private FirebaseAuth auth;
    private DatabaseReference usersref;

    private String username;

    private FirebaseRecyclerAdapter<Project, ProjectViewHolder> adapter;
    private ValueEventListener userListener;

    public MyProjectsFragment() {
        super(R.layout.fragment_my_projects);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        refProject = db.getReference().child("Projects");

        backbtn = view.findViewById(R.id.back_button);
        switchDate = view.findViewById(R.id.switchDate);
        switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        recyclerMyView = view.findViewById(R.id.recycleMyView);
        recyclerMyView.setLayoutManager(new LinearLayoutManager(requireContext()));

        backbtn.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        switchDate.setOnClickListener(v -> {
            if (switchDate.isChecked()) switchSubscribersCnt.setChecked(false);
            rebuildAdapter();
        });

        switchSubscribersCnt.setOnClickListener(v -> {
            if (switchSubscribersCnt.isChecked()) switchDate.setChecked(false);
            rebuildAdapter();
        });

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Object nameVal = snapshot.child("name").getValue();
                    username = nameVal != null ? nameVal.toString() : null;
                    rebuildAdapter();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        usersref.child(uid).addValueEventListener(userListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null && auth.getCurrentUser() != null) {
            usersref.child(auth.getCurrentUser().getUid()).removeEventListener(userListener);
        }
        userListener = null;
        adapter = null;
    }

    private void rebuildAdapter() {
        if (username == null || username.trim().isEmpty()) return;

        Query query = refProject.orderByChild("author").equalTo(username);

        if (switchDate.isChecked()) {
            query = refProject.orderByChild("dateTime");
        } else if (switchSubscribersCnt.isChecked()) {
            query = refProject.orderByChild("subscribers");
        }

        FirebaseRecyclerOptions<Project> options = new FirebaseRecyclerOptions.Builder<Project>()
                .setSnapshotArray(new FirebaseArray<>(query, new ClassSnapshotParser<>(Project.class)))
                .build();

        if (adapter != null) adapter.stopListening();

        adapter = new FirebaseRecyclerAdapter<Project, ProjectViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProjectViewHolder holder, int position, @NonNull Project model) {
                holder.nameUserTextView.setText(model.getAuthor());
                holder.nameprojectTextView.setText(model.getNameProject());

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat viewFormat = new SimpleDateFormat("MMM-dd HH:mm");

                String viewDate = "";
                try {
                    Date date = dateFormat.parse(model.getDateTime());
                    if (date != null) viewDate = viewFormat.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                holder.creationdateTextView.setText(viewDate);
                holder.textprojectTextView.setText(model.getDescription());

            }

            @NonNull
            @Override
            public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
                return new ProjectViewHolder(v, true);
            }
        };

        recyclerMyView.setAdapter(adapter);

        if (getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
            adapter.startListening();
        }
    }
}
