package space.hvoal.ecologyassistant.ui.comments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Comment;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.utils.ProjectWriter;

public class ProjectCommentsFragment extends Fragment {

    private ImageView backBtn;
    private RecyclerView rv;
    private Button sendButton;
    private EditText editMessageText;

    private final CommentsAdapter adapter = new CommentsAdapter();

    private FirebaseAuth auth;
    private DatabaseReference projectsRef;
    private DatabaseReference usersRef;

    private String projectId;
    private String username;
    private Project project;

    private ValueEventListener projectListener;

    private final ProjectWriter projectWriter = new ProjectWriter();

    public ProjectCommentsFragment() {
        super(R.layout.fragment_project_comments);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        projectsRef = FirebaseDatabase.getInstance().getReference("Projects");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // views
        backBtn = view.findViewById(R.id.back_button);
        rv = view.findViewById(R.id.commentsRecyclerView);
        sendButton = view.findViewById(R.id.sendButton);
        editMessageText = view.findViewById(R.id.editMessageText);

        backBtn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // args
        Bundle args = getArguments();
        if (args != null) projectId = args.getString("projectId");

        if (projectId == null) {
            Snackbar.make(view, "projectId не передан", Snackbar.LENGTH_LONG).show();
            sendButton.setEnabled(false);
            return;
        }

        // 1) Загружаем имя текущего пользователя (одноразово)
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object nameVal = snapshot.child("name").getValue();
                username = nameVal != null ? nameVal.toString() : null;
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        // 2) Слушаем проект, чтобы обновлялся список комментариев
        projectListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                project = snapshot.getValue(Project.class);

                List<Comment> comments = Optional.ofNullable(project)
                        .map(Project::getComments)
                        .orElse(new ArrayList<>());

                adapter.setItems(comments);

                if (!comments.isEmpty()) {
                    rv.scrollToPosition(comments.size() - 1);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };

        projectsRef.child(projectId).addValueEventListener(projectListener);

        // 3) Отправка комментария
        sendButton.setOnClickListener(v -> {
            String text = editMessageText.getText().toString().trim();

            if (TextUtils.isEmpty(text)) {
                Snackbar.make(view, "Введите текст комментария", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (username == null || username.trim().isEmpty()) {
                Snackbar.make(view, "Имя пользователя ещё не загрузилось", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (project == null) {
                Snackbar.make(view, "Проект ещё не загрузился", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (project.getComments() == null) project.setComments(new ArrayList<>());
            project.getComments().add(new Comment(username, text));

            editMessageText.setText("");

            // сохраняем проект целиком (как в старом ChatActivity)
            projectWriter.saveProjectInformation(project);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (projectListener != null && projectId != null) {
            projectsRef.child(projectId).removeEventListener(projectListener);
        }
        projectListener = null;
        project = null;
    }
}
