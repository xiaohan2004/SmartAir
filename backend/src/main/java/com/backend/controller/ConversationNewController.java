package com.backend.controller;

import com.backend.entity.ConversationDocument;
import com.backend.service.ConversationIntegratedService;
import com.backend.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会话内容控制器
 * 处理会话内容的创建、追加和查询
 * 
 * @deprecated 已被 {@link ConversationIntegratedController} 替代，为保持兼容性保留
 */
@RestController
@RequestMapping("/deprecated/mongodb/api/conversation")
@RequiredArgsConstructor
@Deprecated
public class ConversationNewController {

    private final ConversationIntegratedService conversationService;

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @param userInput 用户输入的初始消息
     * @return 创建结果
     */
    @PostMapping("/start")
    public Result startConversation(
            @RequestParam Long userId,
            @RequestParam String userInput) {
        Map<String, Object> result = conversationService.createConversation(userId, userInput);
        return Result.success(result);
    }

    /**
     * 追加消息到会话
     *
     * @param uuid 会话UUID
     * @param speaker 发言者
     * @param text 消息内容
     * @return 追加结果
     */
    @PostMapping("/{uuid}/append")
    public Result appendMessage(
            @PathVariable String uuid,
            @RequestParam String speaker,
            @RequestParam String text) {
        conversationService.appendMessage(uuid, speaker, text);
        return Result.success();
    }

    /**
     * 获取用户会话列表（包含完整消息内容）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @GetMapping("/user/{userId}")
    public Result getByUser(@PathVariable Long userId) {
        List<ConversationDocument> conversations = conversationService.getUserConversationContents(userId);
        return Result.success(conversations);
    }

    /**
     * 删除会话
     *
     * @param id 会话UUID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id) {
        boolean success = conversationService.deleteConversation(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }
}
