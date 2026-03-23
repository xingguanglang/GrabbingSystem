package com.grabbing.grabbingsystem.infrastructure.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("promo_sku")
public class PromoSkuDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long promoId;

    private Long skuId;

    private Integer seckillPrice;

    private Integer seckillStock;

    private Integer limitPerUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}