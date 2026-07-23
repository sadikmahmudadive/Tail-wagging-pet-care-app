package com.example.tailwagging;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FeedPost implements Serializable {
    public String postId;
    public String userId;
    public String userName;
    public String userPhoto;
    public String postType; // ADOPTION, RESCUE, MOMENT
    public String content;
    public String imageUrl;
    public long timestamp;
    public int likesCount;
    public Map<String, Boolean> likedBy; // userId -> true

    public FeedPost() {
        this.likedBy = new HashMap<>();
    }

    public FeedPost(String postId, String userId, String userName, String userPhoto, String postType, String content, String imageUrl) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.postType = postType;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
        this.likesCount = 0;
        this.likedBy = new HashMap<>();
    }
}
