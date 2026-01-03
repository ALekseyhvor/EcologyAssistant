package space.hvoal.ecologyassistant.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.User;

public class RegistrationFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference users;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_registration, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        users = FirebaseDatabase.getInstance().getReference("Users");

        view.findViewById(R.id.buttonreg).setOnClickListener(v -> register());

        TextView enter = view.findViewById(R.id.textViewLoginAccaunt);
        enter.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void register() {
        EditText name = root.findViewById(R.id.editTextName);
        EditText secondname = root.findViewById(R.id.editTextSecondName);
        EditText email = root.findViewById(R.id.editTextEmailAddress);
        EditText phone = root.findViewById(R.id.editTextNumberSigned);
        EditText pass = root.findViewById(R.id.editTextPassword);

        if (TextUtils.isEmpty(name.getText().toString())) {
            Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(secondname.getText().toString())) {
            Snackbar.make(root, "Введите вашу фамилию", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email.getText().toString())) {
            Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone.getText().toString())) {
            Snackbar.make(root, "Введите ваш номер телефона", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (pass.getText().toString().length() < 7) {
            Snackbar.make(root, "Пароль должен быть длиннее 7 символов", Snackbar.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnSuccessListener(authResult -> {
                    User user = new User();
                    user.setName(name.getText().toString());
                    user.setSecondname(secondname.getText().toString());
                    user.setEmail(email.getText().toString());
                    user.setPhone(phone.getText().toString());

                    String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    users.child(uid).setValue(user)
                            .addOnSuccessListener(unused -> {
                                Snackbar.make(root, "Регистрация прошла успешно!", Snackbar.LENGTH_SHORT).show();
                                requireActivity().onBackPressed();
                            })
                            .addOnFailureListener(e ->
                                    Snackbar.make(root, "Ошибка записи профиля. " + e.getMessage(), Snackbar.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Snackbar.make(root, "Ошибка регистрации. " + e.getMessage(), Snackbar.LENGTH_SHORT).show()
                );
    }
}
