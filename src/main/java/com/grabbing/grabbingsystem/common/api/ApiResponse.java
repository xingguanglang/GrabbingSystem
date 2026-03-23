package com.grabbing.grabbingsystem.common.api;

/**
 * ApiResponse：统一接口返回结构
 *
 * 为什么要统一？
 * 1) 前端写起来方便：只要判断 code==0 成功，否则失败
 * 2) 后端也方便：全局异常处理可以统一返回 fail(...)
 * 3) Swagger 展示更统一，便于测试
 *
 * 返回结构示例：
 * {
 *   "code": 0,
 *   "msg": "OK",
 *   "data": "ok"
 * }
 */
public class ApiResponse<T> {

    /**
     * 状态码：0 成功；非 0 失败
     */
    public int code;

    /**
     * 提示信息：成功一般是 OK，失败一般是错误原因
     */
    public String msg;

    /**
     * 具体数据（成功时才有）
     */
    public T data;

    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 快捷成功返回
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    /**
     * 快捷失败返回（data 统一为 null）
     */
    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}