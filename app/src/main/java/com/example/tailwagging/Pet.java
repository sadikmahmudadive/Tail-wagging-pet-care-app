package com.example.tailwagging;

public class Pet {
    private String id;
    private String name;
    private String species;
    private String breed;
    private String photoUrl;

    public Pet() {} // Needed for Firebase

    public Pet(String id, String name, String species, String breed, String photoUrl) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.photoUrl = photoUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public String getPhotoUrl() { return photoUrl; }
}