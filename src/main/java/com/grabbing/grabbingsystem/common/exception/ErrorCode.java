package com.grabbing.grabbingsystem.common.exception;

public enum ErrorCode {

    // 通用
    PARAM_INVALID(40001, "参数校验失败"),
    SYSTEM_ERROR(50000, "系统异常"),

    // SKU 相关（第2天必须）
    SKU_NOT_FOUND(40401, "SKU 不存在"),
    SKU_OFFLINE(40002, "SKU 已下架"),

    STOCK_NOT_ENOUGH(40003,"库存不足");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() { return code; }
    public String getMsg() { return msg; }
}