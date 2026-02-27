package com.grabbing.grabbingsystem.domain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grabbing.grabbingsystem.domain.service.SkuService;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.SkuDO;
import com.grabbing.grabbingsystem.infrastructure.dal.mapper.SkuMapper;
import org.springframework.stereotype.Service;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, SkuDO> implements SkuService {
}