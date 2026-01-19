package space.hvoal.ecologyassistant.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import space.hvoal.ecologyassistant.R;

public class EditProfileFragment extends Fragment {

    private EditText nameChange, secondNameChange, numberChange;
    private Button buttonSavedChange;
    private ImageView backBtn;
    private View root;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        root = view.findViewById(R.id.root_element_profile);

        nameChange = view.findViewById(R.id.editTextNameChange);
        secondNameChange = view.findViewById(R.id.editTextSecondNameChange);
        numberChange = view.findViewById(R.id.editTextNumberSignedChange);

        buttonSavedChange = view.findViewById(R.id.button_savedChange);
        backBtn = view.findViewById(R.id.back_button);

        EditText etEmail = view.findViewById(R.id.editTextEmailReadOnly);
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        etEmail.setText(fu != null ? fu.getEmail() : "");

        backBtn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        buttonSavedChange.setOnClickListener(v -> changeInfoController());

        getUserInformation();
    }

    private void changeInfoController() {
        if (TextUtils.isEmpty(nameChange.getText().toString())) {
            Snackbar.make(root, "Заполните поле с именем", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(secondNameChange.getText().toString())) {
            Snackbar.make(root, "Заполните поле с фамилией", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(numberChange.getText().toString())) {
            Snackbar.make(root, "Заполните поле с номером", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("id", uid);
        userMap.put("name", nameChange.getText().toString());
        userMap.put("secondname", secondNameChange.getText().toString());
        userMap.put("phone", numberChange.getText().toString());

        usersRef.child(uid).updateChildren(userMap);

        Snackbar.make(root, "Данные успешно изменены", Snackbar.LENGTH_SHORT).show();
    }

    private void getUserInformation() {
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Object n = snapshot.child("name").getValue();
                    Object sn = snapshot.child("secondname").getValue();
                    Object ph = snapshot.child("phone").getValue();

                    if (n != null) nameChange.setText(n.toString());
                    if (sn != null) secondNameChange.setText(sn.toString());
                    if (ph != null) numberChange.setText(ph.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
