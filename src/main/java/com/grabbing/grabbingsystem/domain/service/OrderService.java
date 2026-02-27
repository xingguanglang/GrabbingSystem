package com.grabbing.grabbingsystem.domain.service;

public interface OrderService {

    /**
     * 创建订单（成功返回订单号）
     */
    String createOrder(Long userId, Long skuId, Integer count);
}