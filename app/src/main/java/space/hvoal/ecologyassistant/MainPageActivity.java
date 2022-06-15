package space.hvoal.ecologyassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainPageActivity extends AppCompatActivity {

   private Button buttonprofile, buttondialog, buttonmap, buttonlogaut, buttonmydialog, buttoncreatedialog;
   private FirebaseAuth lauth;
   private FirebaseUser currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл
        setContentView(R.layout.activity_main_page);

        lauth = FirebaseAuth.getInstance();
        currentuser = lauth.getCurrentUser();


        buttonprofile = findViewById(R.id.buttonprofile);
        buttoncreatedialog = findViewById(R.id.buttoncreatedialog);
        buttondialog = findViewById(R.id.buttondialog);
        buttonmydialog = findViewById(R.id.buttonmydialog);
        buttonmap = findViewById(R.id.buttonmap);
        buttonlogaut = findViewById(R.id.buttonlogaut);


        buttonmap.setOnClickListener(view -> {
            Intent mapintent = new Intent(MainPageActivity.this, MapsActivity.class);
            startActivity(mapintent);
            finish();
        });

        buttonlogaut.setOnClickListener(view -> {
            lauth.signOut();
            logautUser();
        });

        buttonprofile.setOnClickListener(view -> {
            Intent prointent = new Intent(MainPageActivity.this, ProfileActivity.class);
            startActivity(prointent);
            finish();
        });

        buttondialog.setOnClickListener(view -> {
            Intent dialogintent = new Intent(MainPageActivity.this, DisscusionActivity.class);
            startActivity(dialogintent);
            finish();
        });

        buttoncreatedialog.setOnClickListener(view -> {
            Intent createprojectintent = new Intent(MainPageActivity.this, CreateProjectActivity.class);
            startActivity(createprojectintent);
            finish();
        });

        buttonmydialog.setOnClickListener(view -> {
            Intent mydialogintent = new Intent(MainPageActivity.this, MyProjectActivity.class);
            startActivity(mydialogintent);
            finish();
        });

    }


    private void logautUser() {
        Intent logintent = new Intent(MainPageActivity.this, LoginActivity.class);
        startActivity(logintent);
        finish();
    }


}