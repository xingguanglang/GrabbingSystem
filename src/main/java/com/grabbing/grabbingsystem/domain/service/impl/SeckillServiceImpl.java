package com.grabbing.grabbingsystem.domain.service.impl;

import com.grabbing.grabbingsystem.domain.service.OrderService;
import com.grabbing.grabbingsystem.domain.service.SeckillService;
import com.grabbing.grabbingsystem.infrastructure.redis.SeckillRedisExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.grabbing.grabbingsystem.common.exception.BizException;
import com.grabbing.grabbingsystem.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SeckillServiceImpl implements SeckillService {

    private final SeckillRedisExecutor seckillRedisExecutor;
    private final StringRedisTemplate redis;
    private final OrderService orderService;

    public SeckillServiceImpl(SeckillRedisExecutor seckillRedisExecutor,
                              StringRedisTemplate redis,
                              OrderService orderService) {
        this.seckillRedisExecutor = seckillRedisExecutor;
        this.redis = redis;
        this.orderService = orderService;
    }

    @Override
    public String seckillCreateOrder(Long userId, Long promoId, Long skuId) {
        // 1) 从 promo:info 读取活动时间窗（你已经 warmup 写进去了）
        String infoKey = "promo:info:" + promoId;
        String infoJson = redis.opsForValue().get(infoKey);
        if (infoJson == null) {
            throw new RuntimeException("PROMO_NOT_WARMED_UP");
        }

        long startTsMs = extractLong(infoJson, "startTsMs");
        long endTsMs = extractLong(infoJson, "endTsMs");

        long now = Instant.now().toEpochMilli();
        String stockKey = "promo:stock:" + promoId + ":" + skuId;
        String buyKey = "promo:buy:" + promoId + ":" + skuId + ":" + userId;
        String rlKey = "rl:promo:" + promoId + ":" + (now / 1000);

        int buyTtlSeconds = (int) Math.max(5, (endTsMs - now) / 1000);
        long code = seckillRedisExecutor.trySeckill(
                stockKey, buyKey, rlKey,
                now, startTsMs, endTsMs,
                buyTtlSeconds,
                2000, // qpsLimit：先写死，后面做配置
                1     // rlTtlSeconds
        );

        if (code != 0) {
            switch ((int) code) {
                case 11 -> throw new BizException(ErrorCode.SECKILL_NOT_STARTED);
                case 12 -> throw new BizException(ErrorCode.SECKILL_ENDED);
                case 13 -> throw new BizException(ErrorCode.SECKILL_RATE_LIMITED);
                case 14 -> throw new BizException(ErrorCode.SECKILL_DUPLICATE);
                case 15 -> throw new BizException(ErrorCode.SECKILL_SOLD_OUT);
                default -> throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }

        // 2) Redis 预扣成功 => 复用你现有的 MySQL 建单逻辑
        try {
            return orderService.createOrder(userId, skuId, 1);
        } catch (Exception e) {
            // 3) MySQL 落库失败 => 回滚 Redis（把库存+1，删除 buyKey）
            seckillRedisExecutor.rollback(stockKey, buyKey);
            throw e;
        }
    }

    // 超简单 JSON 取值（避免你现在引入 Jackson 映射类）
    private long extractLong(String json, String field) {
        // json 类似：{"startTsMs":1772...,"endTsMs":...,"status":"ONLINE"}
        String k = "\"" + field + "\":";
        int i = json.indexOf(k);
        if (i < 0) throw new RuntimeException("BAD_PROMO_INFO");
        int start = i + k.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Long.parseLong(json.substring(start, end));
    }
}