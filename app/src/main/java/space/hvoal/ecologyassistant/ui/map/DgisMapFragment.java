package space.hvoal.ecologyassistant.ui.map;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.dgis.sdk.map.MapView;
import space.hvoal.ecologyassistant.R;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import ru.dgis.sdk.map.Map;


public class DgisMapFragment extends Fragment {

    private MapView mapView;

    public DgisMapFragment() {
        super(R.layout.fragment_tab_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.dgisMapView);

        getViewLifecycleOwner().getLifecycle().addObserver(mapView);

        mapView.getMapAsync(new Function1<Map, Unit>() {
            @Override
            public Unit invoke(Map map) {
                return Unit.INSTANCE;
            }
        });

    }
}
