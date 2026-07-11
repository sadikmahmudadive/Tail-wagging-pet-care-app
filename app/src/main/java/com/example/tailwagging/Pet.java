package com.example.tailwagging;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Pet implements Parcelable {
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
    private String description;
    private String age;
    private String vaccinationDetails;
    private String medicationTime;
    private List<String> feedingTimes;
    private String currentFoodName;
    private String foodType; // Dry, Wet, Mixed

    public Pet() {
        // Default constructor required for calls to DataSnapshot.getValue(Pet.class)
        this.feedingTimes = new ArrayList<>();
    }

    public Pet(String petID, String ownerID, String name, String breed, String gender, String age, String dob, String color, String sound, String height, String weight, String imageUrl, String vaccinationDetails, String medicationTime, String description) {
        this.petID = petID;
        this.ownerID = ownerID;
        this.name = name;
        this.breed = breed;
        this.gender = gender;
        this.age = age;
        this.dob = dob;
        this.color = color;
        this.sound = sound;
        this.height = height;
        this.weight = weight;
        this.imageUrl = imageUrl;
        this.vaccinationDetails = vaccinationDetails;
        this.medicationTime = medicationTime;
        this.description = description;
        this.feedingTimes = new ArrayList<>();
    }

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
    public String getDescription() { return description; }
    public String getAge() { return age; }
    public String getVaccinationDetails() { return vaccinationDetails; }
    public String getMedicationTime() { return medicationTime; }
    public List<String> getFeedingTimes() { return feedingTimes; }
    public String getCurrentFoodName() { return currentFoodName; }
    public String getFoodType() { return foodType; }

    // Setters
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
    public void setFeedingTimes(List<String> feedingTimes) { this.feedingTimes = feedingTimes; }
    public void setDescription(String description) { this.description = description; }
    public void setCurrentFoodName(String currentFoodName) { this.currentFoodName = currentFoodName; }
    public void setFoodType(String foodType) { this.foodType = foodType; }

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
        description = in.readString();
        age = in.readString();
        vaccinationDetails = in.readString();
        medicationTime = in.readString();
        feedingTimes = in.createStringArrayList();
        currentFoodName = in.readString();
        foodType = in.readString();
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
        dest.writeString(description);
        dest.writeString(age);
        dest.writeString(vaccinationDetails);
        dest.writeString(medicationTime);
        dest.writeStringList(feedingTimes);
        dest.writeString(currentFoodName);
        dest.writeString(foodType);
    }
}
