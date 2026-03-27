package com.grabbing.grabbingsystem.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.grabbing.grabbingsystem.domain.service.OrderService;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.OrderDO;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Value("${order.timeout-second:150}")
    private long timeout;

    // 默认每分钟扫一次，你也可以换成读取配置 cron（先简单固定也行）
    @Scheduled(cron = "${order.timeout-scan-cron:0 * * * * ?}")
    public void scanTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusSeconds(timeout);

        // 只扫 INIT 且 create_time < deadline 的订单，限制数量防止一次扫太多
        List<OrderDO> timeoutOrders = orderMapper.selectList(
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getStatus, "INIT")
                        .lt(OrderDO::getCreateTime, deadline)
                        .last("LIMIT 100")
        );

        if (timeoutOrders.isEmpty()) {
            return;
        }

        log.info("发现超时 INIT 订单数量: {}", timeoutOrders.size());

        for (OrderDO order : timeoutOrders) {
            try {
                orderService.timeoutOrder(order.getOrderNo());
            } catch (Exception e) {
                // 单笔失败不影响下一笔
                log.warn("超时处理失败 orderNo={}", order.getOrderNo(), e);
            }
        }
    }
}