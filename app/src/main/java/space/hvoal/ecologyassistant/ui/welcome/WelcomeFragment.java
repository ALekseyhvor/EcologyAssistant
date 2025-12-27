package space.hvoal.ecologyassistant.ui.welcome;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import space.hvoal.ecologyassistant.R;

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {
        super(R.layout.fragment_welcome);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            NavController nav = NavHostFragment.findNavController(WelcomeFragment.this);

            if (user == null) {
                nav.navigate(R.id.action_welcome_to_login);
            } else {
                nav.navigate(R.id.action_welcome_to_mainStub);
            }
        }, 1500);
    }
}
