package space.hvoal.ecologyassistant.data.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProjectDetailsRepository {

    private final DatabaseReference projectsRef =
            FirebaseDatabase.getInstance().getReference().child("Projects");

    private ValueEventListener listener;
    private DatabaseReference currentRef;

    public LiveData<UiState<Project>> observeProjectById(String projectId) {
        MutableLiveData<UiState<Project>> out = new MutableLiveData<>(UiState.loading());

        clear();

        currentRef = projectsRef.child(projectId);
        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Project p = snapshot.getValue(Project.class);
                if (p == null) {
                    out.postValue(UiState.error("Проект не найден"));
                } else {
                    if (p.getId() == null || p.getId().trim().isEmpty()) {
                        p.setId(snapshot.getKey());
                    }
                    out.postValue(UiState.success(p));
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                out.postValue(UiState.error(error.getMessage()));
            }
        };

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
