package space.hvoal.ecologyassistant;


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import androidx.core.app.NotificationCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.utils.ProjectWriter;


public class CreateProjectActivity extends AppCompatActivity {

    private ImageView backbtn;
    private Button submit;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private DatabaseReference projectRef;
    private DatabaseReference userRef;
    private RelativeLayout root;
    private NotificationManager nm;
    private final int NOTIFICATION_ID = 1;
    private final String CHANNEL_ID = "CHANNEL_ID";
    private String author;
    private String nameP;
    private String mainP;
    private String saveCurrentDate;
    private String projectKey;
    private EditText nameproject, maintext, authortext;
    private ProjectWriter projectWriter;
    private FirebaseUser currentuser;


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
        userRef = db.getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        currentuser = auth.getCurrentUser();

        backbtn = findViewById(R.id.back_button);
        submit = findViewById(R.id.buttonCreateProject);
        root = findViewById(R.id.root_element_crproject);
        authortext = findViewById(R.id.editTextAuthor);
        nameproject = findViewById(R.id.editNameProject);
        maintext = findViewById(R.id.editMainTheme);

        currentuser.getEmail();

        userRef.child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {

                                    String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                    authortext.setText(name);
                                    author = name;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );
        authortext.setEnabled(false);

        submit.setOnClickListener(view -> {
            writerProject();
            showNotification();
            startActivity(new Intent(CreateProjectActivity.this, CreateProjectActivity.class));
            finish();
        });

        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(CreateProjectActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });

        nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        projectWriter = new ProjectWriter();
    }

    private void writerProject() {

        author = authortext.getText().toString();
        nameP = nameproject.getText().toString();
        mainP = maintext.getText().toString();

        if (TextUtils.isEmpty(author)) {
            Snackbar.make(root, "Назовитесь", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(nameP)) {
            Snackbar.make(root, "Введите название вашего проекта", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mainP)) {
            Snackbar.make(root, "Ваше описание проекта пустое", Snackbar.LENGTH_SHORT).show();
            return;
        }

        projectInformation();

    }

    private void projectInformation() {

        Calendar calendar = Calendar.getInstance();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyyMMddHHmmss");
        saveCurrentDate = currentDate.format(calendar.getTime());

        projectKey = UUID.randomUUID().toString();

        projectWriter.saveProjectInformation(
//                String id, String date, String time, String nameProject, String description, String author, int countcomm, int countlike
                new Project(
                        projectKey,
                        saveCurrentDate,
                        nameP,
                        mainP,
                        author
                )
        );

    }


    //Метод Push-уведомления
    private void showNotification() {
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
