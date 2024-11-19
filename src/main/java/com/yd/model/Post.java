package com.yd.model;

import java.time.LocalDateTime;

public class Post {
    private int postId;
    private String text;
    private String writerId;
    private LocalDateTime createdAt;
    private int numOfLikes;
    private int numOfRetweets;
    private byte[] image;

    public Post(int postId, String text, byte[] image, String writerId, LocalDateTime createdAt, int numOfLikes, int numOfRetweets) {
        this.postId = postId;
        this.text = text;
        this.image = image;
        this.writerId = writerId;
        this.createdAt = createdAt;
        this.numOfLikes = numOfLikes;
        this.numOfRetweets = numOfRetweets;
    }

    public Post() {

    }

    // Getters and Setters

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getNumOfRetweets() {
        return numOfRetweets;
    }

    public void setNumOfRetweets(int numOfRetweets) {
        this.numOfRetweets = numOfRetweets;
    }

    public String getWriterId() {
        return writerId;
    }

    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getNumOfLikes() {
        return numOfLikes;
    }

    public void setNumOfLikes(int numOfLikes) {
        this.numOfLikes = numOfLikes;
    }

    @Override
    public String toString() {
        return "[" + writerId + "] " + text + " (" + numOfLikes + " likes)";
    }


}