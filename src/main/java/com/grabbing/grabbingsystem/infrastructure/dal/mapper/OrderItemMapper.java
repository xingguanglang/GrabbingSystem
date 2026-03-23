package com.grabbing.grabbingsystem.infrastructure.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.OrderItemDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemDO> {}