package space.hvoal.ecologyassistant.viewHolder;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import space.hvoal.ecologyassistant.DisscusionActivity;
import space.hvoal.ecologyassistant.Interface.ItemClickListener;
import space.hvoal.ecologyassistant.R;

public class ProjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView nameUserTextView;
    public TextView nameprojectTextView;
    public TextView creationdateTextView;
    public TextView textprojectTextView;
    public TextView commTextView;
    public TextView likeTextView;
    public ItemClickListener listener;


    public ProjectViewHolder(@NonNull View itemView) {
        super(itemView);

        nameUserTextView = itemView.findViewById(R.id.textNameAuthor);
        nameprojectTextView = itemView.findViewById(R.id.textNameProject);
        creationdateTextView = itemView.findViewById(R.id.textTime);
        textprojectTextView = itemView.findViewById(R.id.textMainView);
        commTextView = itemView.findViewById(R.id.count_comment);
        likeTextView = itemView.findViewById(R.id.count_like);

        itemView.setOnClickListener(view -> {
            int positionIndex = getAbsoluteAdapterPosition();
            Log.d(TAG, "onItemClick position: " + positionIndex);
        });

    }

    public void setItemClickListner(ItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view, getAbsoluteAdapterPosition(), false);
    }


}
