package com.backend.service;

import com.backend.entity.ConversationDocument;
import com.backend.entity.ConversationIndex;

import java.util.List;
import java.util.Map;

/**
 * 整合会话服务接口
 * 结合了会话索引(MySQL)和会话内容(MongoDB)的功能
 */
public interface ConversationIntegratedService {
    
    /**
     * 获取会话详情（包含索引信息）
     * @param uuid 会话UUID
     * @return 会话索引
     */
    ConversationIndex getConversationIndex(String uuid);
    
    /**
     * 获取会话详情（包含完整消息内容）
     * @param uuid 会话UUID
     * @return 会话文档
     */
    ConversationDocument getConversationContent(String uuid);
    
    /**
     * 获取用户会话列表（仅索引信息）
     * @param userId 用户ID
     * @return 会话索引列表
     */
    List<ConversationIndex> listConversationsByUserId(Long userId);
    
    /**
     * 获取用户会话列表（包含完整消息内容）
     * @param userId 用户ID
     * @return 会话文档列表
     */
    List<ConversationDocument> getUserConversationContents(Long userId);
    
    /**
     * 获取客服会话列表
     * @param serviceUserId 客服用户ID
     * @return 会话索引列表
     */
    List<ConversationIndex> listConversationsByServiceUserId(Long serviceUserId);
    
    /**
     * 获取所有已转人工的会话
     * @return 会话索引列表
     */
    List<ConversationIndex> listTransferredConversations();
    
    /**
     * 获取用户活跃会话
     * @param userId 用户ID
     * @return 活跃会话
     */
    ConversationIndex getActiveByUserId(Long userId);
    
    /**
     * 创建新会话（同时创建索引和内容）
     * @param userId 用户ID
     * @param initialMessage 初始消息
     * @return 创建的会话信息，包含索引和内容
     */
    Map<String, Object> createConversation(Long userId, String initialMessage);
    
    /**
     * 追加消息到会话
     * @param uuid 会话UUID
     * @param speaker 发言者
     * @param text 消息内容
     * @return 是否成功
     */
    boolean appendMessage(String uuid, String speaker, String text);
    
    /**
     * 转接会话到客服
     * @param uuid 会话UUID
     * @param serviceUserId 客服用户ID
     * @return 是否成功
     */
    boolean transferToService(String uuid, Long serviceUserId);
    
    /**
     * 关闭会话
     * @param uuid 会话UUID
     * @return 是否成功
     */
    boolean closeConversation(String uuid);
    
    /**
     * 删除会话（同时删除索引和内容）
     * @param uuid 会话UUID
     * @return 是否成功
     */
    boolean deleteConversation(String uuid);
    
    /**
     * 获取所有会话列表（管理员使用）
     * @return 会话索引列表
     */
    List<ConversationIndex> listAllConversations();
} 