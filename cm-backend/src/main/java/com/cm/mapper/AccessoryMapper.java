package com.cm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cm.entity.Accessory;
import com.cm.vo.InventoryGroupVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AccessoryMapper extends BaseMapper<Accessory> {

    @Select("SELECT MAX(item_code) FROM accessory WHERE item_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxItemCodeByPrefix(@Param("prefix") String prefix);

    @SelectProvider(type = InventoryGroupProvider.class, method = "build")
    List<InventoryGroupVO> selectInventoryGroup(@Param("barcode") String barcode,
                                                 @Param("categoryId") Long categoryId,
                                                 @Param("statusFilter") Integer statusFilter,
                                                 @Param("workerId") Long workerId,
                                                 @Param("remark") String remark,
                                                 @Param("highValue") Integer highValue,
                                                 @Param("shelfId") Long shelfId,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);

    class InventoryGroupProvider {
        public String build(@Param("barcode") String barcode,
                           @Param("categoryId") Long categoryId,
                           @Param("statusFilter") Integer statusFilter,
                           @Param("workerId") Long workerId,
                           @Param("remark") String remark,
                           @Param("highValue") Integer highValue,
                           @Param("shelfId") Long shelfId,
                           @Param("startDate") String startDate,
                           @Param("endDate") String endDate) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT a.barcode, a.category_id AS categoryId, c.name AS categoryName, c.part_number AS partNumber, ");
            sb.append("COUNT(*) AS totalCount, ");
            sb.append("CAST(SUM(CASE WHEN a.status = 1 THEN 1 ELSE 0 END) AS SIGNED) AS availableCount ");
            sb.append("FROM accessory a LEFT JOIN category c ON a.category_id = c.id ");
            sb.append("WHERE 1=1 ");
            sb.append("AND a.deleted = 0 ");
            if (barcode != null && !barcode.isEmpty()) sb.append("AND (a.barcode LIKE CONCAT('%', #{barcode}, '%') OR a.item_code LIKE CONCAT('%', #{barcode}, '%')) ");
            if (categoryId != null) sb.append("AND a.category_id = #{categoryId} ");
            if (statusFilter != null) sb.append("AND a.status = #{statusFilter} ");
            if (workerId != null) sb.append("AND a.worker_id = #{workerId} ");
            if (remark != null && !remark.isEmpty()) sb.append("AND a.remark LIKE CONCAT('%', #{remark}, '%') ");
            if (highValue != null) sb.append("AND a.is_high_value = #{highValue} ");
            if (shelfId != null) sb.append("AND a.shelf_id = #{shelfId} ");
            if (startDate != null && !startDate.isEmpty()) sb.append("AND a.create_time >= CONCAT(#{startDate}, ' 00:00:00') ");
            if (endDate != null && !endDate.isEmpty()) sb.append("AND a.create_time <= CONCAT(#{endDate}, ' 23:59:59') ");
            sb.append("GROUP BY a.barcode, a.category_id, c.name, c.part_number ");
            sb.append("ORDER BY MAX(a.create_time) DESC");
            return sb.toString();
        }
    }
}