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

import java.util.Arrays;
import java.util.Collection;

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
                new Project("Papa", 1L,"Полнейший разъёб", "Приветсвтую всех сегодня я вам расскажу а не знаю что то толываиаолитылофваитфыволаитолфыатфыволатол",
                        "2022-06-03 19:54:50 UTC", 4L, 545L),
                new Project("Papa", 2L,"Полнейший разъёб2", "Приветсвтую всех сегодня я вам расскажу а не знаю что то толываиаолитылофваитфыволаитолфыатфыволатол",
                "2022-06-03 19:54:50 UTC", 454L, 567L),
                new Project("Papa", 3L,"Полнейший разъёб3", "Приветсвтую всех сегодня я вам расскажу а не знаю что то толываиаолитылофваитфыволаитолфыатфыволатол",
                        "2022-06-03 19:54:50 UTC", 44L, 555L)
        );
    }


}