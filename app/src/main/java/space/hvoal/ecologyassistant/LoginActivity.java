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

public class LoginActivity extends AppCompatActivity {

    private TextView createacc;
    private Button buttonaut;
    private DatabaseReference users;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл
        setContentView(R.layout.activity_login);

        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        auth = FirebaseAuth.getInstance();

        root = findViewById(R.id.root_elementaut);

        createacc = findViewById(R.id.textViewCreateAccaunt);
        createacc.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        buttonaut =  findViewById(R.id.buttonautorization);
        buttonaut.setOnClickListener(view -> login());
    }

    private void login(){
        final EditText email = findViewById(R.id.editTextEmailAddressaut);
        final EditText pass = findViewById(R.id.editTextPasswordaut);

            if (TextUtils.isEmpty(email.getText().toString())){
                Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (pass.getText().toString().length() < 8){
                Snackbar.make(root, "Пароль должен быть длинне 8 символов", Snackbar.LENGTH_SHORT).show();
                return;
            }

        auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnSuccessListener(authResult -> {
                startActivity(new Intent(LoginActivity.this, MapActivity.class));
                finish();
                }).addOnFailureListener(e -> Snackbar.make(root, "Ошибка авторизации. " + e.getMessage(), Snackbar.LENGTH_SHORT).show());


    }

}