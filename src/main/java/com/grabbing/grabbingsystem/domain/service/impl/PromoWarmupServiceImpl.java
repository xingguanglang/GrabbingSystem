package com.grabbing.grabbingsystem.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.grabbing.grabbingsystem.domain.service.PromoWarmupService;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.PromoDO;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.PromoSkuDO;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.PromoMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.PromoSkuMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

@Service
public class PromoWarmupServiceImpl implements PromoWarmupService {

    private final PromoMapper promoMapper;
    private final PromoSkuMapper promoSkuMapper;
    private final StringRedisTemplate redis;

    public PromoWarmupServiceImpl(PromoMapper promoMapper,
                                  PromoSkuMapper promoSkuMapper,
                                  StringRedisTemplate redis) {
        this.promoMapper = promoMapper;
        this.promoSkuMapper = promoSkuMapper;
        this.redis = redis;
    }

    @Override
    public void warmup(Long promoId) {
        PromoDO promo = promoMapper.selectById(promoId);
        if (promo == null) {
            throw new RuntimeException("PROMO_NOT_FOUND");
        }

        long nowMs = System.currentTimeMillis();
        long startMs = promo.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMs = promo.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (nowMs > endMs) {
            throw new RuntimeException("PROMO_ALREADY_ENDED");
        }

        Duration ttl = Duration.ofMillis(Math.max(1000, endMs - nowMs));

        // 1) 活动信息（可选但推荐）
        String infoKey = "promo:info:" + promoId;
        String infoJson = "{\"startTsMs\":" + startMs +
                ",\"endTsMs\":" + endMs +
                ",\"status\":\"" + promo.getStatus() + "\"}";
        redis.opsForValue().set(infoKey, infoJson, ttl);

        // 2) 活动 SKU 列表
        List<PromoSkuDO> list = promoSkuMapper.selectList(
                new LambdaQueryWrapper<PromoSkuDO>()
                        .eq(PromoSkuDO::getPromoId, promoId)
        );

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("PROMO_SKU_EMPTY");
        }

        // 3) 写库存 Key
        for (PromoSkuDO ps : list) {
            String stockKey = "promo:stock:" + promoId + ":" + ps.getSkuId();
            redis.opsForValue().set(stockKey, String.valueOf(ps.getSeckillStock()), ttl);
        }
    }
}