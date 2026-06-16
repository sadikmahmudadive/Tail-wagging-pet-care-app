package com.example.tailwagging;

import java.io.Serializable;

public class Vet implements Serializable {
    private String id;
    private String name;
    private String qualification;
    private float rating;
    private int reviewsCount;
    private String tag;
    private String distance;
    private String price;
    private String phone;
    private String experience;
    private String lastVisit;
    private int imageResId;
    private String imageUrl; // For remote photos
    private String businessHours;
    private String bio;
    private String recommendedFor;

    public Vet(String id, String name, String qualification, float rating, int reviewsCount, String tag, String distance, String price, String experience, String lastVisit, int imageResId) {
        this.id = id;
        this.name = name;
        this.qualification = qualification;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.tag = tag;
        this.distance = distance;
        this.price = price;
        this.experience = experience;
        this.lastVisit = lastVisit;
        this.imageResId = imageResId;
        this.businessHours = "Monday - Friday at 8.00 am - 5.00 pm";
        this.bio = "Expert veterinarian with over " + experience + " of experience. Dedicated to providing the best care for your pets with love and involvement.";
        this.recommendedFor = "All Pets";
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getQualification() { return qualification; }
    public float getRating() { return rating; }
    public int getReviewsCount() { return reviewsCount; }
    public String getTag() { return tag; }
    public String getDistance() { return distance; }
    public String getPrice() { return price; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getExperience() { return experience; }
    public String getLastVisit() { return lastVisit; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getRecommendedFor() { return recommendedFor; }
    public void setRecommendedFor(String recommendedFor) { this.recommendedFor = recommendedFor; }
}