package space.hvoal.ecologyassistant.ui.tabs.projects;

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

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectViewHolder> {

    public interface Listener {
        void onOpenDetails(Project project);
        void onSubscribe(Project project);
    }

    private final Listener listener;
    private final List<Project> items = new ArrayList<>();
    private String username;

    public ProjectsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Project> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setUsername(String username) {
        this.username = username;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(v, false);
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
            Date date = model.getDateTime() == null ? null : dateFormat.parse(model.getDateTime());
            if (date != null) viewDate = viewFormat.format(date);
        } catch (ParseException ignored) { }

        holder.creationdateTextView.setText(viewDate);

        boolean hasUsername = username != null && !username.trim().isEmpty();
        holder.joinButton.setEnabled(hasUsername);
        holder.joinButton.setAlpha(hasUsername ? 1f : 0.5f);

        boolean already = Optional.ofNullable(model.getSubscribers())
                .orElse(new ArrayList<>())
                .contains(username);

        holder.joinButton.setVisibility(already ? View.INVISIBLE : View.VISIBLE);

        holder.joinButton.setOnClickListener(v -> {
            if (!hasUsername) return;
            holder.joinButton.setVisibility(View.INVISIBLE);
            listener.onSubscribe(model);
        });

        holder.chatButton.setOnClickListener(v -> listener.onOpenDetails(model));

        holder.subscribersTextView.setText(String.valueOf(
                model.getSubscribers() == null ? 0 : model.getSubscribers().size()
        ));

        holder.commTextView.setText(String.valueOf(
                model.getComments() == null ? 0 : model.getComments().size()
        ));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
