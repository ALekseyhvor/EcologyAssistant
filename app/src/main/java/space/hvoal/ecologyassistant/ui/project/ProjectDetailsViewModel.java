package space.hvoal.ecologyassistant.ui.project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import space.hvoal.ecologyassistant.data.repo.ProjectDetailsRepository;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProjectDetailsViewModel extends ViewModel {

    private final ProjectDetailsRepository repo = new ProjectDetailsRepository();

    private final MutableLiveData<String> projectId = new MutableLiveData<>();
    private final LiveData<UiState<Project>> state =
            Transformations.switchMap(projectId, repo::observeProjectById);

    public void setProjectId(String id) {
        if (id == null) return;
        if (id.equals(projectId.getValue())) return;
        projectId.setValue(id);
    }

    public LiveData<UiState<Project>> state() {
        return state;
    }

    @Override
    protected void onCleared() {
        repo.clear();
        super.onCleared();
    }
}
