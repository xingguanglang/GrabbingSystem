package com.grabbing.grabbingsystem.infrastructure.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.OrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {}