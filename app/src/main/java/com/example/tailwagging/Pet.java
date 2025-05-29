package com.example.tailwagging;

import android.os.Parcel;
import android.os.Parcelable;

public class Pet implements Parcelable { // Implements Parcelable
    private String petID;
    private String name;
    private String breed;
    private String color;
    private String dob;
    private String gender;
    private String height;
    private String imageUrl;
    private String ownerID;
    private String sound;
    private String weight;
    // New fields from the "My Pets" feature
    private String age;
    private String vaccinationDetails;
    private String medicationTime;

    public Pet() {
        // Default constructor required for calls to DataSnapshot.getValue(Pet.class)
    }

    // Constructor including all fields (optional, but good for completeness)
    public Pet(String petID, String ownerID, String name, String breed, String gender, String age, String dob, String color, String sound, String height, String weight, String imageUrl, String vaccinationDetails, String medicationTime) {
        this.petID = petID;
        this.ownerID = ownerID;
        this.name = name;
        this.breed = breed;
        this.gender = gender;
        this.age = age; // New
        this.dob = dob;
        this.color = color;
        this.sound = sound;
        this.height = height;
        this.weight = weight;
        this.imageUrl = imageUrl;
        this.vaccinationDetails = vaccinationDetails; // New
        this.medicationTime = medicationTime;     // New
    }


    // Getters (existing)
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

    // Getters for new fields
    public String getAge() { return age; }
    public String getVaccinationDetails() { return vaccinationDetails; }
    public String getMedicationTime() { return medicationTime; }

    // Setters (you might need these if you ever construct/modify Pet objects manually after fetching)
    public void setPetID(String petID) { this.petID = petID; }
    public void setName(String name) { this.name = name; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setColor(String color) { this.color = color; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setHeight(String height) { this.height = height; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setOwnerID(String ownerID) { this.ownerID = ownerID; }
    public void setSound(String sound) { this.sound = sound; }
    public void setWeight(String weight) { this.weight = weight; }
    public void setAge(String age) { this.age = age; }
    public void setVaccinationDetails(String vaccinationDetails) { this.vaccinationDetails = vaccinationDetails; }
    public void setMedicationTime(String medicationTime) { this.medicationTime = medicationTime; }


    // --- Parcelable Implementation ---
    protected Pet(Parcel in) {
        petID = in.readString();
        name = in.readString();
        breed = in.readString();
        color = in.readString();
        dob = in.readString();
        gender = in.readString();
        height = in.readString();
        imageUrl = in.readString();
        ownerID = in.readString();
        sound = in.readString();
        weight = in.readString();
        age = in.readString(); // New
        vaccinationDetails = in.readString(); // New
        medicationTime = in.readString(); // New
    }

    public static final Creator<Pet> CREATOR = new Creator<Pet>() {
        @Override
        public Pet createFromParcel(Parcel in) {
            return new Pet(in);
        }

        @Override
        public Pet[] newArray(int size) {
            return new Pet[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(petID);
        dest.writeString(name);
        dest.writeString(breed);
        dest.writeString(color);
        dest.writeString(dob);
        dest.writeString(gender);
        dest.writeString(height);
        dest.writeString(imageUrl);
        dest.writeString(ownerID);
        dest.writeString(sound);
        dest.writeString(weight);
        dest.writeString(age); // New
        dest.writeString(vaccinationDetails); // New
        dest.writeString(medicationTime); // New
    }
}