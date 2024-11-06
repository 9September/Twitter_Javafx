package com.yd.model;

import java.time.LocalDateTime;

public class Follow {
    private String followingId;
    private String followerId;
    private LocalDateTime createdAt;

    public Follow(String followingId, String followerId, LocalDateTime createdAt) {
        this.followingId = followingId;
        this.followerId = followerId;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public String getFollowingId() {
        return followingId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}