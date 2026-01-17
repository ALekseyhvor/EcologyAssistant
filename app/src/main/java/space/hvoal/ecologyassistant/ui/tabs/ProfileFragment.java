package space.hvoal.ecologyassistant.ui.tabs;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.User;
import space.hvoal.ecologyassistant.ui.common.UiState;
import space.hvoal.ecologyassistant.ui.profile.ProfileViewModel;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_tab_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvFullName = view.findViewById(R.id.tvProfileFullName);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvPhone = view.findViewById(R.id.tvProfilePhone);

        ProfileViewModel vm = new ViewModelProvider(this).get(ProfileViewModel.class);

        vm.userState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.status == UiState.Status.LOADING) {
                tvFullName.setText("Загрузка...");
                tvEmail.setText("—");
                tvPhone.setText("—");
                return;
            }

            if (state.status == UiState.Status.ERROR) {
                tvFullName.setText("Ошибка");
                tvEmail.setText("—");
                tvPhone.setText("—");
                Snackbar.make(view, "Ошибка профиля: " + state.error, Snackbar.LENGTH_LONG).show();
                return;
            }

            User u = state.data;
            if (u == null) return;

            tvFullName.setText(safe(u.getName()) + " " + safe(u.getSecondname()));
            tvEmail.setText("Email: " + safe(u.getEmail()));
            tvPhone.setText("Телефон: " + safe(u.getPhone()));
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            vm.logout();

            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.mainFragment, true)
                    .build();

            Navigation.findNavController(requireActivity(), R.id.nav_host)
                    .navigate(R.id.loginFragment, null, opts);
        });

        view.findViewById(R.id.btnMyProjects).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_my_projects)
        );

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_edit_profile)
        );
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
