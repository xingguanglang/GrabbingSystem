package com.grabbing.grabbingsystem.interfaces.controller;

import com.grabbing.grabbingsystem.domain.service.PromoWarmupService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/promo")
public class AdminPromoController {

    private final PromoWarmupService promoWarmupService;

    public AdminPromoController(PromoWarmupService promoWarmupService) {
        this.promoWarmupService = promoWarmupService;
    }

    @PostMapping("/{promoId}/warmup")
    public String warmup(@PathVariable Long promoId) {
        promoWarmupService.warmup(promoId);
        return "OK";
    }
}