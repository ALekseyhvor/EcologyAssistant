package space.hvoal.ecologyassistant.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import space.hvoal.ecologyassistant.model.ProjectLocation;

public class LocationPickerViewModel extends ViewModel {

    private final MutableLiveData<ProjectLocation> selected = new MutableLiveData<>(null);

    public LiveData<ProjectLocation> selected() {
        return selected;
    }

    public ProjectLocation getSelectedValue() {
        return selected.getValue();
    }

    public void setSelected(double lat, double lng) {
        selected.setValue(new ProjectLocation(lat, lng));
    }

    public void clear() {
        selected.setValue(null);
    }
}
