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
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.utils.ProjectWriter;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class DisscusionActivity extends AppCompatActivity {

    private ImageView backbtn;
    private RecyclerView recyclerView;
    private FirebaseDatabase db;
    private DatabaseReference refProject;
    private FirebaseAuth auth;
    private DatabaseReference usersref;
    private Switch switchDate;
    private Switch switchSubscribersCnt;
    private String username;
    private ProjectWriter projectWriter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_disscusion);

        db = FirebaseDatabase.getInstance();
        refProject = db.getReference().child("Projects");
        auth = FirebaseAuth.getInstance();
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        projectWriter = new ProjectWriter();


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
            Intent mainintent = new Intent(DisscusionActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });

        initRecyclerView();

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
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query;
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

                int visibility = Optional.ofNullable(model.getSubscribers())
                        .orElse(new ArrayList<>())
                        .contains(username)
                        ? View.INVISIBLE
                        : View.VISIBLE;
                holder.joinButton.setVisibility(visibility);

                holder.joinButton.setOnClickListener(
                        v -> {
                            holder.joinButton.setVisibility(View.INVISIBLE);
                            if (model.getSubscribers() == null) {
                                model.setSubscribers(new ArrayList<>());
                            }
                            model.getSubscribers().add(username);
                            projectWriter.saveProjectInformation(model);
                            onStart();
                        }
                );

                holder.chatButton.setOnClickListener(
                        v -> {
                            Intent chatintent = new Intent(DisscusionActivity.this, ChatActivity.class);
                            chatintent.putExtra("projectId", model.getId());
                            startActivity(chatintent);
                            finish();
                        }
                );

                holder.subscribersTextView.setText(
                        String.valueOf(
                                Optional.ofNullable(model.getSubscribers())
                                        .orElse(new ArrayList<>())
                                        .size()
                        )
                );

                holder.commTextView.setText(
                        String.valueOf(
                                Optional.ofNullable(model.getComments())
                                        .orElse(new ArrayList<>())
                                        .size()
                        )
                );
            }

            @NonNull
            @Override
            public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
                return new ProjectViewHolder(view, false);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }


}