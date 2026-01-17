package space.hvoal.ecologyassistant.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import space.hvoal.ecologyassistant.Interface.ItemClickListener;
import space.hvoal.ecologyassistant.R;

public class ProjectViewHolder extends RecyclerView.ViewHolder {

    public TextView nameUserTextView;
    public TextView nameprojectTextView;
    public TextView creationdateTextView;
    public TextView textprojectTextView;

    public TextView commTextView;
    public TextView likesTextView;

    public ImageButton likeButton;
    public Button chatButton;

    public ItemClickListener listener;

    public ProjectViewHolder(@NonNull View itemView, boolean showLikeButton) {
        super(itemView);

        nameUserTextView = itemView.findViewById(R.id.textNameAuthor);
        nameprojectTextView = itemView.findViewById(R.id.textNameProject);
        creationdateTextView = itemView.findViewById(R.id.textTime);
        textprojectTextView = itemView.findViewById(R.id.textMainView);

        commTextView = itemView.findViewById(R.id.count_comment);
        likesTextView = itemView.findViewById(R.id.count_likes);

        likeButton = itemView.findViewById(R.id.likeButton);
        chatButton = itemView.findViewById(R.id.chatButton);

        likeButton.setVisibility(showLikeButton ? View.VISIBLE : View.GONE);
    }
}
