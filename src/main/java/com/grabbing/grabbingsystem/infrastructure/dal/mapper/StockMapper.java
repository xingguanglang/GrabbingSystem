package com.grabbing.grabbingsystem.infrastructure.dal.mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grabbing.grabbingsystem.infrastructure.dal.entity.StockDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface StockMapper extends BaseMapper<StockDO> {
    @Update("""
        UPDATE stock
        SET stock_count = stock_count - #{count}
        WHERE sku_id = #{skuId}
          AND stock_count >= #{count}
        """)
    int deductStock(@Param("skuId") Long skuId,
                    @Param("count") Integer count);
}