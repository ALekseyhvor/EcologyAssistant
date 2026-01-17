package space.hvoal.ecologyassistant.data.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class MyProjectsRepository {

    private final DatabaseReference projectsRef = FirebaseDatabase.getInstance().getReference("Projects");

    private Query currentQuery;
    private ValueEventListener listener;

    public LiveData<UiState<List<Project>>> observeByAuthor(String authorName) {
        MutableLiveData<UiState<List<Project>>> out = new MutableLiveData<>(UiState.loading());

        clear();

        currentQuery = projectsRef.orderByChild("author").equalTo(authorName);

        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Project> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Project p = child.getValue(Project.class);
                    if (p != null) list.add(p);
                }
                out.postValue(UiState.success(list));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                out.postValue(UiState.error(error.getMessage()));
            }
        };

        out.postValue(UiState.loading());
        currentQuery.addValueEventListener(listener);

        return out;
    }

    public void clear() {
        if (currentQuery != null && listener != null) {
            currentQuery.removeEventListener(listener);
        }
        currentQuery = null;
        listener = null;
    }
}
