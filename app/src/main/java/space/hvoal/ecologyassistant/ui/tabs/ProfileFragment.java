package space.hvoal.ecologyassistant.ui.tabs;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

import space.hvoal.ecologyassistant.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() { super(R.layout.fragment_tab_profile); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.mainFragment, true)
                    .build();

            Navigation.findNavController(requireActivity(), R.id.nav_host)
                    .navigate(R.id.loginFragment, null, opts);
        });

        view.findViewById(R.id.btnMyProjects).setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_my_projects)
        );

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_edit_profile)
        );


    }
}
