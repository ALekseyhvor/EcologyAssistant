package space.hvoal.ecologyassistant.ui.comments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Comment;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProjectCommentsFragment extends Fragment {

    private final CommentsAdapter adapter = new CommentsAdapter();

    private ProjectCommentsViewModel vm;

    private String projectId;

    private ImageView backBtn;
    private RecyclerView rv;
    private Button sendButton;
    private EditText editMessageText;

    public ProjectCommentsFragment() {
        super(R.layout.fragment_project_comments);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        projectId = args != null ? args.getString("projectId") : null;

        if (projectId == null) {
            Snackbar.make(view, "projectId не передан", Snackbar.LENGTH_LONG).show();
            sendButton.setEnabled(false);
            return;
        }

        vm = new ViewModelProvider(this).get(ProjectCommentsViewModel.class);
        vm.setProjectId(projectId);

        sendButton.setEnabled(false);

        vm.username().observe(getViewLifecycleOwner(), username -> {
            boolean ready = username != null && !username.trim().isEmpty();
            sendButton.setEnabled(ready);
        });

        vm.commentsState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.status == UiState.Status.ERROR) {
                Snackbar.make(view, "Ошибка загрузки комментариев: " + state.error, Snackbar.LENGTH_LONG).show();
                return;
            }

            if (state.status == UiState.Status.SUCCESS) {
                List<Comment> comments = state.data;
                adapter.setItems(comments);
                if (comments != null && !comments.isEmpty()) {
                    rv.scrollToPosition(comments.size() - 1);
                }
            }
        });

        vm.sendState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.status == UiState.Status.ERROR) {
                Snackbar.make(view, state.error, Snackbar.LENGTH_SHORT).show();
            }

            if (state.status == UiState.Status.SUCCESS) {
                editMessageText.setText("");
            }
        });

        sendButton.setOnClickListener(v -> vm.sendComment(editMessageText.getText().toString()));
    }
}
