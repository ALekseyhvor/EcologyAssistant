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

public class ProjectsRepository {

    private final DatabaseReference projectsRef =
            FirebaseDatabase.getInstance().getReference().child("Projects");

    private final MutableLiveData<UiState<List<Project>>> state =
            new MutableLiveData<>(UiState.loading());

    private ValueEventListener listener;

    public LiveData<UiState<List<Project>>> observeAllProjects() {
        if (listener != null) return state;

        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Project> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Project p = child.getValue(Project.class);
                    if (p != null) list.add(p);
                }
                state.postValue(UiState.success(list));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                state.postValue(UiState.error(error.getMessage()));
            }
        };

        state.postValue(UiState.loading());
        projectsRef.addValueEventListener(listener);

        return state;
    }

    public void clear() {
        if (listener != null) {
            projectsRef.removeEventListener(listener);
            listener = null;
        }
    }

    public void subscribeToProject(String projectId, String username) {
        DatabaseReference subsRef = projectsRef.child(projectId).child("subscribers");
        subsRef.get().addOnSuccessListener(snapshot -> {
            List<String> subs = (List<String>) snapshot.getValue();
            if (subs == null) subs = new ArrayList<>();
            if (!subs.contains(username)) subs.add(username);
            subsRef.setValue(subs);
        });
    }
}
