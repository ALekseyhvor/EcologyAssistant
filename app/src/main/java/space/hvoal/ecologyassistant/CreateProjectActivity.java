package space.hvoal.ecologyassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import space.hvoal.ecologyassistant.db.Project;

public class CreateProjectActivity extends AppCompatActivity {

    private ImageView backbtn;
    private Button submit;
    private FirebaseDatabase db;
    private DatabaseReference projectRef;
    private RelativeLayout root;
    private NotificationManager nm;
    private final int NOTIFICATION_ID = 1;
    private final String CHANNEL_ID = "CHANNEL_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл
        setContentView(R.layout.activity_create_project);


        db = FirebaseDatabase.getInstance();
        projectRef = db.getReference("Projects");

        backbtn = findViewById(R.id.back_button);
        submit = findViewById(R.id.buttonCreateProject);
        root = findViewById(R.id.root_element_crproject);

        submit.setOnClickListener(view -> {
            writerProject();
            showNotification();
        });

        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(CreateProjectActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });

        nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

    }

    private void writerProject(){
        final EditText nameproject = findViewById(R.id.editNameProject);
        final EditText maintext = findViewById(R.id.editMainTheme);
        if (TextUtils.isEmpty(nameproject.getText().toString())){
            Snackbar.make(root, "Введите название вашего проекта", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(maintext.getText().toString())){
            Snackbar.make(root, "Ваше описание проекта пустое", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Project project = new Project();
        project.setNameproject(nameproject.getText().toString());
        project.setTextproject(maintext.getText().toString());

        projectRef.setValue(project)
                .addOnSuccessListener(unused -> Snackbar.make(root, "Ваш проект успешно опубликован!", Snackbar.LENGTH_SHORT).show());
        startActivity(new Intent(CreateProjectActivity.this, CreateProjectActivity.class));
        finish();
    }


    //Метод Push-уведомления
    private void showNotification(){
        Intent intent = new Intent(getApplicationContext(), MyProjectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.smart_ecology_eco_nature_world_icon_)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setTicker("Новое уведомление")
                        .setContentTitle("Новый проект")
                        .setContentText("Вы создали новый проект");
                        createChannelIfNeeded(nm);
                        nm.notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private void createChannelIfNeeded(NotificationManager manager) {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(notificationChannel);
    }
}