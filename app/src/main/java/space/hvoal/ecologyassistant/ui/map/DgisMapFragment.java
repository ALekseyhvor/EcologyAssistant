package space.hvoal.ecologyassistant.ui.map;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import ru.dgis.sdk.map.Map;
import ru.dgis.sdk.map.MapObjectManager;
import ru.dgis.sdk.map.MapView;
import ru.dgis.sdk.map.Marker;
import ru.dgis.sdk.map.MarkerOptions;
import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.model.ProjectLocation;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class DgisMapFragment extends Fragment {

    private MapView mapView;
    private Map dgisMap;
    private MapObjectManager objectManager;

    private MapViewModel vm;

    private LocationPickerViewModel pickerVm;
    private Marker pickedMarker;

    private List<Project> lastProjects = new ArrayList<>();

    public DgisMapFragment() {
        super(R.layout.fragment_tab_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean pickerMode = false;
        Bundle args = getArguments();
        if (args != null) pickerMode = args.getBoolean("pickerMode", false);

        View btnPick = view.findViewById(R.id.btnPickPoint);
        View pin = view.findViewById(R.id.centerPin);

        if (!pickerMode) {
            if (btnPick != null) btnPick.setVisibility(View.GONE);
            if (pin != null) pin.setVisibility(View.GONE);
        } else {
            if (btnPick != null) btnPick.setVisibility(View.VISIBLE);
            if (pin != null) pin.setVisibility(View.VISIBLE);
        }

        mapView = view.findViewById(R.id.dgisMapView);
        getViewLifecycleOwner().getLifecycle().addObserver(mapView);

        pickerVm = new ViewModelProvider(requireActivity()).get(LocationPickerViewModel.class);

        btnPick.setOnClickListener(v -> {
            if (dgisMap == null || objectManager == null) return;

            double[] center = DgisInterop.cameraCenter(dgisMap);
            double lat = center[0];
            double lng = center[1];

            // сохраняем выбранную точку в VM (чтобы CreateProject её забрал)
            pickerVm.setSelected(lat, lng);
            Snackbar.make(requireView(), "Точка выбрана. Вернись назад и создай проект", Snackbar.LENGTH_LONG).show();

            // перерисуем "маркер выбора" (чтобы пользователь видел что выбрал)
            if (pickedMarker != null) {
                objectManager.removeObject(pickedMarker);
                pickedMarker = null;
            }

            MarkerOptions opts = DgisInterop.markerOptions(
                    DgisInterop.point(lat, lng),
                    "picked"
            );

            pickedMarker = new Marker(opts);
            objectManager.addObject(pickedMarker);

            androidx.navigation.fragment.NavHostFragment
                    .findNavController(this)
                    .popBackStack();
        });

        vm = new ViewModelProvider(this).get(MapViewModel.class);
        vm.state().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            if (state.status == UiState.Status.SUCCESS) {
                lastProjects = state.data != null ? state.data : new ArrayList<>();
                renderIfReady();
            }
        });

        mapView.getMapAsync(new Function1<Map, Unit>() {
            @Override
            public Unit invoke(Map map) {
                dgisMap = map;
                objectManager = DgisInterop.createObjectManager(map); // Важно: через Kotlin interop
                renderIfReady();
                return Unit.INSTANCE;
            }
        });
    }

    private void renderIfReady() {
        if (dgisMap == null || objectManager == null) return;

        objectManager.removeAll();

        for (Project p : lastProjects) {
            if (p == null) continue;

            ProjectLocation loc = p.getLocation();
            if (loc == null || loc.getLat() == null || loc.getLng() == null) continue;

            MarkerOptions options = DgisInterop.markerOptions(
                    DgisInterop.point(loc.getLat(), loc.getLng()),
                    p.getId()
            );

            objectManager.addObject(new Marker(options));
        }

        // Авто-фокус на первый проект (у тебя уже должен работать moveCamera через Kotlin)
        for (Project p : lastProjects) {
            ProjectLocation loc = p.getLocation();
            if (loc != null && loc.getLat() != null && loc.getLng() != null) {
                DgisInterop.moveCamera(dgisMap, loc.getLat(), loc.getLng(), 15f);
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (objectManager != null) objectManager.removeAll();
        objectManager = null;
        dgisMap = null;
        mapView = null;
        super.onDestroyView();
    }
}
