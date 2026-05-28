package com.cm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cm.entity.Accessory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccessoryMapper extends BaseMapper<Accessory> {

    @Select("SELECT MAX(item_code) FROM accessory WHERE item_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxItemCodeByPrefix(@Param("prefix") String prefix);
}