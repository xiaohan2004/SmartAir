package com.backend.common;

import com.backend.common.enumeration.ResultCode;

/**
 * 统一响应结果封装类
 */
public class Result {
    private Integer code; // 状态码
    private String msg;   // 提示信息
    private Object data;  // 数据

    public Result() {}

    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 成功返回结果
     *
     * @param data 数据
     * @return 结果
     */
    public static Result success(Object data) {
        return new Result(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     *
     * @return 结果
     */
    public static Result success() {
        return new Result(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }
    
    /**
     * 成功返回结果
     *
     * @param message 成功消息
     * @param data 数据
     * @return 结果
     */
    public static Result success(String message, Object data) {
        return new Result(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     *
     * @param msg 错误信息
     * @return 结果
     */
    public static Result error(String msg) {
        return new Result(ResultCode.ERROR.getCode(), msg, null);
    }

    /**
     * 失败返回结果
     *
     * @param resultCode 错误码
     * @param msg 错误信息
     * @return 结果
     */
    public static Result error(ResultCode resultCode, String msg) {
        return new Result(resultCode.getCode(), msg, null);
    }

    /**
     * 失败返回结果
     *
     * @param resultCode 错误码
     * @return 结果
     */
    public static Result error(ResultCode resultCode) {
        return new Result(resultCode.getCode(), resultCode.getMessage(), null);
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}