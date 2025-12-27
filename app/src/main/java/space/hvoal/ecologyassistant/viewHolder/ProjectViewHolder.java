package space.hvoal.ecologyassistant.viewHolder;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import space.hvoal.ecologyassistant.Interface.ItemClickListener;
import space.hvoal.ecologyassistant.R;

public class ProjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView nameUserTextView;
    public TextView nameprojectTextView;
    public TextView creationdateTextView;
    public TextView textprojectTextView;
    public TextView commTextView;
    public TextView subscribersTextView;
    public Button joinButton;
    public Button chatButton;
    public ItemClickListener listener;


    public ProjectViewHolder(@NonNull View itemView, boolean selfProjects) {

        super(itemView);

        nameUserTextView = itemView.findViewById(R.id.textNameAuthor);
        nameprojectTextView = itemView.findViewById(R.id.textNameProject);
        creationdateTextView = itemView.findViewById(R.id.textTime);
        textprojectTextView = itemView.findViewById(R.id.textMainView);
        commTextView = itemView.findViewById(R.id.count_comment);
        subscribersTextView = itemView.findViewById(R.id.count_subscribers);
        joinButton = itemView.findViewById(R.id.joinButton);
        chatButton = itemView.findViewById(R.id.chatButton);
        int visible = selfProjects ? View.INVISIBLE : View.VISIBLE;
        joinButton.setVisibility(visible);
    }

    public void setItemClickListner(ItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view, getAbsoluteAdapterPosition(), false);
    }
}
