package com.example.tailwagging;

import java.io.Serializable;

public class Review implements Serializable {
    public String reviewId;
    public String userId;
    public String userName;
    public String userPhotoUrl;
    public String vetId;
    public float rating;
    public String comment;
    public long timestamp;

    public Review() {} // Required for Firebase

    public Review(String reviewId, String userId, String userName, String userPhotoUrl, String vetId, float rating, String comment, long timestamp) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.vetId = vetId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }
}