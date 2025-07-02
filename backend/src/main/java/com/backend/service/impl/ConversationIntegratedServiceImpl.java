package com.backend.service.impl;

import com.backend.dao.mongo.ConversationRepository;
import com.backend.entity.ConversationDocument;
import com.backend.entity.ConversationIndex;
import com.backend.service.ConversationIndexService;
import com.backend.service.ConversationIntegratedService;
import com.backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 整合会话服务实现类
 */
@Service
@RequiredArgsConstructor
public class ConversationIntegratedServiceImpl implements ConversationIntegratedService {

    private final ConversationIndexService conversationIndexService;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;

    @Override
    public ConversationIndex getConversationIndex(String uuid) {
        return conversationIndexService.getByUuid(uuid);
    }

    @Override
    public ConversationDocument getConversationContent(String uuid) {
        return conversationRepository.findByConversationUuid(uuid).orElse(null);
    }

    @Override
    public List<ConversationIndex> listConversationsByUserId(Long userId) {
        return conversationIndexService.listByUserId(userId);
    }

    @Override
    public List<ConversationDocument> getUserConversationContents(Long userId) {
        return conversationService.getUserConversations(userId);
    }

    @Override
    public List<ConversationIndex> listConversationsByServiceUserId(Long serviceUserId) {
        return conversationIndexService.listTransferredByServiceUserId(serviceUserId);
    }

    @Override
    public List<ConversationIndex> listTransferredConversations() {
        return conversationIndexService.listTransferred();
    }

    @Override
    public ConversationIndex getActiveByUserId(Long userId) {
        return conversationIndexService.getActiveByUserId(userId);
    }

    @Override
    @Transactional
    public Map<String, Object> createConversation(Long userId, String initialMessage) {
        // 1. 创建会话索引
        ConversationIndex conversationIndex = conversationIndexService.createConversation(userId, initialMessage);
        
        // 2. 创建会话内容
        ConversationDocument conversationDocument = conversationService.startConversation(userId, initialMessage);
        
        // 3. 确保UUID一致
        if (!conversationIndex.getConversationUuid().equals(conversationDocument.getConversationUuid())) {
            // 如果UUID不一致，更新MongoDB中的UUID为MySQL中的UUID
            conversationDocument.setConversationUuid(conversationIndex.getConversationUuid());
            conversationRepository.save(conversationDocument);
        }
        
        // 4. 返回合并结果
        Map<String, Object> result = new HashMap<>();
        result.put("index", conversationIndex);
        result.put("content", conversationDocument);
        
        return result;
    }

    @Override
    @Transactional
    public boolean appendMessage(String uuid, String speaker, String text) {
        try {
            // 1. 追加消息到MongoDB
            conversationService.appendMessage(uuid, speaker, text);
            
            // 2. 更新MySQL中的最后消息
            conversationIndexService.updateLastMessage(uuid, text);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean transferToService(String uuid, Long serviceUserId) {
        return conversationIndexService.transferToService(uuid, serviceUserId);
    }

    @Override
    @Transactional
    public boolean closeConversation(String uuid) {
        return conversationIndexService.closeConversation(uuid);
    }

    @Override
    @Transactional
    public boolean deleteConversation(String uuid) {
        try {
            // 1. 获取MongoDB中的文档ID
            Optional<ConversationDocument> document = conversationRepository.findByConversationUuid(uuid);
            if (document.isPresent()) {
                // 2. 删除MongoDB中的会话内容
                conversationService.deleteConversation(document.get().getId());
            }
            
            // 3. 删除MySQL中的会话索引
            ConversationIndex conversationIndex = conversationIndexService.getByUuid(uuid);
            if (conversationIndex != null) {
                conversationIndexService.removeById(conversationIndex.getId());
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ConversationIndex> listAllConversations() {
        return conversationIndexService.list();
    }
} 