package space.hvoal.ecologyassistant.ui.map;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import ru.dgis.sdk.ScreenDistance;
import ru.dgis.sdk.ScreenPoint;
import ru.dgis.sdk.map.Map;
import ru.dgis.sdk.map.MapObject;
import ru.dgis.sdk.map.MapObjectManager;
import ru.dgis.sdk.map.MapView;
import ru.dgis.sdk.map.Marker;
import ru.dgis.sdk.map.MarkerOptions;
import ru.dgis.sdk.map.RenderedObjectInfo;
import ru.dgis.sdk.map.TouchEventsObserver;
import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.app.EcologyAssistantApp;
import space.hvoal.ecologyassistant.data.category.Categories;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.model.ProjectLocation;
import space.hvoal.ecologyassistant.ui.common.UiState;

public class DgisMapFragment extends Fragment {

    private boolean pickerMode = false;

    private MapView mapView;
    private Map dgisMap;
    private MapObjectManager objectManager;

    private MapViewModel vm;

    private LocationPickerViewModel pickerVm;
    private Marker pickedMarker;

    private TouchEventsObserver touchObserver;

    private List<Project> lastProjects = new ArrayList<>();

    public DgisMapFragment() {
        super(R.layout.fragment_tab_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        // Если есть pin поверх карты — форвардим касания в карту
        if (pin != null) {
            pin.setOnTouchListener((v, event) -> {
                if (mapView != null) mapView.dispatchTouchEvent(event);
                return true;
            });
        }

        pickerVm = new ViewModelProvider(requireActivity()).get(LocationPickerViewModel.class);

        if (btnPick != null) {
            btnPick.setOnClickListener(v -> {
                if (dgisMap == null || objectManager == null) return;

                double[] center = DgisInterop.cameraCenter(dgisMap);
                double lat = center[0];
                double lng = center[1];

                pickerVm.setSelected(lat, lng);

                if (pickedMarker != null) {
                    objectManager.removeObject(pickedMarker);
                    pickedMarker = null;
                }

                MarkerOptions opts = DgisInterop.markerOptions(
                        EcologyAssistantApp.SDK_CONTEXT,
                        DgisInterop.point(lat, lng),
                        "picked"
                );

                pickedMarker = new Marker(opts);
                try { pickedMarker.setUserData("picked"); } catch (Throwable ignored) {}
                objectManager.addObject(pickedMarker);

                NavHostFragment.findNavController(this).popBackStack();
            });
        }

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
                objectManager = DgisInterop.createObjectManager(map);

                // Один механизм: tap -> getRenderedObjects -> если есть projectId -> открыть детали
                touchObserver = new TouchEventsObserver() {
                    @Override
                    public void onTap(@NonNull ScreenPoint point) {
                        if (dgisMap == null) return;

                        float radiusPx = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                24f,
                                requireContext().getResources().getDisplayMetrics()
                        );

                        dgisMap.getRenderedObjects(point, new ScreenDistance(radiusPx))
                                .onResult(new Function1<List<RenderedObjectInfo>, Unit>() {
                                    @Override
                                    public Unit invoke(List<RenderedObjectInfo> objects) {
                                        if (!isAdded()) return Unit.INSTANCE;
                                        if (objects == null || objects.isEmpty()) return Unit.INSTANCE;

                                        String projectId = extractProjectId(objects);
                                        if (projectId != null && !"picked".equals(projectId)) {
                                            openProjectDetails(projectId);
                                        }

                                        return Unit.INSTANCE;
                                    }
                                });
                    }

                    @Override public void onLongTouch(@NonNull ScreenPoint point) {}
                    @Override public void onDragBegin(@NonNull ru.dgis.sdk.map.DragBeginData data) {}
                    @Override public void onDragMove(@NonNull ScreenPoint point) {}
                    @Override public void onDragEnd() {}
                };

                try {
                    mapView.setTouchEventsObserver(touchObserver);
                } catch (Throwable ignored) {}

                renderIfReady();
                return Unit.INSTANCE;
            }
        });
    }

    /**
     * Достаём projectId из объектов под тапом.
     * Приоритет: Marker с userData String -> иначе любой с userData String.
     */
    @Nullable
    private String extractProjectId(@NonNull List<RenderedObjectInfo> objects) {
        // 1) приоритет маркера
        for (RenderedObjectInfo info : objects) {
            if (info == null) continue;
            try {
                MapObject mo = info.getItem().getItem();
                if (mo == null) continue;

                Object ud = mo.getUserData();
                if (ud instanceof String && (mo instanceof Marker)) {
                    return (String) ud;
                }
            } catch (Throwable ignored) {}
        }

        // 2) любой объект
        for (RenderedObjectInfo info : objects) {
            if (info == null) continue;
            try {
                MapObject mo = info.getItem().getItem();
                if (mo == null) continue;

                Object ud = mo.getUserData();
                if (ud instanceof String) {
                    return (String) ud;
                }
            } catch (Throwable ignored) {}
        }

        return null;
    }

    private void openProjectDetails(String projectId) {
        Bundle b = new Bundle();
        b.putString("projectId", projectId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_map_to_details, b);
    }

    private void renderIfReady() {
        if (dgisMap == null || objectManager == null) return;

        objectManager.removeAll();

        for (Project p : lastProjects) {
            if (p == null) continue;

            ProjectLocation loc = p.getLocation();
            if (loc == null || loc.getLat() == null || loc.getLng() == null) continue;

            MarkerOptions options = DgisInterop.markerOptions(
                    EcologyAssistantApp.SDK_CONTEXT,
                    DgisInterop.point(loc.getLat(), loc.getLng()),
                    p.getId()
            );

            Marker m = new Marker(options);
            // страховка: если markerOptions не проставляет userData, проставим здесь
            try { m.setUserData(p.getId()); } catch (Throwable ignored) {}
            objectManager.addObject(m);
        }

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
        try {
            if (mapView != null) {
                try { mapView.setTouchEventsObserver(null); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        touchObserver = null;

        if (objectManager != null) objectManager.removeAll();
        objectManager = null;
        dgisMap = null;
        mapView = null;

        super.onDestroyView();
    }

    // Остались методы (если вдруг понадобятся позже)
    private Project findProject(String projectId) {
        if (projectId == null) return null;
        for (Project p : lastProjects) {
            if (p != null && projectId.equals(p.getId())) return p;
        }
        return null;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String categoryTitle(String categoryId) {
        for (Categories.Item it : Categories.all()) {
            if (it.id.equals(categoryId)) return it.title;
        }
        return "Другое";
    }
}
