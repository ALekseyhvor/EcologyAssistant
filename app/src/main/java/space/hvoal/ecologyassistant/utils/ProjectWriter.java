package space.hvoal.ecologyassistant.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import space.hvoal.ecologyassistant.model.Project;

public class ProjectWriter {

    private FirebaseDatabase db;
    private DatabaseReference projectRef;

    public ProjectWriter() {
        db = FirebaseDatabase.getInstance();
        projectRef = db.getReference().child("Projects");
    }

    public void saveProjectInformation(Project project) {
        HashMap<String, Object> projectMap = new HashMap<>();
        projectMap.put("id", project.getId());
        projectMap.put("date", project.getDate());
        projectMap.put("time", project.getTime());
        projectMap.put("nameProject", project.getNameProject());
        projectMap.put("description", project.getDescription());
        projectMap.put("author", project.getAuthor());
        projectMap.put("subscribers", project.getSubscribers());

        projectRef.child(project.getId()).updateChildren(projectMap);
    }
}
