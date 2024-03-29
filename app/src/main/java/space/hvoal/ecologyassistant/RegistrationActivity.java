package space.hvoal.ecologyassistant;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import space.hvoal.ecologyassistant.model.User;

public class RegistrationActivity extends AppCompatActivity {

    private Button buttonreg;
    private DatabaseReference users;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private RelativeLayout root;
    private TextView loginacc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл
        setContentView(R.layout.activity_registration);


        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        auth = FirebaseAuth.getInstance();

        root = findViewById(R.id.root_element);

        loginacc = findViewById(R.id.textViewLoginAccaunt);

        loginacc.setOnClickListener(view -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        buttonreg = findViewById(R.id.buttonreg);

        buttonreg.setOnClickListener(view -> register());

    }




    private void register(){

        final EditText name = findViewById(R.id.editTextName);
        final EditText secondname = findViewById(R.id.editTextSecondName);
        final EditText email = findViewById(R.id.editTextEmailAddress);
        final EditText phone = findViewById(R.id.editTextNumberSigned);
        final EditText pass = findViewById(R.id.editTextPassword);

        if (TextUtils.isEmpty(name.getText().toString())){
            Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(secondname.getText().toString())){
            Snackbar.make(root, "Введите вашу фамилию", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email.getText().toString())){
            Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone.getText().toString())){
            Snackbar.make(root, "Введите ваш номер телефона", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (pass.getText().toString().length() < 7){
            Snackbar.make(root, "Пароль должен быть длинне 7 символов", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Регистрация пользователя
        auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnSuccessListener(authResult -> {
                    User user = new User();
                    user.setName(name.getText().toString());
                    user.setSecondname(secondname.getText().toString());
                    user.setEmail(email.getText().toString());
                    user.setPhone(phone.getText().toString());
                    user.setPassword(pass.getText().toString());

                    users.child((Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid())
                            .setValue(user)
                            .addOnSuccessListener(unused -> Snackbar.make(root, "Регистрация прошла успешна!", Snackbar.LENGTH_SHORT).show());
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                    finish();
                }).addOnFailureListener(e -> Snackbar.make(root, "Ошибка регистрации. " + e.getMessage(), Snackbar.LENGTH_SHORT).show());

    }

}