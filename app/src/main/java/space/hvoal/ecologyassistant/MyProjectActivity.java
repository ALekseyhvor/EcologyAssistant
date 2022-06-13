package space.hvoal.ecologyassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class MyProjectActivity extends AppCompatActivity {

    private ImageView backbtn;
    private RecyclerView recyclerMyView;
    private FirebaseDatabase db;
    private FirebaseAuth mAuth;
    private DatabaseReference refProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //скрываем нижнию панель
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //ночная тема выкл
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_project);


        db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        refProject = db.getReference().child("Projects");



        backbtn = findViewById(R.id.back_button);

        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(MyProjectActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });


        initRecyclerView();


    }

    private void initRecyclerView(){
        recyclerMyView = findViewById(R.id.recycleMyView);

        recyclerMyView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Project> options = new FirebaseRecyclerOptions.Builder<Project>()
                .setQuery(refProject, Project.class).build();

        FirebaseRecyclerAdapter<Project, ProjectViewHolder> adapter = new FirebaseRecyclerAdapter<Project, ProjectViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProjectViewHolder holder, int position, @NonNull Project model) {
                holder.nameUserTextView.setText(model.getAuthor());
                holder.nameprojectTextView.setText(model.getNameProject());
                holder.creationdateTextView.setText(model.getDate());
                holder.textprojectTextView.setText(model.getDescription());
//                holder.commTextView.setText(model.getCountcomm());
//                holder.likeTextView.setText(model.getCountlike());

            }

            @NonNull
            @Override
            public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
                return new ProjectViewHolder(view);
            }
        };

        recyclerMyView.setAdapter(adapter);
        adapter.startListening();

    }
}