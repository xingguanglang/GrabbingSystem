package com.grabbing.grabbingsystem.infrastructure.redis;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeckillRedisExecutor {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> script;

    public SeckillRedisExecutor(StringRedisTemplate redis) {
        this.redis = redis;

        this.script = new DefaultRedisScript<>();
        this.script.setLocation(new ClassPathResource("lua/seckill.lua"));
        this.script.setResultType(Long.class);
    }

    public long trySeckill(String stockKey, String buyKey, String rlKey,
                           long nowTsMs, long startTsMs, long endTsMs,
                           int buyTtlSeconds, int qpsLimit, int rlTtlSeconds) {

        Long result = redis.execute(
                script,
                List.of(stockKey, buyKey, rlKey),
                String.valueOf(nowTsMs),
                String.valueOf(startTsMs),
                String.valueOf(endTsMs),
                String.valueOf(buyTtlSeconds),
                String.valueOf(qpsLimit),
                String.valueOf(rlTtlSeconds)
        );

        return result == null ? -1 : result;
    }

    public void rollback(String stockKey, String buyKey) {
        redis.opsForValue().increment(stockKey);
        redis.delete(buyKey);
    }
}