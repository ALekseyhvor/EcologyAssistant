package space.hvoal.ecologyassistant.model;


public class Project {
    private String id, date, time, nameProject, description, author;
    private int countcomm, countlike;

    public Project() {}

    public Project(String id, String date, String time, String nameProject, String description, String author, int countcomm, int countlike) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.nameProject = nameProject;
        this.description = description;
        this.author = author;
        this.countcomm = countcomm;
        this.countlike = countlike;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public int getCountcomm() {
        return countcomm;
    }

    public void setCountcomm(int countcomm) {
        this.countcomm = countcomm;
    }

    public int getCountlike() {
        return countlike;
    }

    public void setCountlike(int countlike) {
        this.countlike = countlike;
    }
}
