package com.backend.service.impl;

import com.backend.dao.mongo.ConversationRepository;
import com.backend.entity.ConversationDocument;
import com.backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationDao;

    @Override
    public ConversationDocument startConversation(Long userId, String userInput) {
        String uuid = UUID.randomUUID().toString();
        ConversationDocument.Message message = new ConversationDocument.Message("assistant", userInput, new Date());

        ConversationDocument conversation = new ConversationDocument();
        conversation.setUserId(userId);
        conversation.setConversationUuid(uuid);
        conversation.setMessages(new ArrayList<>(List.of(message)));
        conversation.setMetadata(new ConversationDocument.Metadata(new Date(), null));

        return conversationDao.save(conversation);
    }

    @Override
    public void appendMessage(String conversationUuid, String speaker, String text) {
        ConversationDocument doc = conversationDao.findByConversationUuid(conversationUuid)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        doc.getMessages().add(new ConversationDocument.Message(speaker, text, new Date()));
        doc.getMetadata().setSessionEnd(new Date());
        conversationDao.save(doc);
    }

    @Override
    public List<ConversationDocument> getUserConversations(Long userId) {
        return conversationDao.findByUserId(userId);
    }

    @Override
    public void deleteConversation(String id) {
        conversationDao.deleteById(id);
    }
}
