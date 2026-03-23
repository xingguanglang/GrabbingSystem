package com.grabbing.grabbingsystem.interfaces.controller;

import com.grabbing.grabbingsystem.domain.service.SeckillService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @PostMapping("/order/create")
    public String create(@RequestParam Long userId,
                         @RequestParam Long promoId,
                         @RequestParam Long skuId) {
        // 你后面换成从 JWT 取 userId，这里先方便压测
        return seckillService.seckillCreateOrder(userId, promoId, skuId);
    }
}