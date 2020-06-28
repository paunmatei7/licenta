package com.example.licenta;

import com.google.firebase.database.DatabaseReference;

public class Contacts {
    public String name, status, university, image;


    public Contacts() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Contacts(String name, String status, String university, String image) {
        this.name = name;
        this.status = status;
        this.university = university;
        this.image = image;
    }
}
