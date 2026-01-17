package space.hvoal.ecologyassistant.data.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class LikedProjectsRepository {

    private final DatabaseReference projectsRef =
            FirebaseDatabase.getInstance().getReference().child("Projects");

    private final DatabaseReference likesRef =
            FirebaseDatabase.getInstance().getReference().child("Likes");

    private Query likesQuery;
    private ValueEventListener likesListener;

    private final Map<String, ValueEventListener> projectListeners = new HashMap<>();
    private final Map<String, Project> cache = new HashMap<>();
    private final List<String> order = new ArrayList<>();

    public LiveData<UiState<List<Project>>> observeLiked() {
        MutableLiveData<UiState<List<Project>>> out = new MutableLiveData<>(UiState.loading());
        clear();

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null || uid.trim().isEmpty()) {
            out.postValue(UiState.error("Пользователь не авторизован"));
            return out;
        }

        likesQuery = likesRef.orderByChild(uid).equalTo(true);

        likesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                detachAllProjectListeners();

                cache.clear();
                order.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String projectId = child.getKey();
                    if (projectId == null) continue;

                    order.add(projectId);

                    ValueEventListener pl = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot projectSnap) {
                            Project p = projectSnap.getValue(Project.class);
                            if (p == null) {
                                cache.remove(projectId);
                            } else {
                                if (p.getId() == null || p.getId().trim().isEmpty()) {
                                    p.setId(projectId);
                                }
                                cache.put(projectId, p);
                            }
                            publish(out);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            out.postValue(UiState.error(error.getMessage()));
                        }
                    };

                    projectListeners.put(projectId, pl);
                    projectsRef.child(projectId).addValueEventListener(pl);
                }

                if (order.isEmpty()) {
                    out.postValue(UiState.success(new ArrayList<>()));
                } else {
                    out.postValue(UiState.loading());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                out.postValue(UiState.error(error.getMessage()));
            }
        };

        out.postValue(UiState.loading());
        likesQuery.addValueEventListener(likesListener);

        return out;
    }

    private void publish(MutableLiveData<UiState<List<Project>>> out) {
        List<Project> list = new ArrayList<>();
        for (String id : order) {
            Project p = cache.get(id);
            if (p != null) list.add(p);
        }
        out.postValue(UiState.success(list));
    }

    private void detachAllProjectListeners() {
        for (Map.Entry<String, ValueEventListener> e : projectListeners.entrySet()) {
            String projectId = e.getKey();
            ValueEventListener l = e.getValue();
            if (projectId != null && l != null) {
                projectsRef.child(projectId).removeEventListener(l);
            }
        }
        projectListeners.clear();
    }

    public void clear() {
        if (likesQuery != null && likesListener != null) {
            likesQuery.removeEventListener(likesListener);
        }
        likesQuery = null;
        likesListener = null;

        detachAllProjectListeners();
        cache.clear();
        order.clear();
    }
}
