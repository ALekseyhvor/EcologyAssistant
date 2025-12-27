package space.hvoal.ecologyassistant.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

import space.hvoal.ecologyassistant.R;

public class MainStubFragment extends Fragment {

    public MainStubFragment() {
        super(R.layout.fragment_main_stub);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.mainStubFragment, true) // id из nav_root.xml
                    .build();

            NavHostFragment.findNavController(MainStubFragment.this)
                    .navigate(R.id.loginFragment, null, opts);
        });
    }
}
