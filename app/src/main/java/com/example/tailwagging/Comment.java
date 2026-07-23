package com.example.tailwagging;

import java.io.Serializable;

public class Comment implements Serializable {
    public String commentId;
    public String userId;
    public String userName;
    public String userPhoto;
    public String commentText;
    public long timestamp;

    public Comment() {
    }

    public Comment(String commentId, String userId, String userName, String userPhoto, String commentText) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.commentText = commentText;
        this.timestamp = System.currentTimeMillis();
    }
}
