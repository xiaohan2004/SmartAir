package com.backend.common.enumeration;

/**
 * 结果状态码
 */
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 失败
     */
    ERROR(500, "操作失败"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用");
    
    /**
     * 状态码
     */
    private final int code;
    
    /**
     * 消息
     */
    private final String message;
    
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
} 