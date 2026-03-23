package com.grabbing.grabbingsystem.common.health;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthCheck {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisHealthCheck(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void check() {
        String pong = stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .ping();
        System.out.println("[REDIS] PING => " + pong);
    }
}