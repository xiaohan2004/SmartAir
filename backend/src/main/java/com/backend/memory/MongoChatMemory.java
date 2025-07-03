//package com.backend.memory;
//
//import com.backend.dao.mongo.ConversationRepository;
//import com.backend.entity.ConversationDocument;
//import com.backend.entity.ConversationDocument.Message;
//import com.backend.entity.ConversationDocument.Metadata;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//
//@Component
//@RequiredArgsConstructor
//public class MongoChatMemory implements ChatMemory {
//
//    private final ConversationRepository conversationRepository;
//
//    @Override
//    public void add(String conversationId, List<Message> messages) {
//        ConversationDocument conversation = conversationRepository.findByConversationUuid(conversationId)
//                .orElseGet(() -> {
//                    ConversationDocument doc = new ConversationDocument();
//                    doc.setConversationUuid(conversationId);
//                    doc.setMessages(new ArrayList<>());
//                    doc.setMetadata(new Metadata(new Date(), null));
//                    return doc;
//                });
//
//        conversation.getMessages().addAll(messages);
//
//        // 更新 session_end 时间
//        conversation.getMetadata().setSessionEnd(new Date());
//
//        conversationRepository.save(conversation);
//    }
//
//    @Override
//    public List<Message> get(String conversationId, int lastN) {
//        Optional<ConversationDocument> optional = conversationRepository.findByConversationUuid(conversationId);
//        if (optional.isEmpty()) return Collections.emptyList();
//
//        List<Message> messages = optional.get().getMessages();
//        if (messages == null) return Collections.emptyList();
//
//        return messages.stream()
//                .skip(Math.max(0, messages.size() - lastN))
//                .toList();
//    }
//
//    @Override
//    public void clear(String conversationId) {
//        Optional<ConversationDocument> optional = conversationRepository.findByConversationUuid(conversationId);
//        optional.ifPresent(doc -> {
//            doc.setMessages(new ArrayList<>());
//            Metadata metadata = doc.getMetadata();
//            if (metadata != null) {
//                metadata.setSessionEnd(new Date());
//            }
//            conversationRepository.save(doc);
//        });
//    }
//}
