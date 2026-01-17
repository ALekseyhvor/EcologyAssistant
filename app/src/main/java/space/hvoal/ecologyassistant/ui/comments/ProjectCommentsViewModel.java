package space.hvoal.ecologyassistant.ui.comments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.List;

import space.hvoal.ecologyassistant.data.repo.CommentsRepository;
import space.hvoal.ecologyassistant.model.Comment;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProjectCommentsViewModel extends ViewModel {

    private final CommentsRepository repo = new CommentsRepository();

    private final MutableLiveData<String> projectId = new MutableLiveData<>();
    private final LiveData<UiState<List<Comment>>> commentsState =
            Transformations.switchMap(projectId, repo::observeComments);

    private final MutableLiveData<UiState<Boolean>> sendState = new MutableLiveData<>();

    public void setProjectId(String id) {
        if (id == null) return;
        if (id.equals(projectId.getValue())) return;
        projectId.setValue(id);
    }

    public LiveData<UiState<List<Comment>>> commentsState() {
        return commentsState;
    }

    public LiveData<String> username() {
        return repo.username();
    }

    public LiveData<UiState<Boolean>> sendState() {
        return sendState;
    }

    public void sendComment(String text) {
        String id = projectId.getValue();
        if (id == null) {
            sendState.setValue(UiState.error("projectId не задан"));
            return;
        }

        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            sendState.setValue(UiState.error("Введите текст комментария"));
            return;
        }

        sendState.setValue(UiState.loading());

        repo.addComment(id, trimmed, new CommentsRepository.Callback() {
            @Override public void onSuccess() {
                sendState.postValue(UiState.success(true));
            }
            @Override public void onError(String message) {
                sendState.postValue(UiState.error(message));
            }
        });
    }

    @Override
    protected void onCleared() {
        repo.clear();
        super.onCleared();
    }
}
