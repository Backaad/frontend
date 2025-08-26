package com.Mbuntu.MbuntuMobile.api.models;

import retrofit2.http.Url;

public class UpdateProfileRequest {

    private String username;
    private String Bio;
    private Url  ProfilePictureUrl;


    public void setBio(String bio) {
        Bio = bio;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfilePictureUrl(Url profileUrl) {
        ProfilePictureUrl= profileUrl;
    }

    public String getUsername() {
        return username;
    }


    public Url getProfilePictureUrl() {
        return ProfilePictureUrl;
    }

    public String getBio() {
        return this.Bio;
    }
}
