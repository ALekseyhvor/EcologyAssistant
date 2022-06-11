package space.hvoal.ecologyassistant.db;


import java.util.Objects;

public class Project {
    public String author;
    public String idproject;
    public String nameproject, textproject, creationdate;
    public Long countcommm, countlike;

    public Project() {}

    public Project(String author, String idproject, String nameproject, String textproject, String creationdate, Long countcommm, Long countlike) {
        this.author = author;
        this.idproject = idproject;
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

    public String getIdproject() {
        return idproject;
    }

    public void setIdproject(String idproject) {
        this.idproject = idproject;
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
        return author.equals(project.author) && idproject.equals(project.idproject) && nameproject.equals(project.nameproject) && textproject.equals(project.textproject) && creationdate.equals(project.creationdate) && Objects.equals(countcommm, project.countcommm) && Objects.equals(countlike, project.countlike);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, idproject, nameproject, textproject, creationdate, countcommm, countlike);
    }
}
