package space.hvoal.ecologyassistant.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.db.Project;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private static final String PROJECT_RESPONSE_FORMAT = "yyyy-MM-dd HH:mm:ss z";
    private static final String MONTH_DAY_FORMAT = "MMM d";

    private List<Project> projectList = new ArrayList<>();


    class ProjectViewHolder extends RecyclerView.ViewHolder {

        private TextView nameUserTextView;
        private TextView nameprojectTextView;
        private TextView creationdateTextView;
        private TextView textprojectTextView;
        private TextView commTextView;
        private TextView likeTextView;

        public  ProjectViewHolder (View itemView){
            super(itemView);
            nameUserTextView = itemView.findViewById(R.id.textNameAuthor);
            nameprojectTextView = itemView.findViewById(R.id.textNameProject);
            creationdateTextView = itemView.findViewById(R.id.textTime);
            textprojectTextView = itemView.findViewById(R.id.textMainView);
            commTextView = itemView.findViewById(R.id.count_comment);
            likeTextView = itemView.findViewById(R.id.count_like);

        }

        public void bind (Project project){
            nameUserTextView.setText(project.getAuthor());
            nameprojectTextView.setText(project.getNameproject());
            textprojectTextView.setText(project.getTextproject());
            commTextView.setText(String.valueOf(project.getCountcommm()));
            likeTextView.setText(String.valueOf(project.getCountlike()));

            String creationDateFormat = getFormattedDate(project.getCreationdate());
            creationdateTextView.setText(creationDateFormat);
        }

    }

    private String getFormattedDate(String rawDate) {
        SimpleDateFormat utcFormat = new SimpleDateFormat(PROJECT_RESPONSE_FORMAT, Locale.ROOT);
        SimpleDateFormat displayFormat = new SimpleDateFormat(MONTH_DAY_FORMAT, Locale.getDefault());

        try {
            Date date = utcFormat.parse(rawDate);
            assert date != null;
            return displayFormat.format(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    //Метод для наполнения коллекции
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(Collection<Project> projects){
        projectList.addAll(projects);
        notifyDataSetChanged();
    }
    //Метод отчистки коллекции
    @SuppressLint("NotifyDataSetChanged")
    public void clearItems() {
        projectList.clear();
        notifyDataSetChanged();
    }

    //Метод для создания объекта ViewHolder
    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(view);
    }

    //Метод связывающий java объекты и view
    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(projectList.get(position));

    }

    // Метод сообщающий кол-во элементов в списке
    @Override
    public int getItemCount() {
        return projectList.size();
    }


}
