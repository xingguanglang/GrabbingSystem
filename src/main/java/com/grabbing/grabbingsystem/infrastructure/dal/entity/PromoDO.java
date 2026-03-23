package com.grabbing.grabbingsystem.infrastructure.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("promo")
public class PromoDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}