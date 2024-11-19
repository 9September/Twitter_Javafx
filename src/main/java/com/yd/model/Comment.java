package com.yd.model;

import java.time.LocalDateTime;

public class Comment {
    private int commentId;
    private String text;
    private String writerId;
    private int postId;
    private int numOfLikes;
    private LocalDateTime createdAt;
    private Post post;

    public Comment(int commentId, String text, String writerId, int postId, int numOfLikes, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.text = text;
        this.writerId = writerId;
        this.postId = postId;
        this.numOfLikes = numOfLikes;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public int getCommentId() {
        return commentId;
    }

    public String getText() {
        return text;
    }

    public String getWriterId() {
        return writerId;
    }

    public int getPostId() {
        return postId;
    }

    public int getNumOfLikes() {
        return numOfLikes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setNumOfLikes(int numOfLikes) {
        this.numOfLikes = numOfLikes;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}