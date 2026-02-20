package com.grabbing.grabbingsystem.common.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * StartupHealthCheck：项目启动后自动跑探活逻辑
 *
 * 为什么用 ApplicationRunner？
 * - 它会在 Spring Boot 启动完成后执行一次
 * - 很适合做“第 1 天验收用”的连通性检查
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StartupHealthCheck {

    private final HealthMapper healthMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * MySQL 探活：启动时执行 SELECT 1
     */
    @Bean
    public ApplicationRunner mysqlHealthRunner() {
        return args -> {
            Integer one = healthMapper.selectOne();
            log.info("[MYSQL] SELECT 1 => {}", one);
        };
    }

    /**
     * Redis 探活：启动时 set/get 一次 key
     */
    @Bean
    public ApplicationRunner redisHealthRunner() {
        return args -> {
            String key = "grabbingsystem:ping";
            stringRedisTemplate.opsForValue().set(key, "ok");
            String val = stringRedisTemplate.opsForValue().get(key);
            log.info("[REDIS] {} => {}", key, val);
        };
    }
}