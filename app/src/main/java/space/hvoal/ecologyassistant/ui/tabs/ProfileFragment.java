package space.hvoal.ecologyassistant.ui.tabs;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import space.hvoal.ecologyassistant.R;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private DatabaseReference projectsRef;
    private DatabaseReference likesRef;

    private String currentUid;

    private ValueEventListener userListener;
    private ValueEventListener myProjectsCountListener;
    private ValueEventListener likedProjectsCountListener;

    private Query myProjectsQuery;
    private Query likedQuery;

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvMyCount;
    private TextView tvLikedCount;

    public ProfileFragment() {
        super(R.layout.fragment_tab_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        projectsRef = FirebaseDatabase.getInstance().getReference().child("Projects");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvMyCount = view.findViewById(R.id.tvMyProjectsCount);
        tvLikedCount = view.findViewById(R.id.tvLikedProjectsCount);

        Button btnMy = view.findViewById(R.id.btnMyProjects);
        Button btnLiked = view.findViewById(R.id.btnLikedProjects);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        FirebaseUser fu = auth.getCurrentUser();
        currentUid = fu != null ? fu.getUid() : null;

        tvName.setText("Профиль");
        tvEmail.setText(fu != null && fu.getEmail() != null ? fu.getEmail() : "");
        tvMyCount.setText("0");
        tvLikedCount.setText("0");

        btnMy.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_my_projects)
        );

        btnLiked.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_liked_projects)
        );

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.mainFragment, true)
                    .build();

            Navigation.findNavController(requireActivity(), R.id.nav_host)
                    .navigate(R.id.loginFragment, null, opts);
        });

        if (currentUid == null || currentUid.trim().isEmpty()) {
            btnMy.setEnabled(false);
            btnLiked.setEnabled(false);
            btnMy.setAlpha(0.5f);
            btnLiked.setAlpha(0.5f);
            return;
        }

        userListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object nameVal = snapshot.child("name").getValue();
                if (nameVal != null) tvName.setText(nameVal.toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        usersRef.child(currentUid).addValueEventListener(userListener);

        myProjectsQuery = projectsRef.orderByChild("authorId").equalTo(currentUid);
        myProjectsCountListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvMyCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        myProjectsQuery.addValueEventListener(myProjectsCountListener);

        likedQuery = likesRef.orderByChild(currentUid).equalTo(true);
        likedProjectsCountListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvLikedCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        likedQuery.addValueEventListener(likedProjectsCountListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (currentUid != null && usersRef != null && userListener != null) {
            usersRef.child(currentUid).removeEventListener(userListener);
        }

        if (myProjectsQuery != null && myProjectsCountListener != null) {
            myProjectsQuery.removeEventListener(myProjectsCountListener);
        }

        if (likedQuery != null && likedProjectsCountListener != null) {
            likedQuery.removeEventListener(likedProjectsCountListener);
        }

        userListener = null;
        myProjectsCountListener = null;
        likedProjectsCountListener = null;

        myProjectsQuery = null;
        likedQuery = null;
        currentUid = null;
    }
}
