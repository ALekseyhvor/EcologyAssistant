package space.hvoal.ecologyassistant.model;

public class Comment {
    private String author, comment;

    public Comment() {
    }

    public Comment(String author, String comment) {
        this.author = author;
        this.comment = comment;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
