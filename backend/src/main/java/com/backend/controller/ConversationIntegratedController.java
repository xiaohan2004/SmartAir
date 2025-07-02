package com.backend.controller;

import com.backend.common.Result;
import com.backend.common.enumeration.ResultCode;
import com.backend.entity.ConversationDocument;
import com.backend.entity.ConversationIndex;
import com.backend.service.ConversationIntegratedService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 整合会话控制器
 * 处理会话创建、查询、转接、关闭和消息追加等功能
 */
@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
@Primary
public class ConversationIntegratedController {

    private final ConversationIntegratedService conversationService;

    /**
     * 获取会话详情（索引信息）
     *
     * @param uuid 会话UUID
     * @return 会话详情
     */
    @GetMapping("/{uuid}")
    public Result getConversation(@PathVariable String uuid) {
        ConversationIndex conversation = conversationService.getConversationIndex(uuid);
        if (conversation == null) {
            return Result.error(ResultCode.NOT_FOUND, "会话不存在");
        }
        return Result.success(conversation);
    }

    /**
     * 获取会话详情（包含完整消息内容）
     *
     * @param uuid 会话UUID
     * @return 会话详情
     */
    @GetMapping("/{uuid}/content")
    public Result getConversationContent(@PathVariable String uuid) {
        ConversationDocument conversation = conversationService.getConversationContent(uuid);
        if (conversation == null) {
            return Result.error(ResultCode.NOT_FOUND, "会话不存在");
        }
        return Result.success(conversation);
    }

    /**
     * 获取用户会话列表（索引信息）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @GetMapping("/user/{userId}")
    public Result listConversationsByUserId(@PathVariable Long userId) {
        List<ConversationIndex> conversations = conversationService.listConversationsByUserId(userId);
        return Result.success(conversations);
    }

    /**
     * 获取用户会话列表（包含完整消息内容）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @GetMapping("/user/{userId}/contents")
    public Result getUserConversationContents(@PathVariable Long userId) {
        List<ConversationDocument> conversations = conversationService.getUserConversationContents(userId);
        return Result.success(conversations);
    }

    /**
     * 获取客服会话列表（仅已转人工的会话）
     *
     * @param serviceUserId 客服用户ID
     * @return 会话列表
     */
    @GetMapping("/service/{serviceUserId}")
    public Result listConversationsByServiceUserId(@PathVariable Long serviceUserId) {
        List<ConversationIndex> conversations = conversationService.listConversationsByServiceUserId(serviceUserId);
        return Result.success(conversations);
    }

    /**
     * 获取所有已转人工的会话
     *
     * @return 会话列表
     */
    @GetMapping("/transferred")
    public Result listTransferredConversations() {
        List<ConversationIndex> conversations = conversationService.listTransferredConversations();
        return Result.success(conversations);
    }

    /**
     * 获取用户活跃会话
     *
     * @param userId 用户ID
     * @return 活跃会话
     */
    @GetMapping("/user/{userId}/active")
    public Result getUserActiveConversation(@PathVariable Long userId) {
        ConversationIndex activeConversation = conversationService.getActiveByUserId(userId);
        return Result.success(activeConversation);
    }

    /**
     * 创建新会话（兼容旧接口）
     *
     * @param params 包含用户ID和初始消息的Map
     * @return 创建结果
     */
    @PostMapping
    public Result createConversation(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String initialMessage = (String) params.get("initialMessage");
        
        // 参数校验
        if (userId == null || initialMessage == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID或初始消息不能为空");
        }
        
        Map<String, Object> result = conversationService.createConversation(userId, initialMessage);
        ConversationIndex conversation = (ConversationIndex) result.get("index");
        
        if (conversation != null) {
            return Result.success("会话创建成功", conversation);
        } else {
            return Result.error("会话创建失败");
        }
    }

    /**
     * 创建新会话（新接口，使用请求参数）
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
        boolean success = conversationService.appendMessage(uuid, speaker, text);
        if (success) {
            return Result.success();
        } else {
            return Result.error("消息追加失败");
        }
    }

    /**
     * 转接会话到客服
     *
     * @param uuid 会话UUID
     * @param params 包含客服用户ID的Map
     * @return 转接结果
     */
    @PutMapping("/{uuid}/transfer")
    public Result transferToService(@PathVariable String uuid, @RequestBody Map<String, Long> params) {
        Long serviceUserId = params.get("serviceUserId");
        
        // 参数校验
        if (serviceUserId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "客服用户ID不能为空");
        }
        
        boolean success = conversationService.transferToService(uuid, serviceUserId);
        if (success) {
            return Result.success("会话转接成功", null);
        } else {
            return Result.error("会话转接失败");
        }
    }

    /**
     * 关闭会话
     *
     * @param uuid 会话UUID
     * @return 关闭结果
     */
    @PutMapping("/{uuid}/close")
    public Result closeConversation(@PathVariable String uuid) {
        boolean success = conversationService.closeConversation(uuid);
        if (success) {
            return Result.success("会话关闭成功", null);
        } else {
            return Result.error("会话关闭失败");
        }
    }

    /**
     * 删除会话
     *
     * @param id 会话UUID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteConversation(@PathVariable String id) {
        boolean success = conversationService.deleteConversation(id);
        if (success) {
            return Result.success("会话删除成功", null);
        } else {
            return Result.error("会话删除失败");
        }
    }

    /**
     * 获取所有会话列表（管理员使用）
     *
     * @return 会话列表
     */
    @GetMapping("/admin/list")
    public Result listAllConversations() {
        List<ConversationIndex> conversations = conversationService.listAllConversations();
        return Result.success(conversations);
    }
} 