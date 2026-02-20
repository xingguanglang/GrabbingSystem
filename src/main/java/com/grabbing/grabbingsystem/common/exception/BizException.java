package com.grabbing.grabbingsystem.common.exception;

/**
 * BizException：业务异常（你主动抛出的异常）
 *
 * 什么时候用？
 * 比如：用户不存在、库存不足、重复下单、参数不符合业务规则……
 *
 * 为什么不用 RuntimeException 直接抛？
 * 因为我们想带上“业务错误码 code”，让前端可以更精细地提示。
 */
public class BizException extends RuntimeException {

    /**
     * 业务错误码：你可以自己定义（例如 40001 参数错误，50000 系统错误等）
     */
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 一个小工具方法：写起来更短
     */
    public static BizException of(int code, String message) {
        return new BizException(code, message);
    }
}