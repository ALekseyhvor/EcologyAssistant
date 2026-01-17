package space.hvoal.ecologyassistant.ui.profile;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class MyProjectsAdapter extends RecyclerView.Adapter<ProjectViewHolder> {

    public interface Listener {
        void onOpen(Project project);
    }

    private final Listener listener;
    private final List<Project> items = new ArrayList<>();

    public MyProjectsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Project> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(v, true); // selfProjects=true => joinButton скрыт
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project model = items.get(position);

        holder.nameUserTextView.setText(model.getAuthor());
        holder.nameprojectTextView.setText(model.getNameProject());
        holder.textprojectTextView.setText(model.getDescription());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat viewFormat = new SimpleDateFormat("MMM-dd HH:mm");

        String viewDate = "";
        try {
            Date date = dateFormat.parse(model.getDateTime());
            if (date != null) viewDate = viewFormat.format(date);
        } catch (ParseException ignored) { }

        holder.creationdateTextView.setText(viewDate);

        holder.subscribersTextView.setText(String.valueOf(
                Optional.ofNullable(model.getSubscribers()).orElse(new ArrayList<>()).size()
        ));

        holder.commTextView.setText(String.valueOf(
                Optional.ofNullable(model.getComments()).orElse(new ArrayList<>()).size()
        ));

        holder.chatButton.setOnClickListener(v -> listener.onOpen(model));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
