package com.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话索引实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("conversation_index")
public class ConversationIndex {
    
    /**
     * 会话索引ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话UUID
     */
    private String conversationUuid;
    
    /**
     * 最后一条消息内容
     */
    private String lastMessage;
    
    /**
     * 会话状态：active(活跃)、transferred(已转人工)、closed(已关闭)
     */
    private String status;
    
    /**
     * 处理客服ID，转人工时有效
     */
    private Long serviceUserId;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 关联用户信息，非数据库字段
     */
    @TableField(exist = false)
    private User user;
    
    /**
     * 关联客服信息，非数据库字段
     */
    @TableField(exist = false)
    private User serviceUser;
}