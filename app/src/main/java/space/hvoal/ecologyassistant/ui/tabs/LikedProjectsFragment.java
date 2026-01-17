package space.hvoal.ecologyassistant.ui.tabs;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.ui.common.UiState;
import space.hvoal.ecologyassistant.ui.profile.LikedProjectsViewModel;
import space.hvoal.ecologyassistant.ui.profile.MyProjectsAdapter;

public class LikedProjectsFragment extends Fragment {

    public LikedProjectsFragment() {
        super(R.layout.fragment_liked_projects);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView backbtn = view.findViewById(R.id.back_button);
        Switch switchDate = view.findViewById(R.id.switchDate);
        Switch switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        RecyclerView rv = view.findViewById(R.id.recycleLikedView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        backbtn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        MyProjectsAdapter adapter = new MyProjectsAdapter(project -> {
            Bundle b = new Bundle();
            b.putString("projectId", project.getId());
            NavHostFragment.findNavController(LikedProjectsFragment.this)
                    .navigate(R.id.projectDetailsFragment, b);
        });

        rv.setAdapter(adapter);

        LikedProjectsViewModel vm = new ViewModelProvider(this).get(LikedProjectsViewModel.class);

        switchDate.setChecked(false);
        switchSubscribersCnt.setChecked(false);
        vm.setSortMode(LikedProjectsViewModel.SortMode.DEFAULT);

        switchDate.setOnClickListener(v -> {
            if (switchDate.isChecked()) switchSubscribersCnt.setChecked(false);
            vm.setSortMode(switchDate.isChecked()
                    ? LikedProjectsViewModel.SortMode.DATE_DESC
                    : LikedProjectsViewModel.SortMode.DEFAULT);
        });

        switchSubscribersCnt.setOnClickListener(v -> {
            if (switchSubscribersCnt.isChecked()) switchDate.setChecked(false);
            vm.setSortMode(switchSubscribersCnt.isChecked()
                    ? LikedProjectsViewModel.SortMode.SUBSCRIBERS_DESC
                    : LikedProjectsViewModel.SortMode.DEFAULT);
        });

        vm.state().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.status == UiState.Status.LOADING) return;

            if (state.status == UiState.Status.ERROR) {
                Snackbar.make(view, "Ошибка: " + state.error, Snackbar.LENGTH_LONG).show();
                return;
            }

            adapter.submitList(state.data);
        });
    }
}
