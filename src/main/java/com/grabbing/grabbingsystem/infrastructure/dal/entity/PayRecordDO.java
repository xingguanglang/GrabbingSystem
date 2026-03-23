package com.grabbing.grabbingsystem.infrastructure.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pay_record")
public class PayRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("pay_no")
    private String payNo;

    private Long amount;

    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;
}