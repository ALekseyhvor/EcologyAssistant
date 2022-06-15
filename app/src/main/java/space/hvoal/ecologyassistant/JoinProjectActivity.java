package space.hvoal.ecologyassistant;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.utils.ProjectWriter;

public class JoinProjectActivity  extends AppCompatActivity {

    private ImageView backbtn;
    private Button submit;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private DatabaseReference projectRef;
    private RelativeLayout root;
    private NotificationManager nm;
    private final int NOTIFICATION_ID = 1;
    private final String CHANNEL_ID = "CHANNEL_ID";
    private String author, nameP, mainP, saveCurrentDate, saveCurrentTime, projectKey;
    private EditText name;
    private ProjectWriter projectWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл
        setContentView(R.layout.activity_create_project);


        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        projectRef = db.getReference().child("Projects");

        backbtn = findViewById(R.id.back_button);
        submit = findViewById(R.id.joinButton);
        root = findViewById(R.id.root_element_crproject);
        name = findViewById(R.id.joinName);

        projectWriter = new ProjectWriter();

        submit.setOnClickListener(view -> {
            projectWriter.saveProjectInformation(
                    new Project(

                    )
            );
            startActivity(new Intent(JoinProjectActivity.this, MyProjectActivity.class));
            finish();
        });

        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(JoinProjectActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });

        nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

    }
}
