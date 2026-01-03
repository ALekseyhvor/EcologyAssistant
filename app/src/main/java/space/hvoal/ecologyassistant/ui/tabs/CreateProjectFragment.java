package space.hvoal.ecologyassistant.ui.tabs;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.utils.ProjectWriter;

public class CreateProjectFragment extends Fragment {

    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    private RelativeLayout root;
    private EditText authortext, nameproject, maintext;

    private String author;
    private String nameP;
    private String mainP;

    private ProjectWriter projectWriter;

    // Уведомление (оставляем как в Activity; позже можно убрать/переделать)
    private NotificationManager nm;
    private final int NOTIFICATION_ID = 1;
    private final String CHANNEL_ID = "CHANNEL_ID";

    private ValueEventListener userListener;

    public CreateProjectFragment() {
        super(R.layout.fragment_create_project);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userRef = db.getReference().child("Users");
        projectWriter = new ProjectWriter();

        nm = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        ImageView backbtn = view.findViewById(R.id.back_button);
        Button submit = view.findViewById(R.id.buttonCreateProject);
        root = view.findViewById(R.id.root_element_crproject);

        authortext = view.findViewById(R.id.editTextAuthor);
        nameproject = view.findViewById(R.id.editNameProject);
        maintext = view.findViewById(R.id.editMainTheme);

        // Подставляем имя автора из Users/<uid>/name (как было)
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Object nameVal = snapshot.child("name").getValue();
                    if (nameVal != null) {
                        author = nameVal.toString();
                        authortext.setText(author);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        userRef.child(uid).addValueEventListener(userListener);

        authortext.setEnabled(false);

        backbtn.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        submit.setOnClickListener(v -> {
            if (!validate()) return;

            saveProject();
            showNotification();

            Snackbar.make(root, "Проект создан!", Snackbar.LENGTH_SHORT).show();

            // Возвращаемся в список проектов (вкладка “Проекты”)
            NavHostFragment.findNavController(this).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null && auth.getCurrentUser() != null) {
            userRef.child(auth.getCurrentUser().getUid()).removeEventListener(userListener);
        }
        userListener = null;
    }

    private boolean validate() {
        nameP = nameproject.getText().toString().trim();
        mainP = maintext.getText().toString().trim();

        if (TextUtils.isEmpty(author)) {
            Snackbar.make(root, "Не удалось определить автора", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(nameP)) {
            Snackbar.make(root, "Введите название вашего проекта", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mainP)) {
            Snackbar.make(root, "Ваше описание проекта пустое", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveProject() {
        Calendar calendar = Calendar.getInstance();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyyMMddHHmmss");
        String saveCurrentDate = currentDate.format(calendar.getTime());

        String projectKey = UUID.randomUUID().toString();

        projectWriter.saveProjectInformation(
                new Project(projectKey, saveCurrentDate, nameP, mainP, author)
        );
    }

    private void showNotification() {
        // Пока оставим простое уведомление (как у тебя), можно потом улучшить
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.smart_ecology_eco_nature_world_icon_)
                        .setWhen(System.currentTimeMillis())
                        .setTicker("Новое уведомление")
                        .setContentTitle("Новый проект")
                        .setContentText("Вы создали новый проект")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Вы создали новый проект"));

        createChannelIfNeeded(nm);
        nm.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createChannelIfNeeded(NotificationManager manager) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT
        );
        manager.createNotificationChannel(channel);
    }
}
