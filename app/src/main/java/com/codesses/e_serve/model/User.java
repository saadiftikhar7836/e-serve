package com.codesses.e_serve.model;

public class User {
    private String userId, full_name, email, phone_no, password, profile_image, role, fcm_token, business_name;
    private ServiceLocation location;
    private int is_available;

    public User() {
    }


    public String getFcm_token() {
        return fcm_token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public ServiceLocation getLocation() {
        return location;
    }

    public void setLocation(ServiceLocation location) {
        this.location = location;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public int getIs_available() {
        return is_available;
    }

    public void setIs_available(int is_available) {
        this.is_available = is_available;
    }

    @Override
    public String toString() {
        return full_name;
    }
}
