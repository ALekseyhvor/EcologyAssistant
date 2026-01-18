package space.hvoal.ecologyassistant.ui.tabs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.data.category.Categories;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.model.ProjectLocation;
import space.hvoal.ecologyassistant.utils.ProjectWriter;

import android.app.AlertDialog;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import space.hvoal.ecologyassistant.ui.map.LocationPickerViewModel;

public class CreateProjectFragment extends Fragment {

    private static final int REQ_LOCATION_PERMISSION = 1001;

    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    private RelativeLayout root;
    private EditText authortext, nameproject, maintext;
    private ChipGroup chipGroupCategory;

    private String author;
    private String nameP;
    private String mainP;

    private ProjectWriter projectWriter;

    private NotificationManager nm;
    private final int NOTIFICATION_ID = 1;
    private final String CHANNEL_ID = "CHANNEL_ID";

    private ValueEventListener userListener;

    private TextView tvPickedLocation;

    private space.hvoal.ecologyassistant.ui.map.LocationPickerViewModel pickerVm;
    @Nullable
    private Project pendingProject;

    public CreateProjectFragment() {
        super(R.layout.fragment_create_project);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userRef = db.getReference().child("Users");
        projectWriter = new ProjectWriter();

        nm = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        ImageView backbtn = view.findViewById(R.id.back_button);
        Button submit = view.findViewById(R.id.buttonCreateProject);
        root = view.findViewById(R.id.root_element_crproject);

        authortext = view.findViewById(R.id.editTextAuthor);
        nameproject = view.findViewById(R.id.editNameProject);
        maintext = view.findViewById(R.id.editMainTheme);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);

        for (Categories.Item item : Categories.all()) {
            Chip chip = new Chip(requireContext());
            chip.setText(item.title);
            chip.setCheckable(true);
            chip.setTag(item.id);
            chipGroupCategory.addView(chip);

            if (Categories.OTHER.equals(item.id)) {
                chip.setChecked(true);
            }
        }

        pickerVm = new ViewModelProvider(requireActivity()).get(space.hvoal.ecologyassistant.ui.map.LocationPickerViewModel.class);


        pickerVm = new ViewModelProvider(requireActivity()).get(LocationPickerViewModel.class);

        tvPickedLocation = view.findViewById(R.id.tvPickedLocation);
        View btnPickLocation = view.findViewById(R.id.btnPickLocation);

        pickerVm.selected().observe(getViewLifecycleOwner(), loc -> {
            if (tvPickedLocation == null) return;
            if (loc == null || loc.getLat() == null || loc.getLng() == null) {
                tvPickedLocation.setText("Геопозиция не выбрана");
            } else {
                tvPickedLocation.setText("Выбрано: " + loc.getLat() + ", " + loc.getLng());
            }
        });


