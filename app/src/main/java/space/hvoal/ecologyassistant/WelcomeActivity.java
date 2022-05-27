package space.hvoal.ecologyassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл


        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                {
                    try {
                        sleep(5000);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        Intent logintent = new Intent(WelcomeActivity.this, LoginActivity.class);
                        startActivity(logintent);
                    }
                }

            }
        };
        thread.start();


    }

        @Override
        protected void onPause() {
            super.onPause();
            finish();
        }

}