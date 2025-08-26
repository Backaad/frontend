package com.Mbuntu.MbuntuMobile.api.models;

/**
 * Data Transfer Object (DTO) pour envoyer un nouveau message.
 * Il contient les informations nécessaires que le backend attend.
 */
public class SendMessageRequest {

    // Note: les noms des variables doivent correspondre aux clés attendues par le backend.
    private Long conversationId;
    private String content;

    public SendMessageRequest(Long conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
    }

    // Getters (sont utilisés par Gson/Retrofit pour la sérialisation en JSON)
    public Long getConversationId() {
        return conversationId;
    }

    public String getContent() {
        return content;
    }
}