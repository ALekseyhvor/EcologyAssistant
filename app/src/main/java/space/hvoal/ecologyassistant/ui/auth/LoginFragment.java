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

import space.hvoal.ecologyassistant.R;

public class LoginFragment extends Fragment {

    private FirebaseAuth auth;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_login, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        TextView createAcc = view.findViewById(R.id.textViewCreateAccaunt);
        createAcc.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_login_to_registration)
        );

        view.findViewById(R.id.buttonautorization).setOnClickListener(v -> login());
    }

    private void login() {
        EditText email = root.findViewById(R.id.editTextEmailAddressaut);
        EditText pass = root.findViewById(R.id.editTextPasswordaut);

        if (TextUtils.isEmpty(email.getText().toString())) {
            Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (pass.getText().toString().length() < 7) {
            Snackbar.make(root, "Пароль должен быть длиннее 7 символов", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnSuccessListener(authResult ->
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_login_to_main)
                )
                .addOnFailureListener(e ->
                        Snackbar.make(root, "Ошибка авторизации. " + e.getMessage(),
                                      Snackbar.LENGTH_SHORT).show()
                );
    }
}
