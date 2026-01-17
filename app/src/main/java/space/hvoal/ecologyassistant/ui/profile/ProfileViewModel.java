package space.hvoal.ecologyassistant.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import space.hvoal.ecologyassistant.data.repo.UserRepository;
import space.hvoal.ecologyassistant.model.User;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class ProfileViewModel extends ViewModel {

    private final UserRepository repo = new UserRepository();
    private final LiveData<UiState<User>> userState = repo.observeCurrentUser();

    public LiveData<UiState<User>> userState() {
        return userState;
    }

    public void logout() {
        repo.signOut();
    }

    @Override
    protected void onCleared() {
        repo.clear();
        super.onCleared();
    }
}
