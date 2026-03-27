package com.grabbing.grabbingsystem.common.health;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.grabbing.grabbingsystem.domain.service.PromoWarmupService;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.PromoDO;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.PromoSkuDO;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.PromoMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.PromoSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevPressureTestStockRunner {
    private final PromoWarmupService promoWarmupService;
    private final PromoMapper promoMapper;
    private final PromoSkuMapper promoSkuMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${pressure-test.auto-refill.enabled:false}")
    private boolean enabled;
    @Value("${pressure-test.auto-refill.promo-id:1}")
    private Long promoId;
    @Value("${pressure-test.auto-refill.stock:50000}")
    private Integer refillStock;
    @Bean
    public ApplicationRunner autoRefillStockRunner() {
        return args -> {
            if (!enabled) {
                log.info("[Pressure-Test] auto refill disabled");
                return;
            }
            try {
                promoWarmupService.warmup(promoId);
            } catch (Exception e) {
                log.warn("[PRESSURE-TEST] warmup failed, promoId={}, err={}", promoId, e.getMessage());
            }
            PromoDO promo = promoMapper.selectById(promoId);
            if (promo == null) {
                log.warn("[PRESSURE-TEST] promo not found, promoId={}", promoId);
                return;
            }
            Duration ttl = resolveTtl(promo.getEndTime());
            List<PromoSkuDO> promoSkus = promoSkuMapper.selectList(
                    new LambdaQueryWrapper<PromoSkuDO>().eq(PromoSkuDO::getPromoId, promoId)
            );
            if (promoSkus == null || promoSkus.isEmpty()) {
                log.warn("[PRESSURE-TEST] promo_sku empty, promoId={}", promoId);
                return;
            }

            for (PromoSkuDO ps : promoSkus) {
                String key = "promo:stock:" + promoId + ":" + ps.getSkuId();
                stringRedisTemplate.opsForValue().set(key, String.valueOf(refillStock), ttl);
            }

            log.info("[PRESSURE-TEST] auto refill done, promoId={}, skuCount={}, stockEach={}",
                    promoId, promoSkus.size(), refillStock);
        };
    }
    private Duration resolveTtl(LocalDateTime endTime) {
        long nowMs = System.currentTimeMillis();
        long endMs = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return Duration.ofMillis(Math.max(1000, endMs - nowMs));
    }
}
