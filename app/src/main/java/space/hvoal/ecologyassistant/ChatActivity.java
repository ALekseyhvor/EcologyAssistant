package space.hvoal.ecologyassistant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import space.hvoal.ecologyassistant.model.Comment;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.utils.ProjectWriter;

public class ChatActivity extends AppCompatActivity {

    private ImageView backbtn;
    private RecyclerView chatRecyclerView;
    private Button sendButton;
    private EditText editMessageText;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference refProject;
    private DatabaseReference usersref;
    private Project project;
    private ProjectWriter projectWriter;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_chat);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        refProject = db.getReference("Projects");
        usersref = db.getReference().child("Users");
        projectWriter = new ProjectWriter();


        backbtn = findViewById(R.id.back_button);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        sendButton = findViewById(R.id.sendButton);
        editMessageText = findViewById(R.id.editMessageText);


        backbtn.setOnClickListener(view -> {
            Intent mainintent = new Intent(ChatActivity.this, DisscusionActivity.class);
            startActivity(mainintent);
            finish();
        });

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Bundle bundle = getIntent().getExtras();
        refProject.child(bundle.getString("projectId"))
                .addValueEventListener(
                        new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                project = snapshot.getValue(Project.class);
                                AtomicReference<List<Comment>> comments = new AtomicReference<>(new ArrayList<>());
                                Optional.ofNullable(project).ifPresent(
                                        projectVal -> comments.set(Optional.ofNullable(projectVal.getComments()).orElse(new ArrayList<>()))
                                );
                                chatRecyclerView.setAdapter(
                                        new ChatAdapter(comments.get()) {
                                            @NonNull
                                            @Override
                                            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                                Context context = parent.getContext();
                                                LayoutInflater inflater = LayoutInflater.from(context);

                                                // Inflate the custom layout
                                                View contactView = inflater.inflate(R.layout.chat_item, parent, false);

                                                // Return a new holder instance
                                                return new ViewHolder(contactView);
                                            }

                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                                                // Get the data model based on position
                                                Comment comment = comments.get(position);

                                                // Set item views based on your views and data model
                                                holder.authorTextView.setText(comment.getAuthor() + ": ");
                                                holder.messageTextView.setText(comment.getComment());
                                            }

                                            @Override
                                            public int getItemCount() {
                                                return comments.size();
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                System.err.println(error);
                            }
                        }
                );

        usersref.child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                                    username = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );


        sendButton.setOnClickListener(
                v -> {
                    if (project.getComments() == null) {
                        project.setComments(new ArrayList<>());
                    }
                    project.getComments().add(
                            new Comment(
                                    username,
                                    editMessageText.getText().toString()
                            )
                    );
                    projectWriter.saveProjectInformation(project);
                    onStart();
                }
        );
    }

    public abstract static class ChatAdapter extends
            RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        protected List<Comment> comments;

        public ChatAdapter(List<Comment> chats) {
            this.comments = chats;
        }

        // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView messageTextView;
            public TextView authorTextView;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
                authorTextView = (TextView) itemView.findViewById(R.id.authorTextView);
            }
        }
    }
}
