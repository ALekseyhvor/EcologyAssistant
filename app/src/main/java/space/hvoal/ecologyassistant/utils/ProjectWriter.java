package space.hvoal.ecologyassistant.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import space.hvoal.ecologyassistant.model.Project;

public class ProjectWriter {

    private final DatabaseReference projectRef =
            FirebaseDatabase.getInstance().getReference().child("Projects");

    public void saveProjectInformation(Project project) {
        HashMap<String, Object> m = new HashMap<>();

        m.put("id", project.getId());
        m.put("dateTime", project.getDateTime());
        m.put("nameProject", project.getNameProject());
        m.put("description", project.getDescription());

        m.put("author", project.getAuthor());
        m.put("authorId", project.getAuthorId());

        m.put("categoryId", project.getCategoryId());

        m.put("likes", project.getLikes());
        m.put("likesCount", project.getLikesCount());

        m.put("comments", project.getComments());

        projectRef.child(project.getId()).updateChildren(m);
    }
}
