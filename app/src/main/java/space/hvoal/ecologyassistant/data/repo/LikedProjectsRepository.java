package space.hvoal.ecologyassistant.data.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class LikedProjectsRepository {

    private final DatabaseReference projectsRef =
            FirebaseDatabase.getInstance().getReference().child("Projects");

    private DatabaseReference currentRef;
    private ValueEventListener listener;

    public LiveData<UiState<List<Project>>> observeLikedByUsername(String username) {
        MutableLiveData<UiState<List<Project>>> out = new MutableLiveData<>(UiState.loading());

        clear();

        currentRef = projectsRef;
        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Project> result = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Project p = child.getValue(Project.class);
                    if (p == null) continue;

                    List<String> subs = p.getSubscribers();
                    if (subs != null && username != null && subs.contains(username)) {
                        result.add(p);
                    }
                }

                out.postValue(UiState.success(result));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                out.postValue(UiState.error(error.getMessage()));
            }
        };

        out.postValue(UiState.loading());
        currentRef.addValueEventListener(listener);

        return out;
    }

    public void clear() {
        if (currentRef != null && listener != null) {
            currentRef.removeEventListener(listener);
        }
        currentRef = null;
        listener = null;
    }
}
