package com.grabbing.grabbingsystem.interfaces.controller;

import com.grabbing.grabbingsystem.common.api.ApiResponse;
import com.grabbing.grabbingsystem.domain.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ApiResponse<CreateOrderResponse> create(@RequestBody CreateOrderRequest req) {
        String orderNo = orderService.createOrder(req.getUserId(), req.getSkuId(), req.getCount());
        return ApiResponse.ok(new CreateOrderResponse(orderNo));
    }

    @Data
    public static class CreateOrderRequest {
        private Long userId;
        private Long skuId;
        private Integer count;
    }

    @Data
    @RequiredArgsConstructor
    public static class CreateOrderResponse {
        private final String orderNo;
    }
}