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

    public enum SortMode { NAME_ASC, DATE_DESC, SUBSCRIBERS_DESC }

    private final ProjectsRepository repo = new ProjectsRepository();

    private final MutableLiveData<SortMode> sortMode = new MutableLiveData<>(SortMode.NAME_ASC);
    private final MediatorLiveData<UiState<List<Project>>> out = new MediatorLiveData<>();

    private UiState<List<Project>> lastRepoState;

    public ProjectsViewModel() {
        LiveData<UiState<List<Project>>> src = repo.observeAllProjects();

        out.addSource(src, state -> {
            lastRepoState = state;
            out.setValue(applySort(state, sortMode.getValue()));
        });

        out.addSource(sortMode, mode -> {
            if (lastRepoState != null) out.setValue(applySort(lastRepoState, mode));
        });
    }

    public LiveData<UiState<List<Project>>> state() {
        return out;
    }

    public void setSortMode(SortMode mode) {
        sortMode.setValue(mode);
    }

    public void subscribe(String projectId, String username) {
        repo.subscribeToProject(projectId, username);
    }

    @Override
    protected void onCleared() {
        repo.clear();
        super.onCleared();
    }

    private UiState<List<Project>> applySort(UiState<List<Project>> state, SortMode mode) {
        if (state == null) return UiState.loading();
        if (state.status != UiState.Status.SUCCESS || state.data == null) return state;

        List<Project> copy = new ArrayList<>(state.data);

        if (mode == SortMode.DATE_DESC) {
            copy.sort((a, b) -> {
                String da = a.getDateTime() == null ? "" : a.getDateTime();
                String db = b.getDateTime() == null ? "" : b.getDateTime();
                return db.compareTo(da);
            });
        } else if (mode == SortMode.SUBSCRIBERS_DESC) {
            copy.sort(Comparator.comparingInt((Project p) ->
                    p.getSubscribers() == null ? 0 : p.getSubscribers().size()
            ).reversed());
        } else {
            copy.sort((a, b) -> {
                String na = a.getNameProject() == null ? "" : a.getNameProject();
                String nb = b.getNameProject() == null ? "" : b.getNameProject();
                return na.compareToIgnoreCase(nb);
            });
        }

        return UiState.success(copy);
    }
}
