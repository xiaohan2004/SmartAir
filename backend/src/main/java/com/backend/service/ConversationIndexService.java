package com.backend.service;

import com.backend.entity.ConversationIndex;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 会话索引服务接口
 */
public interface ConversationIndexService extends IService<ConversationIndex> {
    
    /**
     * 根据用户ID查询会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ConversationIndex> listByUserId(Long userId);
    
    /**
     * 根据UUID查询会话
     * @param uuid 会话UUID
     * @return 会话索引
     */
    ConversationIndex getByUuid(String uuid);
    
    /**
     * 创建新会话
     * @param userId 用户ID
     * @param initialMessage 初始消息
     * @return 创建的会话索引
     */
    ConversationIndex createConversation(Long userId, String initialMessage);
    
    /**
     * 更新会话最后消息
     * @param uuid 会话UUID
     * @param message 最新消息
     * @return 是否成功
     */
    boolean updateLastMessage(String uuid, String message);
    
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
     * 查询客服正在处理的会话列表
     * @param serviceUserId 客服用户ID
     * @return 会话列表
     */
    List<ConversationIndex> listByServiceUserId(Long serviceUserId);
}