package space.hvoal.ecologyassistant.model;

import java.util.List;
import java.util.Map;

public class Project {

    private String id;
    private String dateTime;
    private String nameProject;
    private String description;

    private String author;
    private String authorId;

    private ProjectLocation location;

    private String categoryId;

    private Map<String, Boolean> likes;
    private Integer likesCount;

    private List<Comment> comments;

    public Project() { }

    public Project(String id, String datetime, String nameProject, String description, String author) {
        this.id = id;
        this.dateTime = datetime;
        this.nameProject = nameProject;
        this.description = description;
        this.author = author;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getNameProject() { return nameProject; }
    public void setNameProject(String nameProject) { this.nameProject = nameProject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public ProjectLocation getLocation() { return location; }
    public void setLocation(ProjectLocation location) { this.location = location; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }

    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}
