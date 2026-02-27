package com.grabbing.grabbingsystem.domain.service.impl;

import com.grabbing.grabbingsystem.domain.service.OrderService;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.OrderDO;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.OrderItemDO;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.SkuDO;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.OrderItemMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.OrderMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.SkuMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grabbing.grabbingsystem.common.exception.BusinessException;
import com.grabbing.grabbingsystem.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StockMapper stockMapper;
    private final SkuMapper skuMapper;

    @Override
    @Transactional
    public String createOrder(Long userId, Long skuId, Integer count) {

        // 1) 查 sku
        SkuDO sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }
        if (!"ON".equals(sku.getStatus())) {
            throw new BusinessException(ErrorCode.SKU_OFFLINE);
        }

        // 2) 扣库存（防超卖核心）
        int affected = stockMapper.deductStock(skuId, count);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        // 3) 建订单
        String orderNo = UUID.randomUUID().toString();
        long totalAmount = sku.getPrice() * count;

        OrderDO order = new OrderDO();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("INIT");
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        orderMapper.insert(order);

        // 4) 建明细
        OrderItemDO item = new OrderItemDO();
        item.setOrderId(order.getId());
        item.setSkuId(skuId);
        item.setPrice(sku.getPrice());
        item.setCount(count);
        item.setCreateTime(now);
        orderItemMapper.insert(item);

        return orderNo;
    }
}