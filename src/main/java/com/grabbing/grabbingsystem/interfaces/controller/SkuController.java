package com.grabbing.grabbingsystem.interfaces.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grabbing.grabbingsystem.common.api.ApiResponse;
import com.grabbing.grabbingsystem.common.exception.BizException;
import com.grabbing.grabbingsystem.common.exception.ErrorCode;
import com.grabbing.grabbingsystem.domain.service.SkuService;
import com.grabbing.grabbingsystem.domain.service.StockService;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.SkuDO;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.StockDO;
import com.grabbing.grabbingsystem.interfaces.vo.SkuDetailVO;
import com.grabbing.grabbingsystem.interfaces.vo.SkuListVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sku")
@Validated // 让 @PathVariable/@RequestParam 的校验生效
public class SkuController {

    private final SkuService skuService;
    private final StockService stockService;

    public SkuController(SkuService skuService, StockService stockService) {
        this.skuService = skuService;
        this.stockService = stockService;
    }

    /**
     * GET /api/sku/list?page=1&size=10&status=ON
     */
    @GetMapping("/list")
    public ApiResponse<List<SkuListVO>> list(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "ON") String status
    ) {
        Page<SkuDO> p = skuService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<SkuDO>()
                        .eq(SkuDO::getStatus, status)
                        .orderByDesc(SkuDO::getId)
        );

        List<SkuListVO> list = p.getRecords().stream().map(sku -> {
            SkuListVO vo = new SkuListVO();
            vo.setSkuId(sku.getId());
            vo.setName(sku.getName());
            vo.setPrice(sku.getPrice());
            vo.setStatus(sku.getStatus());
            return vo;
        }).toList();

        return ApiResponse.ok(list);
    }

    /**
     * GET /api/sku/{skuId}
     */
    @GetMapping("/{skuId}")
    public ApiResponse<SkuDetailVO> detail(
            @PathVariable @NotNull @Min(1) Long skuId
    ) {
        SkuDO sku = skuService.getById(skuId);
        if (sku == null) {
            throw new BizException(ErrorCode.SKU_NOT_FOUND);
        }
        if (!"ON".equalsIgnoreCase(sku.getStatus())) {
            throw new BizException(ErrorCode.SKU_OFFLINE);
        }

        StockDO stock = stockService.getOne(
                new LambdaQueryWrapper<StockDO>().eq(StockDO::getSkuId, skuId),
                false // 第二个参数：不抛出 “查到多条” 异常（虽然你有 unique sku_id 理论上不会）
        );

        SkuDetailVO vo = new SkuDetailVO();
        vo.setSkuId(sku.getId());
        vo.setName(sku.getName());
        vo.setPrice(sku.getPrice());
        vo.setStatus(sku.getStatus());
        vo.setStockCount(stock == null ? 0 : stock.getStockCount());

        return ApiResponse.ok(vo);
    }
}