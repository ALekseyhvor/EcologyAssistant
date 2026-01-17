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
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class MyProjectsFragment extends Fragment {

    private RecyclerView recyclerMyView;
    private Switch switchDate;
    private Switch switchSubscribersCnt;
    private ImageView backbtn;

    private FirebaseAuth auth;
    private DatabaseReference projectsRef;
    private DatabaseReference usersRef;

    private FirebaseRecyclerAdapter<Project, ProjectViewHolder> adapter;

    public MyProjectsFragment() {
        super(R.layout.fragment_my_projects);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        projectsRef = FirebaseDatabase.getInstance().getReference().child("Projects");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        backbtn = view.findViewById(R.id.back_button);
        switchDate = view.findViewById(R.id.switchDate);
        switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        recyclerMyView = view.findViewById(R.id.recycleMyView);
        recyclerMyView.setLayoutManager(new LinearLayoutManager(requireContext()));

        backbtn.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        switchDate.setVisibility(View.GONE);
        switchSubscribersCnt.setVisibility(View.GONE);

        migrateOldProjectsThenLoad();
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

    private void migrateOldProjectsThenLoad() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null || uid.trim().isEmpty()) return;

        usersRef.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object nameVal = snapshot.getValue();
                String username = nameVal != null ? nameVal.toString() : null;

                if (username == null || username.trim().isEmpty()) {
                    rebuildAdapter(uid);
                    return;
                }

                projectsRef.orderByChild("author").equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                                for (DataSnapshot child : snap.getChildren()) {
                                    Object authorIdVal = child.child("authorId").getValue();
                                    if (authorIdVal == null) {
                                        child.getRef().child("authorId").setValue(uid);
                                    }
                                }
                                rebuildAdapter(uid);
                            }

                            @Override public void onCancelled(@NonNull DatabaseError error) {
                                rebuildAdapter(uid);
                            }
                        });
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                String uid2 = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
                if (uid2 != null) rebuildAdapter(uid2);
            }
        });
    }

    private void rebuildAdapter(String uid) {
        Query query = projectsRef.orderByChild("authorId").equalTo(uid);

        FirebaseRecyclerOptions<Project> options = new FirebaseRecyclerOptions.Builder<Project>()
                .setSnapshotArray(new FirebaseArray<>(query, new ClassSnapshotParser<>(Project.class)))
                .build();

        if (adapter != null) adapter.stopListening();

        adapter = new FirebaseRecyclerAdapter<Project, ProjectViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull ProjectViewHolder holder, int position, @NonNull Project model) {
                holder.nameUserTextView.setText(model.getAuthor());
                holder.nameprojectTextView.setText(model.getNameProject());
                holder.textprojectTextView.setText(model.getDescription());

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat vf = new SimpleDateFormat("MMM-dd HH:mm");

                String viewDate = "";
                try {
                    Date d = df.parse(model.getDateTime());
                    if (d != null) viewDate = vf.format(d);
                } catch (ParseException ignored) { }
                holder.creationdateTextView.setText(viewDate);

                int commentsCount = Optional.ofNullable(model.getComments()).orElse(new ArrayList<>()).size();
                holder.commTextView.setText(String.valueOf(commentsCount));

                int likesCount = 0;
                if (model.getLikesCount() != null) likesCount = model.getLikesCount();
                else if (model.getLikes() != null) likesCount = model.getLikes().size();
                holder.likesTextView.setText(String.valueOf(likesCount));

                holder.likeButton.setVisibility(View.GONE);

                holder.chatButton.setOnClickListener(v -> {
                    Bundle b = new Bundle();
                    b.putString("projectId", model.getId());
                    NavHostFragment.findNavController(MyProjectsFragment.this)
                            .navigate(R.id.projectDetailsFragment, b);
                });
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
