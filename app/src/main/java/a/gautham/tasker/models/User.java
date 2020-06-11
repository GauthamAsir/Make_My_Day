package a.gautham.tasker.models;

import java.util.Date;

public class User {

    private String uid, email, name;
    private String registeredDate;

    public User(String uid, String email, String name, String registeredDate) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.registeredDate = registeredDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
    }
}