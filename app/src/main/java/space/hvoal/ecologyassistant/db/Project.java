package space.hvoal.ecologyassistant.db;


import java.util.Objects;

public class Project {
    private String author;
    private Long id;
    private String nameproject, textproject, creationdate;
    private Long countcommm, countlike;

    public Project(String author, Long id, String nameproject, String textproject, String creationdate, Long countcommm, Long countlike) {
        this.author = author;
        this.id = id;
        this.nameproject = nameproject;
        this.textproject = textproject;
        this.creationdate = creationdate;
        this.countcommm = countcommm;
        this.countlike = countlike;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameproject() {
        return nameproject;
    }

    public void setNameproject(String nameproject) {
        this.nameproject = nameproject;
    }

    public String getTextproject() {
        return textproject;
    }

    public void setTextproject(String textproject) {
        this.textproject = textproject;
    }

    public String getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(String creationdate) {
        this.creationdate = creationdate;
    }

    public Long getCountcommm() {
        return countcommm;
    }

    public void setCountcommm(Long countcommm) {
        this.countcommm = countcommm;
    }

    public Long getCountlike() {
        return countlike;
    }

    public void setCountlike(Long countlike) {
        this.countlike = countlike;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return author.equals(project.author) && id.equals(project.id) && nameproject.equals(project.nameproject) && textproject.equals(project.textproject) && creationdate.equals(project.creationdate) && countcommm.equals(project.countcommm) && countlike.equals(project.countlike);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, id, nameproject, textproject, creationdate, countcommm, countlike);
    }
}
