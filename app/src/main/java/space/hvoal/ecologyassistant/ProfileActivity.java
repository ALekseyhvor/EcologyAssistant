package space.hvoal.ecologyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameChange, secondNameChange, numberChange;
    private Button button_savedChange;
    private ImageView backbtn;
    private FirebaseAuth auth;
    private DatabaseReference usersref;
    private RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");

        nameChange = findViewById(R.id.editTextNameChange);
        secondNameChange = findViewById(R.id.editTextSecondNameChange);
        numberChange = findViewById(R.id.editTextNumberSignedChange);
        button_savedChange = findViewById(R.id.button_savedChange);
        backbtn = findViewById(R.id.back_button);
        root = findViewById(R.id.root_element_profile);


        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(ProfileActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });

        button_savedChange.setOnClickListener(view -> changeInfoController());

        getUserInformation();

    }

    private void changeInfoController() {

        if (TextUtils.isEmpty(nameChange.getText().toString())) {
            Snackbar.make(root, "Заполните поле с  именем", Snackbar.LENGTH_SHORT).show();
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

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("id", Objects.requireNonNull(auth.getCurrentUser()).getUid());
        userMap.put("name", nameChange.getText().toString());
        userMap.put("secondname", secondNameChange.getText().toString());
        userMap.put("phone", numberChange.getText().toString());

        usersref.child(auth.getCurrentUser().getUid()).updateChildren(userMap);

        Snackbar.make(root, "Данные успешно изменены", Snackbar.LENGTH_SHORT).show();
    }

    private void getUserInformation() {
        usersref.child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {

                                    String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                    String secondname = Objects.requireNonNull(snapshot.child("secondname").getValue()).toString();
                                    String phone = Objects.requireNonNull(snapshot.child("phone").getValue()).toString();

                                    nameChange.setText(name);
                                    secondNameChange.setText(secondname);
                                    numberChange.setText(phone);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );
    }
}