package com.backend.dao.mapper;

import com.backend.entity.ConversationIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会话索引Mapper接口
 */
@Mapper
public interface ConversationIndexMapper extends BaseMapper<ConversationIndex> {
    
    /**
     * 查询客服处理的已转人工会话列表
     * @param serviceUserId 客服用户ID
     * @return 已转人工的会话列表
     */
    @Select("SELECT * FROM conversation_index WHERE service_user_id = #{serviceUserId} AND status = 2 ORDER BY updated_at DESC")
    List<ConversationIndex> selectTransferredByServiceUserId(@Param("serviceUserId") Long serviceUserId);
    
    /**
     * 查询用户的活跃会话
     * @param userId 用户ID
     * @return 活跃会话
     */
    @Select("SELECT * FROM conversation_index WHERE user_id = #{userId} AND status != 3 ORDER BY updated_at DESC LIMIT 1")
    ConversationIndex selectActiveByUserId(@Param("userId") Long userId);
}