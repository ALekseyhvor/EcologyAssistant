package space.hvoal.ecologyassistant.ui.map;

import android.os.Bundle;
import android.util.TypedValue;
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

    private static final String UD_PICKED  = "picked";
    private static final String UD_PROJECT = "project:";
    private static final String UD_PARK    = "park:";

    private boolean pickerMode = false;

    private MapView mapView;
    private Map dgisMap;
    private MapObjectManager objectManager;

    private MapViewModel vm;

    private LocationPickerViewModel pickerVm;
    private Marker pickedMarker;

    private TouchEventsObserver touchObserver;

    private List<Project> lastProjects = new ArrayList<>();

    // -------------------- Parks (Nizhny Novgorod) --------------------

    private static class Park {
        final String id;
        final String title;
        final String fullDesc;
        final double lat;
        final double lng;

        Park(String id, String title, String fullDesc, double lat, double lng) {
            this.id = id;
            this.title = title;
            this.fullDesc = fullDesc;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private final List<Park> parks = new ArrayList<Park>() {{
        add(new Park(
                "switzerland",
                "Парк «Швейцария»",
                "Парк «Швейцария» — один из самых больших парков Нижнего Новгорода. "
                        + "Подходит для длинных прогулок: много зелени, аллеи, видовые точки, "
                        + "места для спокойного отдыха и активности.",
                56.274489, 43.973351
        ));
        add(new Park(
                "kulibin",
                "Парк им. Кулибина",
                "Парк им. Кулибина — спокойный городской парк в центре: аллеи, лавочки, "
                        + "удобно зайти на короткую прогулку. Исторически парк был создан в 1940 году "
                        + "на территории бывшего кладбища.",
                56.315191, 44.008250
        ));
        add(new Park(
                "alex_garden",
                "Александровский сад",
                "Александровский сад — один из символов исторического центра. "
                        + "Считается первым общественным парком Нижнего Новгорода (основание — 1835). "
                        + "Находится на склонах Волги, рядом с набережными — хорошее место для видов и прогулки.",
                56.329961, 44.019039
        ));
    }};

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
                        UD_PICKED
                );

                pickedMarker = new Marker(opts);
                try { pickedMarker.setUserData(UD_PICKED); } catch (Throwable ignored) {}
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

                                        String ud = extractUserData(objects);
                                        if (ud == null || UD_PICKED.equals(ud)) return Unit.INSTANCE;

                                        // Парк -> показываем AlertDialog
                                        if (ud.startsWith(UD_PARK)) {
                                            String parkId = ud.substring(UD_PARK.length());
                                            Park park = findParkById(parkId);
                                            if (park != null) showParkDialog(park);
                                            return Unit.INSTANCE;
                                        }

                                        // Проект -> переходим в детали проекта
                                        if (ud.startsWith(UD_PROJECT)) {
                                            String projectId = ud.substring(UD_PROJECT.length());
                                            if (!projectId.isEmpty()) openProjectDetails(projectId);
                                            return Unit.INSTANCE;
                                        }

                                        // fallback на случай старых маркеров без префикса
                                        openProjectDetails(ud);
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
     * Достаём userData (String) из объектов под тапом.
     * Приоритет: Marker с String userData -> иначе любой объект с String userData.
     */
    @Nullable
    private String extractUserData(@NonNull List<RenderedObjectInfo> objects) {
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

    private void openProjectDetails(@NonNull String projectId) {
        Bundle b = new Bundle();
        b.putString("projectId", projectId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_map_to_details, b);
    }

    private void renderIfReady() {
        if (dgisMap == null || objectManager == null) return;

        objectManager.removeAll();

        // ---- Projects ----
        for (Project p : lastProjects) {
            if (p == null) continue;

            ProjectLocation loc = p.getLocation();
            if (loc == null || loc.getLat() == null || loc.getLng() == null) continue;

            String ud = UD_PROJECT + p.getId();

            MarkerOptions options = DgisInterop.markerOptionsProject(
                    EcologyAssistantApp.SDK_CONTEXT,
                    DgisInterop.point(loc.getLat(), loc.getLng()),
                    ud
            );

            Marker m = new Marker(options);
            try { m.setUserData(ud); } catch (Throwable ignored) {}
            objectManager.addObject(m);
        }

        // ---- Parks ----
        for (Park p : parks) {
            String ud = UD_PARK + p.id;

            MarkerOptions options = DgisInterop.markerOptionsPark(
                    EcologyAssistantApp.SDK_CONTEXT,
                    DgisInterop.point(p.lat, p.lng),
                    ud
            );

            Marker m = new Marker(options);
            try { m.setUserData(ud); } catch (Throwable ignored) {}
            objectManager.addObject(m);
        }

        // Камера — на первый проект (если есть), иначе на первый парк
        boolean moved = false;

        for (Project p : lastProjects) {
            ProjectLocation loc = p.getLocation();
            if (loc != null && loc.getLat() != null && loc.getLng() != null) {
                DgisInterop.moveCamera(dgisMap, loc.getLat(), loc.getLng(), 15f);
                moved = true;
                break;
            }
        }

        if (!moved && !parks.isEmpty()) {
            Park p = parks.get(0);
            DgisInterop.moveCamera(dgisMap, p.lat, p.lng, 12f);
        }
    }

    // -------------------- Park helpers --------------------

    @Nullable
    private Park findParkById(String id) {
        if (id == null) return null;
        for (Park p : parks) if (id.equals(p.id)) return p;
        return null;
    }

    private void showParkDialog(@NonNull Park p) {
        if (!isAdded()) return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(p.title)
                .setMessage(p.fullDesc)
                .setPositiveButton("Ок", null)
                .show();
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

    // -------------------- Unused helpers (can be removed later) --------------------

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
