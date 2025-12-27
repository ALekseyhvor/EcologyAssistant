package space.hvoal.ecologyassistant.ui.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import space.hvoal.ecologyassistant.R;

public class ChatFragment extends Fragment {

    public ChatFragment() {
        super(R.layout.fragment_chat);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String projectId = null;
        Bundle args = getArguments();
        if (args != null) projectId = args.getString("projectId");

        TextView tv = view.findViewById(R.id.tvChatStub);
        tv.setText("Чат проекта\nprojectId = " + (projectId != null ? projectId : "null"));
    }
}
