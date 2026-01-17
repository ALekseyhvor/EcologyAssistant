package space.hvoal.ecologyassistant.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import space.hvoal.ecologyassistant.data.repo.MyProjectsRepository;
import space.hvoal.ecologyassistant.data.repo.UserRepository;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.model.User;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class MyProjectsViewModel extends ViewModel {

    public enum SortMode { DEFAULT, DATE_DESC, SUBSCRIBERS_DESC }

    private final UserRepository userRepo = new UserRepository();
    private final MyProjectsRepository projectsRepo = new MyProjectsRepository();

    private final MutableLiveData<SortMode> sortMode = new MutableLiveData<>(SortMode.DEFAULT);
    private final MediatorLiveData<UiState<List<Project>>> state = new MediatorLiveData<>();

    private LiveData<UiState<User>> userState;
    private LiveData<UiState<List<Project>>> projectsState;

    private UiState<List<Project>> lastProjectsState = UiState.loading();

    public MyProjectsViewModel() {
        userState = userRepo.observeCurrentUser();
        state.addSource(userState, this::onUserState);
        state.addSource(sortMode, m -> publish());
    }

    public LiveData<UiState<List<Project>>> state() {
        return state;
    }

    public void setSortMode(SortMode mode) {
        sortMode.setValue(mode);
    }

    private void onUserState(UiState<User> s) {
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
        if (u == null || u.getName() == null || u.getName().trim().isEmpty()) {
            state.setValue(UiState.error("Не удалось определить имя пользователя"));
            return;
        }

        if (projectsState != null) state.removeSource(projectsState);

        projectsState = projectsRepo.observeByAuthor(u.getName());
        state.addSource(projectsState, ps -> {
            lastProjectsState = ps == null ? UiState.loading() : ps;
            publish();
        });
    }

    private void publish() {
        UiState<List<Project>> s = lastProjectsState;

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
            list.sort(Comparator.comparingInt(this::likesOf).reversed());
        }

        state.setValue(UiState.success(list));
    }

    private int likesOf(Project p) {
        if (p == null) return 0;
        if (p.getLikesCount() != null) return p.getLikesCount();
        return p.getLikes() == null ? 0 : p.getLikes().size();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    protected void onCleared() {
        userRepo.clear();
        projectsRepo.clear();
        super.onCleared();
    }
}

