package com.grabbing.grabbingsystem.interfaces.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/debug/redis")
public class RedisDebugController {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisDebugController(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostMapping("/setStock")
    public String setStock(@RequestParam Long promoId,
                           @RequestParam Long skuId,
                           @RequestParam Integer stock) {
        String key = "promo:stock:" + promoId + ":" + skuId;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(stock), Duration.ofHours(1));
        return "OK key=" + key;
    }

    @GetMapping("/get")
    public String get(@RequestParam String key) {
        return String.valueOf(stringRedisTemplate.opsForValue().get(key));
    }
}