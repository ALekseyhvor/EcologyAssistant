package space.hvoal.ecologyassistant.ui.project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import space.hvoal.ecologyassistant.data.repo.ProjectDetailsRepository;
import space.hvoal.ecologyassistant.data.repo.ProjectLikesRepository;
import space.hvoal.ecologyassistant.data.repo.ProjectsRepository;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProjectDetailsViewModel extends ViewModel {

    private final ProjectDetailsRepository detailsRepo = new ProjectDetailsRepository();
    private final ProjectsRepository projectsRepo = new ProjectsRepository();
    private final ProjectLikesRepository likesRepo = new ProjectLikesRepository();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final MutableLiveData<String> projectId = new MutableLiveData<>();

    private final LiveData<UiState<Project>> state =
            Transformations.switchMap(projectId, detailsRepo::observeProjectById);

    private final LiveData<Boolean> liked =
            Transformations.switchMap(projectId, id -> {
                FirebaseUser u = auth.getCurrentUser();
                String uid = u != null ? u.getUid() : null;
                return likesRepo.observeIsLiked(id, uid);
            });

    public void setProjectId(String id) {
        if (id == null) return;
        if (id.equals(projectId.getValue())) return;
        projectId.setValue(id);
    }

    public LiveData<UiState<Project>> state() {
        return state;
    }

    public LiveData<Boolean> liked() {
        return liked;
    }

    public void toggleLike() {
        String id = projectId.getValue();
        if (id == null) return;
        projectsRepo.toggleLike(id);
    }

    @Override
    protected void onCleared() {
        detailsRepo.clear();
        likesRepo.clear();
        super.onCleared();
    }
}
