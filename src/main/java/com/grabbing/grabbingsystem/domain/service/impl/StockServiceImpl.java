package com.grabbing.grabbingsystem.domain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grabbing.grabbingsystem.domain.service.StockService;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.StockDO;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.StockMapper;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, StockDO> implements StockService {
}