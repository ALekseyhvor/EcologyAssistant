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
import space.hvoal.ecologyassistant.ui.profile.MyProjectsAdapter;
import space.hvoal.ecologyassistant.ui.profile.MyProjectsViewModel;

public class MyProjectsFragment extends Fragment {

    public MyProjectsFragment() {
        super(R.layout.fragment_my_projects);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView backbtn = view.findViewById(R.id.back_button);
        Switch switchDate = view.findViewById(R.id.switchDate);
        Switch switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        RecyclerView recyclerMyView = view.findViewById(R.id.recycleMyView);
        recyclerMyView.setLayoutManager(new LinearLayoutManager(requireContext()));

        backbtn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        MyProjectsAdapter adapter = new MyProjectsAdapter(project -> {
            Bundle b = new Bundle();
            b.putString("projectId", project.getId());
            NavHostFragment.findNavController(MyProjectsFragment.this)
                    .navigate(R.id.projectDetailsFragment, b);
        });
        recyclerMyView.setAdapter(adapter);

        MyProjectsViewModel vm = new ViewModelProvider(this).get(MyProjectsViewModel.class);

        switchDate.setChecked(false);
        switchSubscribersCnt.setChecked(false);
        vm.setSortMode(MyProjectsViewModel.SortMode.DEFAULT);

        switchDate.setOnClickListener(v -> {
            if (switchDate.isChecked()) {
                switchSubscribersCnt.setChecked(false);
                vm.setSortMode(MyProjectsViewModel.SortMode.DATE_DESC);
            } else {
                vm.setSortMode(MyProjectsViewModel.SortMode.DEFAULT);
            }
        });

        switchSubscribersCnt.setOnClickListener(v -> {
            if (switchSubscribersCnt.isChecked()) {
                switchDate.setChecked(false);
                vm.setSortMode(MyProjectsViewModel.SortMode.SUBSCRIBERS_DESC);
            } else {
                vm.setSortMode(MyProjectsViewModel.SortMode.DEFAULT);
            }
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
