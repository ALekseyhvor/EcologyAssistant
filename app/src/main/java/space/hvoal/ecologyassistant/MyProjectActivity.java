package space.hvoal.ecologyassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseArray;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class MyProjectActivity extends AppCompatActivity {

    private ImageView backbtn;
    private RecyclerView recyclerMyView;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private DatabaseReference refProject;
    private DatabaseReference usersref;
    private Switch switchDate;
    private Switch switchSubscribersCnt;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл

        setContentView(R.layout.activity_my_project);


        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");

        refProject = db.getReference().child("Projects");

        backbtn = findViewById(R.id.back_button);
        switchDate = findViewById(R.id.switchDate);
        switchSubscribersCnt = findViewById(R.id.switchSubscribersCnt);

        switchDate.setOnClickListener(
                v -> {
                    if (switchDate.isChecked()) {
                        switchSubscribersCnt.setChecked(false);
                    }
                    onStart();
                }
        );

        switchSubscribersCnt.setOnClickListener(
                v -> {
                    if (switchSubscribersCnt.isChecked()) {
                        switchDate.setChecked(false);
                    }
                    onStart();
                }
        );

        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(MyProjectActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });

        usersref.child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                                    username = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        }
                );
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerMyView = findViewById(R.id.recycleMyView);
        recyclerMyView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {

        super.onStart();

        Query query = refProject.equalTo(username, "author");
        if (switchDate.isChecked()) {
            query = refProject.orderByChild("dateTime");
        } else if (switchSubscribersCnt.isChecked()) {
            query = refProject.orderByChild("subscribers");
        } else {
            query = refProject.orderByChild("nameProject");
        }

        FirebaseRecyclerOptions<Project> options = new FirebaseRecyclerOptions.Builder<Project>()
                .setSnapshotArray(
                        new FirebaseArray<>(
                                query,
                                new ClassSnapshotParser<>(Project.class)
                        )
                )
                .build();

        FirebaseRecyclerAdapter<Project, ProjectViewHolder> adapter = new FirebaseRecyclerAdapter<Project, ProjectViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProjectViewHolder holder, int position, @NonNull Project model) {
                holder.nameUserTextView.setText(model.getAuthor());
                holder.nameprojectTextView.setText(model.getNameProject());
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat viewFormat = new SimpleDateFormat("MMM-dd HH:mm");
                String viewDate = null;
                try {
                    Date date = dateFormat.parse(model.getDateTime());
                    viewDate = viewFormat.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                holder.creationdateTextView.setText(viewDate);
                holder.textprojectTextView.setText(model.getDescription());
            }

            @NonNull
            @Override
            public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
                return new ProjectViewHolder(view, true);
            }
        };

        recyclerMyView.setAdapter(adapter);
        adapter.startListening();

    }
}