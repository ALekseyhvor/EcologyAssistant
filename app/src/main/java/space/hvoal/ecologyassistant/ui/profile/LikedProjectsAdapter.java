package space.hvoal.ecologyassistant.ui.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class LikedProjectsAdapter extends RecyclerView.Adapter<ProjectViewHolder> {

    public interface Listener {
        void onToggleLike(@NonNull Project project);
        void onOpenDetails(@NonNull Project project);
    }

    private final List<Project> items = new ArrayList<>();
    private final Listener listener;

    public LikedProjectsAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Project> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat viewFormat = new SimpleDateFormat("MMM-dd HH:mm");

        String viewDate = "";
        try {
            Date date = dateFormat.parse(model.getDateTime());
            if (date != null) viewDate = viewFormat.format(date);
        } catch (ParseException ignored) { }
        holder.creationdateTextView.setText(viewDate);

        int commentsCount = (model.getComments() != null) ? model.getComments().size() : 0;
        holder.commTextView.setText(String.valueOf(commentsCount));

        int likesCount = (model.getLikesCount() != null) ? model.getLikesCount() : 0;
        holder.likesTextView.setText(String.valueOf(likesCount));

        holder.likeButton.setVisibility(View.VISIBLE);
        holder.likeButton.setSelected(true);

        holder.likeButton.setOnClickListener(v -> {
            holder.likeButton.setEnabled(false);

            holder.likeButton.setSelected(!holder.likeButton.isSelected());

            listener.onToggleLike(model);

            holder.likeButton.postDelayed(() -> holder.likeButton.setEnabled(true), 350);
        });

        holder.chatButton.setOnClickListener(v -> listener.onOpenDetails(model));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
