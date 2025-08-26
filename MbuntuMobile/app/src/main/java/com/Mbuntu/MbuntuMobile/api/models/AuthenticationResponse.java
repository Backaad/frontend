package com.Mbuntu.MbuntuMobile.api.models;

public class AuthenticationResponse {

    private String token;
    private Long userId;

    private String username;

    // Getters pour accéder aux données
    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }
}
