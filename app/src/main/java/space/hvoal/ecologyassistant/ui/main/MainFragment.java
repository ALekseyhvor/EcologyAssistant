package space.hvoal.ecologyassistant.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import space.hvoal.ecologyassistant.R;

public class MainFragment extends Fragment {

    public MainFragment() {
        super(R.layout.fragment_main);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_nav);

        NavHostFragment navHost =
                (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.main_nav_host);
        NavController navController = navHost.getNavController();

        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            boolean show = (id == R.id.discussionFragment
                    || id == R.id.mapFragment
                    || id == R.id.profileFragment);
            bottomNav.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }
}
