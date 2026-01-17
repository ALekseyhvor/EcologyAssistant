package space.hvoal.ecologyassistant.data.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProjectLikesRepository {

    private final DatabaseReference likesRootRef =
            FirebaseDatabase.getInstance().getReference().child("Likes");

    private DatabaseReference currentRef;
    private ValueEventListener listener;

    public LiveData<Boolean> observeIsLiked(String projectId, String uid) {
        MutableLiveData<Boolean> out = new MutableLiveData<>(false);

        clear();

        if (projectId == null || uid == null) {
            out.postValue(false);
            return out;
        }

        currentRef = likesRootRef.child(projectId).child(uid);
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                out.postValue(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                out.postValue(false);
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
