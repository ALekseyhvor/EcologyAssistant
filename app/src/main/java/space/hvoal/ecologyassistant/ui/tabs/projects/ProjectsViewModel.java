package space.hvoal.ecologyassistant.ui.tabs.projects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import space.hvoal.ecologyassistant.data.repo.ProjectsRepository;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProjectsViewModel extends ViewModel {

    public enum SortMode { DEFAULT, DATE_DESC, SUBSCRIBERS_DESC }

    private final ProjectsRepository repo = new ProjectsRepository();

    private final MutableLiveData<SortMode> sortMode = new MutableLiveData<>(SortMode.DEFAULT);
    private final MediatorLiveData<UiState<List<Project>>> state = new MediatorLiveData<>();

    private final LiveData<UiState<List<Project>>> source = repo.observeAllProjects();

    private UiState<List<Project>> last = UiState.loading();

    public ProjectsViewModel() {
        state.addSource(source, s -> {
            last = s == null ? UiState.loading() : s;
            publish();
        });
        state.addSource(sortMode, m -> publish());
    }

    public LiveData<UiState<List<Project>>> state() {
        return state;
    }

    public void setSortMode(SortMode mode) {
        sortMode.setValue(mode);
    }

    private void publish() {
        if (last.status == UiState.Status.LOADING) {
            state.setValue(UiState.loading());
            return;
        }

        if (last.status == UiState.Status.ERROR) {
            state.setValue(UiState.error(last.error));
            return;
        }

        List<Project> list = last.data == null ? new ArrayList<>() : new ArrayList<>(last.data);
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
        repo.clear();
        super.onCleared();
    }
}
