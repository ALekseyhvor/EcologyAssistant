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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class DiscussionFragment extends Fragment {

    private RecyclerView recyclerView;
    private Switch switchDate;
    private Switch switchSubscribersCnt;

    private DatabaseReference refProject;
    private FirebaseAuth auth;

    private FirebaseRecyclerAdapter<Project, ProjectViewHolder> adapter;

    public DiscussionFragment() {
        super(R.layout.fragment_tab_discussion);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        refProject = FirebaseDatabase.getInstance().getReference().child("Projects");

        ImageView backBtn = view.findViewById(R.id.back_button);
        switchDate = view.findViewById(R.id.switchDate);
        switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        backBtn.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.fabCreateProject).setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.action_projects_to_create)
        );

        switchDate.setOnClickListener(v -> {
            if (switchDate.isChecked()) switchSubscribersCnt.setChecked(false);
            rebuildAdapter();
        });

        switchSubscribersCnt.setOnClickListener(v -> {
            if (switchSubscribersCnt.isChecked()) switchDate.setChecked(false);
            rebuildAdapter();
        });

        rebuildAdapter();
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

    private void rebuildAdapter() {
        Query query;
        if (switchDate != null && switchDate.isChecked()) {
            query = refProject.orderByChild("dateTime");
        } else if (switchSubscribersCnt != null && switchSubscribersCnt.isChecked()) {
            query = refProject.orderByChild("likesCount");
        } else {
            query = refProject.orderByChild("nameProject");
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
                } catch (ParseException ignored) { }

                holder.creationdateTextView.setText(viewDate);
                holder.textprojectTextView.setText(model.getDescription());

                int commentsCount = Optional.ofNullable(model.getComments()).orElse(new ArrayList<>()).size();
                holder.commTextView.setText(String.valueOf(commentsCount));

                int likesCount = 0;
                if (model.getLikesCount() != null) likesCount = model.getLikesCount();
                else if (model.getLikes() != null) likesCount = model.getLikes().size();
                holder.likesTextView.setText(String.valueOf(likesCount));

                String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

                boolean liked = uid != null
                        && model.getLikes() != null
                        && Boolean.TRUE.equals(model.getLikes().get(uid));

                holder.likeButton.setImageResource(
                        liked ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
                );

                holder.likeButton.setOnClickListener(v -> {
                    if (uid == null) return;
                    holder.likeButton.setEnabled(false);
                    toggleLike(model.getId(), uid, () -> holder.likeButton.setEnabled(true));
                });

                holder.chatButton.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("projectId", model.getId());
                    androidx.navigation.fragment.NavHostFragment
                            .findNavController(DiscussionFragment.this)
                            .navigate(R.id.action_projects_to_details, args);
                });
            }

            @NonNull
            @Override
            public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.project_item, parent, false);
                return new ProjectViewHolder(v, true);
            }
        };

        recyclerView.setAdapter(adapter);

        if (getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
            adapter.startListening();
        }
    }

    private interface Done { void run(); }

    private void toggleLike(String projectId, String uid, Done done) {
        refProject.child(projectId).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Object likesObj = currentData.child("likes").getValue();
                Map<String, Object> likes = (likesObj instanceof Map)
                        ? (Map<String, Object>) likesObj
                        : new HashMap<>();

                boolean alreadyLiked = likes.containsKey(uid);

                long cnt = 0;
                Object cntObj = currentData.child("likesCount").getValue();
                if (cntObj instanceof Long) cnt = (Long) cntObj;
                else if (cntObj instanceof Integer) cnt = ((Integer) cntObj).longValue();

                if (alreadyLiked) {
                    likes.remove(uid);
                    cnt = Math.max(0, cnt - 1);
                } else {
                    likes.put(uid, true);
                    cnt = cnt + 1;
                }

                currentData.child("likes").setValue(likes);
                currentData.child("likesCount").setValue(cnt);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                done.run();
            }
        });
    }
}
