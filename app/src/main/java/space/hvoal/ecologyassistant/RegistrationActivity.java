package space.hvoal.ecologyassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextName, editTextSecondName, editTextEmailAddress, editTextNumberSigned, editTextPassword;
    private Button buttonreg;
    private TextView textViewLoginAccaunt;
    private DatabaseReference mDataBase;
    private String USER_KEY = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл
        setContentView(R.layout.activity_registration);
        init();
    }

    private void init() {
        editTextName = findViewById(R.id.editTextName);
        editTextSecondName = findViewById(R.id.editTextSecondName);
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextNumberSigned = findViewById(R.id.editTextNumberSigned);
        editTextPassword = findViewById(R.id.editTextPassword);
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY);
    }

    public void onClickSave(View view){
    String id = mDataBase.getKey();
    String name = editTextName.getText().toString();
    String secondname = editTextSecondName.getText().toString();
    String email = editTextEmailAddress.getText().toString();
    String number = editTextNumberSigned.getText().toString();
    String password = editTextPassword.getText().toString();

    }


}