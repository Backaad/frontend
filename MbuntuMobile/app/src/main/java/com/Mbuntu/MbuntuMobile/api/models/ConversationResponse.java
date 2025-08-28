package com.Mbuntu.MbuntuMobile.api.models;

import java.time.LocalDateTime;
import java.util.Set;

public class ConversationResponse {
    private Long id;
    private String name;
    private Set<Long> participantIds;
    // Pour les dates, Gson a besoin d'aide. On simplifie pour l'instant.
    private Set<UserProfileResponse> participants; // NOUVEAU
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Set<Long> getParticipantIds() { return participantIds; }
    public Set<UserProfileResponse> getParticipants() {
        return participants;
    }
}