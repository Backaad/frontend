package com.Mbuntu.MbuntuMobile.api.models;

import java.util.List;

public class CreateConversationRequest {
    private String name;
    private List<Long> userIds;

    public CreateConversationRequest(String name, List<Long> userIds) {
        this.name = name;
        this.userIds = userIds;
    }
}