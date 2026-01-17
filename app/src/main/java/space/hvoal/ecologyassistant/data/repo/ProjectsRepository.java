package space.hvoal.ecologyassistant.data.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProjectsRepository {

    private final DatabaseReference projectsRef =
            FirebaseDatabase.getInstance().getReference().child("Projects");

    private final DatabaseReference likesRef =
            FirebaseDatabase.getInstance().getReference().child("Likes");

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final MutableLiveData<UiState<List<Project>>> state =
            new MutableLiveData<>(UiState.loading());

    private ValueEventListener listener;

    public LiveData<UiState<List<Project>>> observeAllProjects() {
        if (listener != null) return state;

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Project> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Project p = child.getValue(Project.class);
                    if (p != null) list.add(p);
                }
                state.postValue(UiState.success(list));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                state.postValue(UiState.error(error.getMessage()));
            }
        };

        state.postValue(UiState.loading());
        projectsRef.addValueEventListener(listener);
        return state;
    }

    public void toggleLike(String projectId) {
        if (projectId == null) return;

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        DatabaseReference userLikeRef = likesRef.child(projectId).child(uid);

        userLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLikedNow = snapshot.exists();

                if (isLikedNow) {
                    userLikeRef.removeValue();
                } else {
                    userLikeRef.setValue(true);
                }

                DatabaseReference likesCountRef = projectsRef.child(projectId).child("likesCount");
                likesCountRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Object raw = currentData.getValue();
                        int current;

                        if (raw instanceof Long) {
                            current = ((Long) raw).intValue();
                        } else if (raw instanceof Integer) {
                            current = (Integer) raw;
                        } else {
                            current = 0;
                        }

                        int updated = isLikedNow ? Math.max(0, current - 1) : current + 1;
                        currentData.setValue(updated);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void clear() {
        if (listener != null) {
            projectsRef.removeEventListener(listener);
            listener = null;
        }
    }
}
