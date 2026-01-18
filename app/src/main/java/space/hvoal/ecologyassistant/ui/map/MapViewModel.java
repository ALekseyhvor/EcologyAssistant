package space.hvoal.ecologyassistant.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import space.hvoal.ecologyassistant.data.repo.ProjectsRepository;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class MapViewModel extends ViewModel {

    private final ProjectsRepository repo = new ProjectsRepository();
    private final LiveData<UiState<List<Project>>> state = repo.observeAllProjects();

    public LiveData<UiState<List<Project>>> state() {
        return state;
    }

    @Override
    protected void onCleared() {
        repo.clear();
        super.onCleared();
    }
}
