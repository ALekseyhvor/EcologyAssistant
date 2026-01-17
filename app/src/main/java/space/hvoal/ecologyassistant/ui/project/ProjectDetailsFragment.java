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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.data.category.Categories;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProjectDetailsFragment extends Fragment {

    private String projectId;

    public ProjectDetailsFragment() {
        super(R.layout.fragment_project_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        projectId = args != null ? args.getString("projectId") : null;

        ImageView backBtn = view.findViewById(R.id.back_button);

        TextView tvTitle = view.findViewById(R.id.tvProjectTitle);
        TextView tvAuthor = view.findViewById(R.id.tvProjectAuthor);
        TextView tvDate = view.findViewById(R.id.tvProjectDate);
        TextView tvCategory = view.findViewById(R.id.tvProjectCategory);
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

        ProjectDetailsViewModel vm = new ViewModelProvider(this).get(ProjectDetailsViewModel.class);
        vm.setProjectId(projectId);

        vm.state().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.status == UiState.Status.LOADING) {
                tvTitle.setText("Загрузка...");
                btnComments.setEnabled(false);
                return;
            }

            if (state.status == UiState.Status.ERROR) {
                tvTitle.setText("Ошибка");
                Snackbar.make(view, "Ошибка: " + state.error, Snackbar.LENGTH_LONG).show();
                btnComments.setEnabled(false);
                return;
            }

            Project project = state.data;
            if (project == null) {
                tvTitle.setText("Проект не найден");
                btnComments.setEnabled(false);
                return;
            }

            tvTitle.setText(safe(project.getNameProject()));
            tvAuthor.setText("Автор: " + safe(project.getAuthor()));
            tvDesc.setText(safe(project.getDescription()));
            tvDate.setText("Дата: " + formatProjectDate(project.getDateTime()));

            String catId = project.getCategoryId() != null ? project.getCategoryId() : Categories.OTHER;
            tvCategory.setText("Категория: " + categoryTitle(catId));

            int commentsCount = project.getComments() == null ? 0 : project.getComments().size();
            btnComments.setText("Комментарии (" + commentsCount + ")");
            btnComments.setEnabled(true);
        });
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @SuppressLint("SimpleDateFormat")
    private String formatProjectDate(String raw) {
        if (raw == null) return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat viewFormat = new SimpleDateFormat("MMM-dd HH:mm");
        try {
            Date d = dateFormat.parse(raw);
            return d == null ? "" : viewFormat.format(d);
        } catch (ParseException ignored) {
            return "";
        }
    }

    private String categoryTitle(String categoryId) {
        for (Categories.Item it : Categories.all()) {
            if (it.id.equals(categoryId)) return it.title;
        }
        return "Другое";
    }
}
