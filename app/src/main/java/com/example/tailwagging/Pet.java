package com.example.tailwagging;

public class Pet {
    private String petID;
    private String name;
    private String breed;
    private String color;
    private String dob;
    private String gender;
    private String height;
    private String imageUrl; // <-- matches Firebase field!
    private String ownerID;
    private String sound;
    private String weight;
    // Add other fields if your database contains them

    public Pet() {} // Needed for Firebase

    // Getters
    public String getPetID() { return petID; }
    public String getName() { return name; }
    public String getBreed() { return breed; }
    public String getColor() { return color; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getHeight() { return height; }
    public String getImageUrl() { return imageUrl; }
    public String getOwnerID() { return ownerID; }
    public String getSound() { return sound; }
    public String getWeight() { return weight; }
}