package com.grabbing.grabbingsystem.common.exception;

public enum ErrorCode {

    // 通用
    // 秒杀相关
    SECKILL_NOT_STARTED(41001, "活动未开始"),
    SECKILL_ENDED(41002, "活动已结束"),
    SECKILL_RATE_LIMITED(41003, "请求过于频繁"),
    SECKILL_DUPLICATE(41004, "您已参与过秒杀"),
    SECKILL_SOLD_OUT(41005, "商品已售罄"),
    PARAM_INVALID(40001, "参数校验失败"),
    SYSTEM_ERROR(50000, "系统异常"),

    // SKU 相关（第2天必须）
    SKU_NOT_FOUND(40401, "SKU 不存在"),
    SKU_OFFLINE(40002, "SKU 已下架"),

    STOCK_NOT_ENOUGH(40003,"库存不足"),
    ORDER_NOT_EXIST(45001,"订单不存在"),
    ORDER_CANNOT_CANCEL(45000,"当前状态无法取消");
    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() { return code; }
    public String getMsg() { return msg; }
}