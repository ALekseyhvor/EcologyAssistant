package space.hvoal.ecologyassistant.data.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import space.hvoal.ecologyassistant.model.Comment;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class CommentsRepository {

    public interface Callback {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final DatabaseReference usersRef =
            FirebaseDatabase.getInstance().getReference().child("Users");
    private final DatabaseReference projectsRef =
            FirebaseDatabase.getInstance().getReference().child("Projects");

    private final MutableLiveData<String> usernameLiveData = new MutableLiveData<>();
    private String cachedUsername;

    private DatabaseReference commentsRef;
    private ValueEventListener commentsListener;

    public CommentsRepository() {
        loadUsernameOnce();
    }

    public LiveData<String> username() {
        return usernameLiveData;
    }

    public LiveData<UiState<List<Comment>>> observeComments(String projectId) {
        MutableLiveData<UiState<List<Comment>>> out = new MutableLiveData<>(UiState.loading());
        clearCommentsListener();

        commentsRef = projectsRef.child(projectId).child("comments");
        commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<Comment>> t =
                        new GenericTypeIndicator<List<Comment>>() {};
                List<Comment> list = snapshot.getValue(t);
                if (list == null) list = new ArrayList<>();
                out.postValue(UiState.success(list));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                out.postValue(UiState.error(error.getMessage()));
            }
        };

        out.postValue(UiState.loading());
        commentsRef.addValueEventListener(commentsListener);
        return out;
    }

    public void addComment(String projectId, String text, Callback cb) {
        String u = cachedUsername;

        if (u == null || u.trim().isEmpty()) {
            cb.onError("Имя пользователя ещё не загрузилось");
            return;
        }

        DatabaseReference ref = projectsRef.child(projectId).child("comments");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<Comment>> t =
                        new GenericTypeIndicator<List<Comment>>() {};
                List<Comment> list = snapshot.getValue(t);
                if (list == null) list = new ArrayList<>();

                list.add(new Comment(u, text));

                ref.setValue(list)
                        .addOnSuccessListener(unused -> cb.onSuccess())
                        .addOnFailureListener(e -> cb.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                cb.onError(error.getMessage());
            }
        });
    }

    public void clear() {
        clearCommentsListener();
    }

    private void clearCommentsListener() {
        if (commentsRef != null && commentsListener != null) {
            commentsRef.removeEventListener(commentsListener);
        }
        commentsRef = null;
        commentsListener = null;
    }

    private void loadUsernameOnce() {
        if (auth.getCurrentUser() == null) {
            cachedUsername = null;
            usernameLiveData.postValue(null);
            return;
        }

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object nameVal = snapshot.child("name").getValue();
                cachedUsername = nameVal != null ? nameVal.toString() : null;
                usernameLiveData.postValue(cachedUsername);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
