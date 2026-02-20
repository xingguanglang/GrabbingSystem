package com.grabbing.grabbingsystem.common.health;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * HealthMapper：用于“验证 MySQL 真连通”的最小 Mapper
 *
 * 为什么要这样做？
 * 因为“配置没报错”≠“真的能查库”
 * 我们要真正执行一次 SQL（SELECT 1）
 */
@Mapper
public interface HealthMapper {

    /**
     * SELECT 1 是最轻量的探活 SQL：
     * - 不依赖任何业务表
     * - 成功就证明连接 + 查询都 OK
     */
    @Select("SELECT 1")
    Integer selectOne();
}