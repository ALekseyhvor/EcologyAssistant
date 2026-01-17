package space.hvoal.ecologyassistant.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import space.hvoal.ecologyassistant.data.repo.UserRepository;
import space.hvoal.ecologyassistant.model.User;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class EditProfileViewModel extends ViewModel {

    private final UserRepository repo = new UserRepository();

    private final LiveData<UiState<User>> userState = repo.observeCurrentUser();
    private final MutableLiveData<UiState<Boolean>> saveState = new MutableLiveData<>();

    public LiveData<UiState<User>> userState() {
        return userState;
    }

    public LiveData<UiState<Boolean>> saveState() {
        return saveState;
    }

    public void save(String name, String secondName, String phone) {
        String n = name == null ? "" : name.trim();
        String sn = secondName == null ? "" : secondName.trim();
        String ph = phone == null ? "" : phone.trim();

        if (n.isEmpty()) {
            saveState.setValue(UiState.error("Заполните поле с именем"));
            return;
        }
        if (sn.isEmpty()) {
            saveState.setValue(UiState.error("Заполните поле с фамилией"));
            return;
        }
        if (ph.isEmpty()) {
            saveState.setValue(UiState.error("Заполните поле с номером"));
            return;
        }

        saveState.setValue(UiState.loading());

        repo.updateProfile(n, sn, ph, new UserRepository.Callback() {
            @Override public void onSuccess() {
                saveState.postValue(UiState.success(true));
            }

            @Override public void onError(String message) {
                saveState.postValue(UiState.error(message));
            }
        });
    }

    @Override
    protected void onCleared() {
        repo.clear();
        super.onCleared();
    }
}
