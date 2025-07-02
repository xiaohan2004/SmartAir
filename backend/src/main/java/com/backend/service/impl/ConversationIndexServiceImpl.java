package com.backend.service.impl;

import com.backend.entity.ConversationIndex;
import com.backend.entity.User;
import com.backend.dao.mapper.ConversationIndexMapper;
import com.backend.service.ConversationIndexService;
import com.backend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话索引服务实现类
 */
@Service
public class ConversationIndexServiceImpl extends ServiceImpl<ConversationIndexMapper, ConversationIndex> implements ConversationIndexService {

    @Autowired
    private UserService userService;

    @Override
    public List<ConversationIndex> listByUserId(Long userId) {
        LambdaQueryWrapper<ConversationIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationIndex::getUserId, userId)
                .orderByDesc(ConversationIndex::getUpdatedAt);
        List<ConversationIndex> conversations = list(queryWrapper);

        // 填充用户信息
        return conversations.stream().map(this::fillUserInfo).collect(Collectors.toList());
    }

    @Override
    public ConversationIndex getByUuid(String uuid) {
        LambdaQueryWrapper<ConversationIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationIndex::getConversationUuid, uuid);
        ConversationIndex conversation = getOne(queryWrapper);

        if (conversation != null) {
            // 填充用户信息
            return fillUserInfo(conversation);
        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationIndex createConversation(Long userId, String initialMessage) {
        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 创建会话索引
        ConversationIndex conversation = new ConversationIndex();
        conversation.setUserId(userId);
        conversation.setConversationUuid(UUID.randomUUID().toString());
        conversation.setLastMessage(initialMessage);
        // 状态设置为1-活跃
        conversation.setStatus(1);

        // 保存会话
        save(conversation);

        // 填充用户信息
        return fillUserInfo(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastMessage(String uuid, String message) {
        ConversationIndex conversation = getByUuid(uuid);
        if (conversation == null) {
            return false;
        }

        conversation.setLastMessage(message);

        return updateById(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferToService(String uuid, Long serviceUserId) {
        // 检查客服是否存在
        User serviceUser = userService.getById(serviceUserId);
        if (serviceUser == null || serviceUser.getUserType() != 2) { // 2表示客服人员
            throw new RuntimeException("客服不存在或用户类型不是客服");
        }

        ConversationIndex conversation = getByUuid(uuid);
        if (conversation == null) {
            return false;
        }

        // 状态设置为2-已转人工
        conversation.setStatus(2);
        conversation.setServiceUserId(serviceUserId);

        return updateById(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeConversation(String uuid) {
        ConversationIndex conversation = getByUuid(uuid);
        if (conversation == null) {
            return false;
        }

        // 状态设置为3-已关闭
        conversation.setStatus(3);

        return updateById(conversation);
    }

    @Override
    public List<ConversationIndex> listByServiceUserId(Long serviceUserId) {
        LambdaQueryWrapper<ConversationIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationIndex::getServiceUserId, serviceUserId)
                .orderByDesc(ConversationIndex::getUpdatedAt);
        List<ConversationIndex> conversations = list(queryWrapper);

        // 填充用户信息
        return conversations.stream().map(this::fillUserInfo).collect(Collectors.toList());
    }

    @Override
    public List<ConversationIndex> listTransferredByServiceUserId(Long serviceUserId) {
        // 使用Mapper的自定义方法
        List<ConversationIndex> conversations = baseMapper.selectTransferredByServiceUserId(serviceUserId);

        // 填充用户信息
        return conversations.stream().map(this::fillUserInfo).collect(Collectors.toList());
    }

    @Override
    public ConversationIndex getActiveByUserId(Long userId) {
        // 使用Mapper的自定义方法
        ConversationIndex conversation = baseMapper.selectActiveByUserId(userId);

        // 填充用户信息
        if (conversation != null) {
            return fillUserInfo(conversation);
        }

        return null;
    }

    @Override
    public List<ConversationIndex> listTransferred() {
        LambdaQueryWrapper<ConversationIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationIndex::getStatus, 2) // 2表示已转人工
                .orderByDesc(ConversationIndex::getUpdatedAt);

        List<ConversationIndex> conversations = list(queryWrapper);
        // 填充用户信息
        return conversations.stream().map(this::fillUserInfo).collect(Collectors.toList());
    }

    /**
     * 填充会话相关的用户信息
     *
     * @param conversation 会话索引
     * @return 填充后的会话索引
     */
    private ConversationIndex fillUserInfo(ConversationIndex conversation) {
        // 设置用户信息
        User user = userService.getById(conversation.getUserId());
        conversation.setUser(user);

        // 如果有客服ID，设置客服信息
        if (conversation.getServiceUserId() != null) {
            User serviceUser = userService.getById(conversation.getServiceUserId());
            conversation.setServiceUser(serviceUser);
        }

        return conversation;
    }
}