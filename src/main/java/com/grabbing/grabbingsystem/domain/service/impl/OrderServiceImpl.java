package com.grabbing.grabbingsystem.domain.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.grabbing.grabbingsystem.domain.service.OrderService;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.OrderDO;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.OrderItemDO;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.SkuDO;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.PayRecordDO;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.OrderItemMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.OrderMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.SkuMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.StockMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.PayRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grabbing.grabbingsystem.common.exception.BusinessException;
import com.grabbing.grabbingsystem.common.exception.ErrorCode;
import com.grabbing.grabbingsystem.common.exception.BizException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StockMapper stockMapper;
    private final SkuMapper skuMapper;
    private final PayRecordMapper payRecordMapper;
    @Transactional
    @Override
    public void cancelOrder(String orderNo) {

        // 1️⃣ 查订单
        OrderDO order = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getOrderNo, orderNo)
        );

        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST);
        }

        // 2️⃣ 只有 INIT 才能取消
        if (!"INIT".equals(order.getStatus())) {

            // 幂等处理：如果已经是 CANCELED，直接返回
            if ("CANCELED".equals(order.getStatus())) {
                return;
            }

            throw new BizException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        // 3️⃣ 条件更新（防并发）
        int updated = orderMapper.update(
                null,
                new LambdaUpdateWrapper<OrderDO>()
                        .eq(OrderDO::getOrderNo, orderNo)
                        .eq(OrderDO::getStatus, "INIT")
                        .set(OrderDO::getStatus, "CANCELED")
        );

        if (updated == 0) {
            // 说明已经被别的线程处理
            return;
        }

        // 4️⃣ 查订单明细
        List<OrderItemDO> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemDO>()
                        .eq(OrderItemDO::getOrderId, order.getId())
        );

        // 5️⃣ 回补库存
        for (OrderItemDO item : items) {
            stockMapper.addStock(item.getSkuId(), item.getCount());
        }
    }
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
    @Transactional
    @Override
    public void mockPay(String orderNo) {

        // 1) 查订单
        OrderDO order = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new BizException("订单不存在");
        }

        // 2) 幂等：已支付直接返回成功
        if ("PAID".equals(order.getStatus())) {
            return;
        }

        // 3) 只有 INIT 才能支付
        if (!"INIT".equals(order.getStatus())) {
            throw new BizException("当前状态不可支付: " + order.getStatus());
        }

        // 4) 计算金额（用 order_item 求和）
        Long amount = 0L;
        var items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemDO>()
                        .eq(OrderItemDO::getOrderId, order.getId())
        );
        for (var it : items) {
            amount += it.getPrice() * it.getCount();
        }

        // 5) 先插入 pay_record（order_no 唯一，保证幂等）
        PayRecordDO record = new PayRecordDO();
        record.setOrderNo(orderNo);
        record.setPayNo(UUID.randomUUID().toString());
        record.setAmount(amount);
        record.setStatus("SUCCESS");

        try {
            payRecordMapper.insert(record);
        } catch (Exception e) {
            // 可能是唯一索引冲突：说明已经有人支付过 / 并发支付
            // 再查一次订单状态，如果已 PAID 直接成功返回
            OrderDO latest = orderMapper.selectOne(
                    new LambdaQueryWrapper<OrderDO>().eq(OrderDO::getOrderNo, orderNo)
            );
            if (latest != null && "PAID".equals(latest.getStatus())) {
                return;
            }
            throw new BizException("支付记录写入失败（可能并发），请重试");
        }

        // 6) 条件更新订单状态：INIT -> PAID（防并发）
        int updated = orderMapper.update(
                null,
                new LambdaUpdateWrapper<OrderDO>()
                        .eq(OrderDO::getOrderNo, orderNo)
                        .eq(OrderDO::getStatus, "INIT")
                        .set(OrderDO::getStatus, "PAID")
        );

        if (updated == 0) {
            // 理论上极少发生：说明状态已经被改掉（比如刚超时/取消）
            throw new BizException("订单状态已变化，支付失败");
        }
    }
    @Transactional
    @Override
    public void timeoutOrder(String orderNo) {

        // 1) 先查订单（可选：为了拿 orderId 查 item）
        OrderDO order = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getOrderNo, orderNo)
        );
        if (order == null) {
            return;
        }

        // 2) 幂等：如果已经不是 INIT，直接跳过
        if (!"INIT".equals(order.getStatus())) {
            return;
        }

        // 3) 条件更新：INIT -> TIMEOUT（防并发，避免重复回补）
        int updated = orderMapper.update(
                null,
                new LambdaUpdateWrapper<OrderDO>()
                        .eq(OrderDO::getOrderNo, orderNo)
                        .eq(OrderDO::getStatus, "INIT")
                        .set(OrderDO::getStatus, "TIMEOUT")
        );

        if (updated == 0) {
            return; // 已被支付/取消/其他线程处理
        }

        // 4) 查明细并回补库存
        List<OrderItemDO> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemDO>()
                        .eq(OrderItemDO::getOrderId, order.getId())
        );

        for (OrderItemDO item : items) {
            stockMapper.addStock(item.getSkuId(), item.getCount());
        }
    }

}