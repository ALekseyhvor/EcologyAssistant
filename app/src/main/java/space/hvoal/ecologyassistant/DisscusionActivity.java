package space.hvoal.ecologyassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import space.hvoal.ecologyassistant.adapter.ProjectAdapter;
import space.hvoal.ecologyassistant.db.Project;

public class DisscusionActivity extends AppCompatActivity {

    private ImageView backbtn;
    private RecyclerView recyclerView;
    private ProjectAdapter projectAdapter;

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

        initRecyclerView();
        loadProject();

    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recycleView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        projectAdapter = new ProjectAdapter();
        recyclerView.setAdapter(projectAdapter);
    }

    private void loadProject() {
        Collection<Project> project = getProject() ;
        projectAdapter.setItems(project);
    }

   private Collection<Project> getProject() {
        return Arrays.asList(
                new Project("Papa", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                        "Полнейший разъёб", "Приветсвтую всех сегодня я вам расскажу а не знаю что то толываиаолитылофваитфыволаитолфыа",
                        "2022-06-03 19:54:50 UTC", 4L, 545L)
        );
    }


}