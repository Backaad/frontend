package com.Mbuntu.MbuntuMobile.api.models;

import java.time.LocalDateTime;

public class MessageResponse {
    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private Long senderId;
    private String senderUsername;
    private Long conversationId;

    // Constructeur pour notre "Optimistic UI Update"
    public MessageResponse(String content, Long conversationId, Long senderId, String senderUsername, LocalDateTime timestamp) {
        this.id = -1L; // On met un ID temporaire négatif pour les messages locaux
        this.content = content;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.timestamp = timestamp;
    }

    // Getters
    public Long getId() { return id; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Long getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public Long getConversationId() { return conversationId; }
}