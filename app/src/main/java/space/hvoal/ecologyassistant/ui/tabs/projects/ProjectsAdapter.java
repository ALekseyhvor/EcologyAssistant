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
        void onOpenDetails(@NonNull Project project);
        void onToggleLike(@NonNull Project project);
    }

    private final List<Project> items = new ArrayList<>();
    private Listener listener;

    private String currentUid;

    private boolean showLikeButton = true;

    public ProjectsAdapter() { }

    public ProjectsAdapter(Listener listener) {
        this.listener = listener;
    }

    public ProjectsAdapter(List<Project> list, Listener listener) {
        submitList(list);
        this.listener = listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setCurrentUid(String uid) {
        this.currentUid = uid;
        notifyDataSetChanged();
    }

    public void setShowLikeButton(boolean show) {
        this.showLikeButton = show;
        notifyDataSetChanged();
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
        return new ProjectViewHolder(v, true);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project model = items.get(position);

        holder.nameUserTextView.setText(model.getAuthor());
        holder.nameprojectTextView.setText(model.getNameProject());
        holder.textprojectTextView.setText(model.getDescription());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat vf = new SimpleDateFormat("MMM-dd HH:mm");
        String viewDate = "";
        try {
            Date d = df.parse(model.getDateTime());
            if (d != null) viewDate = vf.format(d);
        } catch (ParseException ignored) {}
        holder.creationdateTextView.setText(viewDate);

        int commentsCount = Optional.ofNullable(model.getComments()).orElse(new ArrayList<>()).size();
        holder.commTextView.setText(String.valueOf(commentsCount));

        int likesCount = likesOf(model);
        holder.likesTextView.setText(String.valueOf(likesCount));

        holder.likeButton.setVisibility(showLikeButton ? View.VISIBLE : View.GONE);

        boolean liked = currentUid != null
                && model.getLikes() != null
                && Boolean.TRUE.equals(model.getLikes().get(currentUid));

        holder.likeButton.setImageResource(
                liked ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
        );

        holder.likeButton.setOnClickListener(v -> {
            if (listener == null) return;
            holder.likeButton.setEnabled(false);
            listener.onToggleLike(model);
            holder.likeButton.setEnabled(true);
        });

        holder.chatButton.setOnClickListener(v -> {
            if (listener == null) return;
            listener.onOpenDetails(model);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int likesOf(Project p) {
        if (p == null) return 0;
        if (p.getLikesCount() != null) return p.getLikesCount();
        return p.getLikes() == null ? 0 : p.getLikes().size();
    }
}

