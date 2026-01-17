package space.hvoal.ecologyassistant.data.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import space.hvoal.ecologyassistant.model.User;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class UserRepository {

    public interface Callback {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

    private final MutableLiveData<UiState<User>> userState = new MutableLiveData<>(UiState.loading());

    private DatabaseReference currentRef;
    private ValueEventListener listener;

    public LiveData<UiState<User>> observeCurrentUser() {
        if (listener != null) return userState;

        if (auth.getCurrentUser() == null) {
            userState.postValue(UiState.error("Пользователь не авторизован"));
            return userState;
        }

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        currentRef = usersRef.child(uid);
        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                if (u == null) {
                    userState.postValue(UiState.error("Профиль не найден"));
                } else {
                    userState.postValue(UiState.success(u));
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                userState.postValue(UiState.error(error.getMessage()));
            }
        };

        userState.postValue(UiState.loading());
        currentRef.addValueEventListener(listener);

        return userState;
    }

    public void updateProfile(String name, String secondName, String phone, Callback cb) {
        if (auth.getCurrentUser() == null) {
            cb.onError("Пользователь не авторизован");
            return;
        }

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("secondname", secondName);
        map.put("phone", phone);

        usersRef.child(uid).updateChildren(map)
                .addOnSuccessListener(unused -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void signOut() {
        auth.signOut();
    }

    public void clear() {
        if (currentRef != null && listener != null) {
            currentRef.removeEventListener(listener);
        }
        currentRef = null;
        listener = null;
    }
}
