package com.grabbing.grabbingsystem.interfaces.vo;

import lombok.Data;

@Data
public class SkuListVO {
    private Long skuId;
    private String name;
    private Long price;
    private String status;
}