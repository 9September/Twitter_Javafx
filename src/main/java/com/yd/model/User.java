package com.yd.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private String id;
    private String password;
    private String email;
    private LocalDateTime createdAt;
    private LocalDate birthday;
    private String phoneNumber;
    private byte[] profileImage;

    // 생성자
    public User(String id, String password, String email, LocalDate birthday, String phoneNumber) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.birthday = birthday;
        this.phoneNumber = phoneNumber;
    }

    // 전체 생성자
    public User(String id, String password, String email, LocalDateTime createdAt, LocalDate birthday, String phoneNumber, byte[] profileImage) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.birthday = birthday;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
    }


    // Getters and Setters

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

}
