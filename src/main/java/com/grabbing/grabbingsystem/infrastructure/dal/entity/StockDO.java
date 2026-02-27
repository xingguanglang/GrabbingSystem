package com.grabbing.grabbingsystem.infrastructure.dal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock")
public class StockDO {
    private Long id;
    private Long skuId;
    private Integer stockCount;
    private LocalDateTime updateTime;
}