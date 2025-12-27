package space.hvoal.ecologyassistant.ui.tabs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseArray;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import space.hvoal.ecologyassistant.R;
import space.hvoal.ecologyassistant.model.Project;
import space.hvoal.ecologyassistant.utils.ProjectWriter;
import space.hvoal.ecologyassistant.viewHolder.ProjectViewHolder;

public class DiscussionFragment extends Fragment {

    private RecyclerView recyclerView;
    private Switch switchDate;
    private Switch switchSubscribersCnt;

    private FirebaseDatabase db;
    private DatabaseReference refProject;
    private FirebaseAuth auth;
    private DatabaseReference usersref;

    private String username;
    private ProjectWriter projectWriter;

    private FirebaseRecyclerAdapter<Project, ProjectViewHolder> adapter;
    private ValueEventListener userListener;

    public DiscussionFragment() {
        super(R.layout.fragment_tab_discussion);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseDatabase.getInstance();
        refProject = db.getReference().child("Projects");
        auth = FirebaseAuth.getInstance();
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        projectWriter = new ProjectWriter();

        ImageView backBtn = view.findViewById(R.id.back_button);
        switchDate = view.findViewById(R.id.switchDate);
        switchSubscribersCnt = view.findViewById(R.id.switchSubscribersCnt);

        // В BottomNavigation "назад" не нужен — прячем, чтобы не путал
        backBtn.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        switchDate.setOnClickListener(v -> {
            if (switchDate.isChecked()) switchSubscribersCnt.setChecked(false);
            rebuildAdapter();
        });

        switchSubscribersCnt.setOnClickListener(v -> {
            if (switchSubscribersCnt.isChecked()) switchDate.setChecked(false);
            rebuildAdapter();
        });

        // Подтягиваем имя пользователя (как было в Activity)
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Object nameVal = snapshot.child("name").getValue();
                    username = nameVal != null ? nameVal.toString() : null;
                    // Чтобы joinButton корректно перерисовался, когда имя загрузилось
                    if (adapter != null) adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        usersref.child(uid).addValueEventListener(userListener);

        // Первичная загрузка
        rebuildAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Снимаем слушатель, чтобы не утекал
        if (userListener != null) {
            String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
            if (uid != null) usersref.child(uid).removeEventListener(userListener);
        }
        userListener = null;
        adapter = null;
    }

    private void rebuildAdapter() {
        Query query;
        if (switchDate != null && switchDate.isChecked()) {
            query = refProject.orderByChild("dateTime");
        } else if (switchSubscribersCnt != null && switchSubscribersCnt.isChecked()) {
            // (Пока оставляем как есть, хотя сортировка по списку subscribers некорректна — поправим позже через subscribersCount)
            query = refProject.orderByChild("subscribers");
        } else {
            query = refProject.orderByChild("nameProject");
        }

        FirebaseRecyclerOptions<Project> options = new FirebaseRecyclerOptions.Builder<Project>()
                .setSnapshotArray(new FirebaseArray<>(query, new ClassSnapshotParser<>(Project.class)))
                .build();

        // Если был старый адаптер — остановим
        if (adapter != null) adapter.stopListening();

        adapter = new FirebaseRecyclerAdapter<Project, ProjectViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull ProjectViewHolder holder, int position, @NonNull Project model) {
                holder.nameUserTextView.setText(model.getAuthor());
                holder.nameprojectTextView.setText(model.getNameProject());

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat viewFormat = new SimpleDateFormat("MMM-dd HH:mm");

                String viewDate = "";
                try {
                    Date date = dateFormat.parse(model.getDateTime());
                    if (date != null) viewDate = viewFormat.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                holder.creationdateTextView.setText(viewDate);
                holder.textprojectTextView.setText(model.getDescription());

                // Пока username не загрузился — кнопку join не даём нажимать (иначе добавишь null в subscribers)
                boolean hasUsername = username != null && !username.trim().isEmpty();
                holder.joinButton.setEnabled(hasUsername);
                holder.joinButton.setAlpha(hasUsername ? 1f : 0.5f);

                int visibility = Optional.ofNullable(model.getSubscribers())
                        .orElse(new ArrayList<>())
                        .contains(username)
                        ? View.INVISIBLE
                        : View.VISIBLE;
                holder.joinButton.setVisibility(visibility);

                holder.joinButton.setOnClickListener(v -> {
                    if (!hasUsername) return;

                    holder.joinButton.setVisibility(View.INVISIBLE);

                    if (model.getSubscribers() == null) {
                        model.setSubscribers(new ArrayList<>());
                    }
                    if (!model.getSubscribers().contains(username)) {
                        model.getSubscribers().add(username);
                    }

                    projectWriter.saveProjectInformation(model);
                    rebuildAdapter();
                    if (adapter != null) adapter.startListening();
                });

                holder.chatButton.setOnClickListener(v -> {
                    // Переход в чат через Navigation (внутри nav_main.xml должен быть chatFragment)
                    Bundle args = new Bundle();
                    args.putString("projectId", model.getId());
                    androidx.navigation.fragment.NavHostFragment
                            .findNavController(DiscussionFragment.this)
                            .navigate(R.id.chatFragment, args);
                });

                holder.subscribersTextView.setText(String.valueOf(
                        Optional.ofNullable(model.getSubscribers()).orElse(new ArrayList<>()).size()
                ));

                holder.commTextView.setText(String.valueOf(
                        Optional.ofNullable(model.getComments()).orElse(new ArrayList<>()).size()
                ));
            }

            @NonNull
            @Override
            public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.project_item, parent, false);
                return new ProjectViewHolder(v, false);
            }
        };

        recyclerView.setAdapter(adapter);

        // если фрагмент уже стартанул — включаем прослушку сразу
        if (getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
            adapter.startListening();
        }
    }
}
