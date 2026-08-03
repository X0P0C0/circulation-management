package com.cm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cm.entity.OperationLog;
import com.cm.vo.OperationLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    @Select("<script>" +
        "SELECT ol.id, ol.action_type AS actionType, ol.content, " +
        "ol.accessory_id AS accessoryId, ol.accessory_ids AS accessoryIds, ol.related_category_id AS relatedCategoryId, " +
        "ol.related_worker_id AS relatedWorkerId, ol.operator, ol.ip, ol.create_time AS createTime, " +
        "a.barcode, a.item_code AS itemCode, " +
        "c.name AS categoryName, c.part_number AS partNumber, pc.name AS parentCategoryName, " +
        "w.name AS workerName " +
        "FROM operation_log ol " +
        "LEFT JOIN accessory a ON ol.accessory_id = a.id " +
        "LEFT JOIN category c ON ol.related_category_id = c.id LEFT JOIN category pc ON c.parent_id = pc.id " +
        "LEFT JOIN worker w ON ol.related_worker_id = w.id " +
        "WHERE 1=1 " +
        "<if test='actionType != null and actionType != \"\"'>AND ol.action_type = #{actionType}</if> " +
        "<if test='operator != null and operator != \"\"'>AND ol.operator LIKE CONCAT('%', #{operator}, '%')</if> " +
        "<if test='barcode != null and barcode != \"\"'>AND a.barcode LIKE CONCAT('%', #{barcode}, '%')</if> " +
        "<if test='startDate != null and startDate != \"\"'>AND ol.create_time &gt;= CONCAT(#{startDate}, ' 00:00:00')</if> " +
        "<if test='endDate != null and endDate != \"\"'>AND ol.create_time &lt;= CONCAT(#{endDate}, ' 23:59:59')</if> " +
        "ORDER BY ol.create_time DESC" +
        "</script>")
    List<OperationLogVO> selectLogWithJoin(@Param("actionType") String actionType,
                                           @Param("operator") String operator,
                                           @Param("barcode") String barcode,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);
}