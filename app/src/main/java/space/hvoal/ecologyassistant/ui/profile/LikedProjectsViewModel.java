package space.hvoal.ecologyassistant.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import space.hvoal.ecologyassistant.data.repo.LikedProjectsRepository;
import space.hvoal.ecologyassistant.data.repo.UserRepository;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.model.User;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class LikedProjectsViewModel extends ViewModel {

    public enum SortMode { DEFAULT, DATE_DESC, SUBSCRIBERS_DESC }

    private final UserRepository userRepo = new UserRepository();
    private final LikedProjectsRepository likedRepo = new LikedProjectsRepository();

    private final MutableLiveData<SortMode> sortMode = new MutableLiveData<>(SortMode.DEFAULT);
    private final MediatorLiveData<UiState<List<Project>>> state = new MediatorLiveData<>();

    private LiveData<UiState<User>> userState;
    private LiveData<UiState<List<Project>>> likedState;

    private UiState<List<Project>> lastLiked = UiState.loading();

    public LikedProjectsViewModel() {
        userState = userRepo.observeCurrentUser();
        state.addSource(userState, this::onUser);
        state.addSource(sortMode, m -> publish());
    }

    public LiveData<UiState<List<Project>>> state() {
        return state;
    }

    public void setSortMode(SortMode mode) {
        sortMode.setValue(mode);
    }

    private void onUser(UiState<User> s) {
        if (s == null) return;

        if (s.status == UiState.Status.LOADING) {
            state.setValue(UiState.loading());
            return;
        }

        if (s.status == UiState.Status.ERROR) {
            state.setValue(UiState.error(s.error));
            return;
        }

        User u = s.data;
        String username = u != null ? u.getName() : null;
        if (username == null || username.trim().isEmpty()) {
            state.setValue(UiState.error("Не удалось определить имя пользователя"));
            return;
        }

        if (likedState != null) state.removeSource(likedState);

        likedState = likedRepo.observeLikedByUsername(username);
        state.addSource(likedState, ls -> {
            lastLiked = ls == null ? UiState.loading() : ls;
            publish();
        });
    }

    private void publish() {
        UiState<List<Project>> s = lastLiked;
        if (s.status == UiState.Status.LOADING) {
            state.setValue(UiState.loading());
            return;
        }
        if (s.status == UiState.Status.ERROR) {
            state.setValue(UiState.error(s.error));
            return;
        }

        List<Project> list = s.data == null ? new ArrayList<>() : new ArrayList<>(s.data);
        SortMode mode = sortMode.getValue() == null ? SortMode.DEFAULT : sortMode.getValue();

        if (mode == SortMode.DATE_DESC) {
            list.sort((a, b) -> safe(b.getDateTime()).compareTo(safe(a.getDateTime())));
        } else if (mode == SortMode.SUBSCRIBERS_DESC) {
            list.sort(Comparator.comparingInt((Project p) ->
                    p.getSubscribers() == null ? 0 : p.getSubscribers().size()
            ).reversed());
        }

        state.setValue(UiState.success(list));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    protected void onCleared() {
        userRepo.clear();
        likedRepo.clear();
        super.onCleared();
    }
}
