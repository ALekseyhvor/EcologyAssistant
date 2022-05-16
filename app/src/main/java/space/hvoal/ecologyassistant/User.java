package space.hvoal.ecologyassistant;

public class User {
    public String id, name, secondname, email, number, password;

    public User() {}

    public User(String id, String name, String secondname, String email, String number, String password) {
        this.id = id;
        this.name = name;
        this.secondname = secondname;
        this.email = email;
        this.number = number;
        this.password = password;
    }
}
