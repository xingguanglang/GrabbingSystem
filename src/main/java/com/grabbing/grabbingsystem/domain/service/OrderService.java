package com.grabbing.grabbingsystem.domain.service;

public interface OrderService {

    /**
     * 创建订单（成功返回订单号）
     */
    String createOrder(Long userId, Long skuId, Integer count, String orderType, Long promoId);
    void timeoutOrder(String orderNo);
    void mockPay(String orderNo);
    void cancelOrder(String orderNo);
    String createOrder(Long userId, Long skuId, Integer count);
}

