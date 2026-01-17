package space.hvoal.ecologyassistant.ui.tabs.projects;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final DatabaseReference likesRef =
            FirebaseDatabase.getInstance().getReference().child("Likes");


    private final Map<ProjectViewHolder, ListenerEntry> likeListeners = new HashMap<>();

    private final Map<String, Boolean> likedCache = new HashMap<>();

    private static class ListenerEntry {
        DatabaseReference ref;
        ValueEventListener listener;
    }

    public ProjectsAdapter() { }

    public ProjectsAdapter(Listener listener) {
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
        return new ProjectViewHolder(v, showLikeButton);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project model = items.get(position);

        holder.nameUserTextView.setText(model.getAuthor());
        holder.nameprojectTextView.setText(model.getNameProject());
        holder.textprojectTextView.setText(model.getDescription());
        holder.creationdateTextView.setText(formatDate(model.getDateTime()));

        int commentsCount = Optional.ofNullable(model.getComments()).orElse(new ArrayList<>()).size();
        holder.commTextView.setText(String.valueOf(commentsCount));

        int likesCount = likesOf(model);
        holder.likesTextView.setText(String.valueOf(likesCount));

        holder.likeButton.setVisibility(showLikeButton ? View.VISIBLE : View.GONE);

        detachLikeListener(holder);

        String projectId = model.getId();
        boolean cachedLiked = projectId != null && Boolean.TRUE.equals(likedCache.get(projectId));
        holder.likeButton.setImageResource(
                cachedLiked ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
        );

        holder.likeButton.setEnabled(currentUid != null);

        if (currentUid != null && projectId != null) {
            DatabaseReference ref = likesRef.child(projectId).child(currentUid);

            ValueEventListener l = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean liked = snapshot.exists();
                    likedCache.put(projectId, liked);

                    holder.likeButton.setImageResource(
                            liked ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
                    );
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };

            ref.addValueEventListener(l);

            ListenerEntry entry = new ListenerEntry();
            entry.ref = ref;
            entry.listener = l;
            likeListeners.put(holder, entry);
        }

        holder.likeButton.setOnClickListener(v -> {
            if (listener == null) return;
            if (currentUid == null) return;

            holder.likeButton.setEnabled(false);
            listener.onToggleLike(model);
            holder.likeButton.postDelayed(() -> holder.likeButton.setEnabled(true), 400);
        });

        holder.chatButton.setOnClickListener(v -> {
            if (listener == null) return;
            listener.onOpenDetails(model);
        });
    }

    @Override
    public void onViewRecycled(@NonNull ProjectViewHolder holder) {
        detachLikeListener(holder);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void detachLikeListener(@NonNull ProjectViewHolder holder) {
        ListenerEntry entry = likeListeners.remove(holder);
        if (entry != null && entry.ref != null && entry.listener != null) {
            entry.ref.removeEventListener(entry.listener);
        }
    }

    private int likesOf(Project p) {
        if (p == null) return 0;
        if (p.getLikesCount() != null) return p.getLikesCount();
        if (p.getLikes() != null) return p.getLikes().size();
        return 0;
    }

    private String formatDate(String raw) {
        if (raw == null) return "";
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat vf = new SimpleDateFormat("MMM-dd HH:mm");
        try {
            Date d = df.parse(raw);
            return d == null ? "" : vf.format(d);
        } catch (ParseException ignored) {
            return "";
        }
    }
}
