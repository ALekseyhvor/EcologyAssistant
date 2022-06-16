package space.hvoal.ecologyassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class DisscusionActivity extends AppCompatActivity {

    private ImageView backbtn;
    private RecyclerView recyclerView;
    private FirebaseDatabase db;
    private DatabaseReference refProject;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_disscusion);

        db = FirebaseDatabase.getInstance();
        refProject = db.getReference().child("Projects");


        backbtn = findViewById(R.id.back_button);


        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(DisscusionActivity.this, MainPageActivity.class);
            startActivity(mainintent);
            finish();
        });

        initRecyclerView();


    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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


            }

            @NonNull
            @Override
            public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
                return new ProjectViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }


}