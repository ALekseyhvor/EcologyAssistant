package space.hvoal.ecologyassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class DisscusionActivity extends AppCompatActivity {

    private ImageView backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл
        setContentView(R.layout.activity_disscusion);


        backbtn = findViewById(R.id.back_button);

        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(DisscusionActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });

    }
}