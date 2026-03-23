package com.grabbing.grabbingsystem.interfaces.controller;

import com.grabbing.grabbingsystem.common.api.ApiResponse;
import com.grabbing.grabbingsystem.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final OrderService orderService;

    @PostMapping("/mock")
    public ApiResponse<Void> mock(@RequestParam String orderNo) {
        orderService.mockPay(orderNo);
        return ApiResponse.ok(null);
    }
}