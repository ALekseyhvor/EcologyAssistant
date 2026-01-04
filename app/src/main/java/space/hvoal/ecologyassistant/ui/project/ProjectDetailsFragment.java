package space.hvoal.ecologyassistant.ui.project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;

public class ProjectDetailsFragment extends Fragment {

    private DatabaseReference refProject;
    private ValueEventListener projectListener;

    private String projectId;
    private Project project;

    public ProjectDetailsFragment() {
        super(R.layout.fragment_project_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refProject = FirebaseDatabase.getInstance().getReference("Projects");

        Bundle args = getArguments();
        if (args != null) projectId = args.getString("projectId");

        ImageView backBtn = view.findViewById(R.id.back_button);
        TextView tvTitle = view.findViewById(R.id.tvProjectTitle);
        TextView tvAuthor = view.findViewById(R.id.tvProjectAuthor);
        TextView tvDate = view.findViewById(R.id.tvProjectDate);
        TextView tvDesc = view.findViewById(R.id.tvProjectDescription);
        Button btnComments = view.findViewById(R.id.btnOpenComments);

        backBtn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        btnComments.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("projectId", projectId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_projectDetails_to_comments, b);
        });

        if (projectId == null) {
            tvTitle.setText("Проект не найден (projectId=null)");
            btnComments.setEnabled(false);
            return;
        }

        projectListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                project = snapshot.getValue(Project.class);
                if (project == null) {
                    tvTitle.setText("Проект не найден");
                    btnComments.setEnabled(false);
                    return;
                }

                tvTitle.setText(project.getNameProject());
                tvAuthor.setText("Автор: " + project.getAuthor());
                tvDesc.setText(project.getDescription());

                // дата как в списке
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat viewFormat = new SimpleDateFormat("MMM-dd HH:mm");
                String viewDate = "";
                try {
                    Date d = dateFormat.parse(project.getDateTime());
                    if (d != null) viewDate = viewFormat.format(d);
                } catch (ParseException ignored) { }
                tvDate.setText("Дата: " + viewDate);

                int cnt = Optional.ofNullable(project.getComments()).orElse(new ArrayList<>()).size();
                btnComments.setText("Комментарии (" + cnt + ")");
                btnComments.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        refProject.child(projectId).addValueEventListener(projectListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (projectListener != null && projectId != null) {
            refProject.child(projectId).removeEventListener(projectListener);
        }
        projectListener = null;
    }
}
