package com.backend.service;

import com.backend.entity.ConversationDocument;

import java.util.List;

public interface ConversationService {
    ConversationDocument startConversation(Long userId, String userInput);
    void appendMessage(String conversationUuid, String speaker, String text);
    List<ConversationDocument> getUserConversations(Long userId);
    void deleteConversation(String id);
}
