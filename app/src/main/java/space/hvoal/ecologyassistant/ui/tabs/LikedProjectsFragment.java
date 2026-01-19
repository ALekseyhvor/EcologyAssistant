package space.hvoal.ecologyassistant.ui.tabs;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.profile.LikedProjectsAdapter;

public class LikedProjectsFragment extends Fragment {

    private RecyclerView recyclerView;
    private Switch switchDate;
    private Switch switchSubscribersCnt;
    private ImageView backbtn;

    private FirebaseAuth auth;
    private DatabaseReference projectsRef;
    private DatabaseReference likesRef;

    private String uid;

    private Query likedQuery;
    private ValueEventListener likedListener;

    private final Map<String, Project> loadedProjects = new HashMap<>();
    private final HashSet<String> currentLikedIds = new HashSet<>();

    private LikedProjectsAdapter adapter;

    public LikedProjectsFragment() {
        super(R.layout.fragment_liked_projects);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        projectsRef = FirebaseDatabase.getInstance().getReference().child("Projects");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        backbtn = view.findViewById(R.id.back_button);
        switchDate = view.findViewById(R.id.switchDate);
        switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        recyclerView = view.findViewById(R.id.recycleLikedView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        backbtn.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        switchDate.setVisibility(View.GONE);
        switchSubscribersCnt.setVisibility(View.GONE);

        adapter = new LikedProjectsAdapter(new LikedProjectsAdapter.Listener() {
            @Override
            public void onToggleLike(@NonNull Project project) {
                toggleLike(project.getId(), uid, () -> { });
            }

            @Override
            public void onOpenDetails(@NonNull Project project) {
                Bundle b = new Bundle();
                b.putString("projectId", project.getId());
                NavHostFragment.findNavController(LikedProjectsFragment.this)
                        .navigate(R.id.projectDetailsFragment, b);
            }
        });

        recyclerView.setAdapter(adapter);

        if (uid == null || uid.trim().isEmpty()) return;

        likedQuery = likesRef.orderByChild(uid).equalTo(true);
        attachLikedListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (likedQuery != null && likedListener != null) {
            likedQuery.removeEventListener(likedListener);
        }

        likedQuery = null;
        likedListener = null;

        loadedProjects.clear();
        currentLikedIds.clear();

        adapter = null;
        recyclerView = null;
    }

    private void attachLikedListener() {
        likedListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<String> newLikedIds = new HashSet<>();
                for (DataSnapshot projectNode : snapshot.getChildren()) {
                    String projectId = projectNode.getKey();
                    if (projectId != null) newLikedIds.add(projectId);
                }

                HashSet<String> removed = new HashSet<>(currentLikedIds);
                removed.removeAll(newLikedIds);
                for (String id : removed) {
                    currentLikedIds.remove(id);
                    loadedProjects.remove(id);
                }

                HashSet<String> added = new HashSet<>(newLikedIds);
                added.removeAll(currentLikedIds);
                currentLikedIds.addAll(added);

                if (added.isEmpty()) {
                    publishList();
                    return;
                }

                for (String projectId : added) {
                    projectsRef.child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot projectSnap) {
                            Project p = projectSnap.getValue(Project.class);
                            if (p != null) {
                                if (p.getId() == null || p.getId().trim().isEmpty()) {
                                    try { p.setId(projectSnap.getKey()); } catch (Exception ignored) { }
                                }
                                loadedProjects.put(projectId, p);
                            } else {
                                loadedProjects.remove(projectId);
                            }
                            publishList();
                        }

                        @Override public void onCancelled(@NonNull DatabaseError error) {
                            publishList();
                        }
                    });
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };

        likedQuery.addValueEventListener(likedListener);
    }

    private void publishList() {
        List<Project> list = new ArrayList<>();
        for (String id : currentLikedIds) {
            Project p = loadedProjects.get(id);
            if (p != null) list.add(p);
        }
        if (adapter != null) adapter.setItems(list);
    }

    private interface Done { void run(); }

    private void toggleLike(String projectId, String uid, Done done) {
        if (projectId == null || projectId.trim().isEmpty() || uid == null || uid.trim().isEmpty()) {
            done.run();
            return;
        }

        DatabaseReference likeNode = likesRef.child(projectId).child(uid);

        likeNode.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Object val = currentData.getValue();
                boolean alreadyLiked = (val instanceof Boolean && (Boolean) val);

                if (alreadyLiked) currentData.setValue(null);
                else currentData.setValue(true);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                boolean nowLiked = snapshot != null && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));

                projectsRef.child(projectId).child("likesCount").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        long cnt = 0;
                        Object cntObj = currentData.getValue();
                        if (cntObj instanceof Long) cnt = (Long) cntObj;
                        else if (cntObj instanceof Integer) cnt = ((Integer) cntObj).longValue();

                        if (nowLiked) cnt = cnt + 1;
                        else cnt = Math.max(0, cnt - 1);

                        currentData.setValue(cnt);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error2, boolean committed2, @Nullable DataSnapshot snapshot2) {
                        done.run();
                    }
                });
            }
        });
    }
}
