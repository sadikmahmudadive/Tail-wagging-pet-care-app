package com.example.tailwagging;

public class AdminUser {
    public String id;
    public String name;
    public String email;
    public String role;
    public String photoUrl;
    public boolean isVerified;

    public AdminUser() {}

    public AdminUser(String id, String name, String email, String role, String photoUrl, boolean isVerified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.photoUrl = photoUrl;
        this.isVerified = isVerified;
    }
}