        btnPickLocation.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putBoolean("pickerMode", true);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.mapFragment, b);

            Snackbar.make(root, "Передвинь карту и нажми «Выбрать эту точку»", Snackbar.LENGTH_LONG).show();
        });

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Object nameVal = snapshot.child("name").getValue();
                    if (nameVal != null) {
                        author = nameVal.toString();
                        authortext.setText(author);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        userRef.child(uid).addValueEventListener(userListener);

        authortext.setEnabled(false);

        backbtn.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        submit.setOnClickListener(v -> {
            if (!validate()) return;

            Project project = buildProject();
            if (project == null) return;

            showLocationChoiceAndSave(project);
        });
    }

    private void showLocationChoiceAndSave(Project project) {
        ProjectLocation picked = pickerVm.getSelectedValue();

        if (picked != null && picked.getLat() != null && picked.getLng() != null) {
            project.setLocation(new ProjectLocation(picked.getLat(), picked.getLng()));
            pickerVm.clear();
            finishSave(project);
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Геопозиция проекта")
                .setMessage("Выбери, как указать место проекта")
                .setPositiveButton("Выбрать на карте", (d, w) -> {
                    Bundle b = new Bundle();
                    b.putBoolean("pickerMode", true);

                    NavHostFragment.findNavController(this)
                            .navigate(R.id.mapFragment, b);

                    Snackbar.make(root, "Передвинь карту и нажми «Выбрать эту точку»", Snackbar.LENGTH_LONG).show();
                })
                .setNeutralButton("Пропустить", (d, w) -> {
                    project.setLocation(null);
                    finishSave(project);
                })
                .setNegativeButton("По геолокации устройства", (d, w) -> {
                    saveProjectWithLocation(project);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null && auth.getCurrentUser() != null) {
            userRef.child(auth.getCurrentUser().getUid()).removeEventListener(userListener);
        }
        userListener = null;
    }

    private boolean validate() {
        nameP = nameproject.getText().toString().trim();
        mainP = maintext.getText().toString().trim();

        if (TextUtils.isEmpty(author)) {
            Snackbar.make(root, "Не удалось определить автора", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(nameP)) {
            Snackbar.make(root, "Введите название вашего проекта", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mainP)) {
            Snackbar.make(root, "Ваше описание проекта пустое", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Nullable
    private Project buildProject() {
        Calendar calendar = Calendar.getInstance();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyyMMddHHmmss");
        String saveCurrentDate = currentDate.format(calendar.getTime());

        String projectKey = UUID.randomUUID().toString();

        int checkedId = chipGroupCategory.getCheckedChipId();
        if (checkedId == View.NO_ID) {
            Snackbar.make(root, "Выберите категорию", Snackbar.LENGTH_SHORT).show();
            return null;
        }
        Chip checkedChip = chipGroupCategory.findViewById(checkedId);
        String categoryId = (String) checkedChip.getTag();

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        Project project = new Project(projectKey, saveCurrentDate, nameP, mainP, author);
        project.setAuthorId(uid);
        project.setCategoryId(categoryId);

        project.setLikes(null);
        project.setLikesCount(0);

        return project;
    }

    private void saveProjectWithLocation(Project project) {
        space.hvoal.ecologyassistant.model.ProjectLocation picked = pickerVm.getSelectedValue();
        if (picked != null && picked.getLat() != null && picked.getLng() != null) {
            project.setLocation(new space.hvoal.ecologyassistant.model.ProjectLocation(picked.getLat(), picked.getLng()));
            pickerVm.clear();
            finishSave(project);
            return;
        }

        if (hasLocationPermission()) {
            attachLastKnownLocation(project);
            finishSave(project);
            return;
        }

        pendingProject = project;
        requestPermissions(
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQ_LOCATION_PERMISSION
        );
    }

    private boolean hasLocationPermission() {
        Context ctx = requireContext();
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void attachLastKnownLocation(Project project) {
        Location loc = getLastKnownLocationSafe();
        if (loc == null) return;
        project.setLocation(new ProjectLocation(loc.getLatitude(), loc.getLongitude()));
    }

    @Nullable
    private Location getLastKnownLocationSafe() {
        try {
            LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return null;

            Location best = null;

            // Prefer network -> GPS -> passive
            Location net = null;
            Location gps = null;
            Location pass = null;

            if (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                pass = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
            if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            best = newer(best, net);
            best = newer(best, gps);
            best = newer(best, pass);

            return best;
        } catch (SecurityException e) {
            return null;
        }
    }

    private boolean hasPermission(String p) {
        return ContextCompat.checkSelfPermission(requireContext(), p) == PackageManager.PERMISSION_GRANTED;
    }

    @Nullable
    private Location newer(@Nullable Location current, @Nullable Location candidate) {
        if (candidate == null) return current;
        if (current == null) return candidate;
        return candidate.getTime() > current.getTime() ? candidate : current;
    }

    private void finishSave(Project project) {
        projectWriter.saveProjectInformation(project);

        showNotification();
        Snackbar.make(root, "Проект создан!", Snackbar.LENGTH_SHORT).show();
        NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQ_LOCATION_PERMISSION) return;

        Project project = pendingProject;
        pendingProject = null;
        if (project == null) return;

        boolean granted = false;
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }

        if (granted) {
            attachLastKnownLocation(project);
        } else {
            Snackbar.make(root, "Локация не добавлена (нет разрешения)", Snackbar.LENGTH_SHORT).show();
        }

        finishSave(project);
    }

    private void showNotification() {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.smart_ecology_eco_nature_world_icon_)
                        .setWhen(System.currentTimeMillis())
                        .setTicker("Новое уведомление")
                        .setContentTitle("Новый проект")
                        .setContentText("Вы создали новый проект")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Вы создали новый проект"));

        createChannelIfNeeded(nm);
        nm.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createChannelIfNeeded(NotificationManager manager) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT
        );
        manager.createNotificationChannel(channel);
    }
}
