package space.hvoal.ecologyassistant.model;


import java.util.List;

public class Project {
    private String id, dateTime, nameProject, description, author, categoryId;
    private List<String> subscribers;
    private List<Comment> comments;

    public Project() {
    }

    public Project(String id, String datetime, String nameProject, String description, String author) {
        this.id = id;
        this.dateTime = datetime;
        this.nameProject = nameProject;
        this.description = description;
        this.author = author;
    }

    public Project(String id, String datetime, String nameProject, String description, String author, List<String> subscribers, List<Comment> comments) {
        this.id = id;
        this.dateTime = datetime;
        this.nameProject = nameProject;
        this.description = description;
        this.author = author;
        this.subscribers = subscribers;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getNameProject() {
        return nameProject;
    }

    public void setNameProject(String nameProject) {
        this.nameProject = nameProject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<String> subscribers) {
        this.subscribers = subscribers;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
