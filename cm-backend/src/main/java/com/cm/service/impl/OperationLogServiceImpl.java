package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.result.PageResult;
import com.cm.entity.OperationLog;
import com.cm.vo.OperationLogVO;
import com.cm.mapper.OperationLogMapper;
import com.cm.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.cm.common.util.SortUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void log(String actionType, String content, Long accessoryId, String accessoryIds, Long categoryId, Long workerId, String operator, String ip) {
        OperationLog log = new OperationLog();
        log.setActionType(actionType);
        log.setContent(content);
        log.setAccessoryId(accessoryId);
        log.setAccessoryIds(accessoryIds);
        log.setRelatedCategoryId(categoryId);
        log.setRelatedWorkerId(workerId);
        log.setOperator(operator);
        log.setIp(ip);
        save(log);
    }

    @Override
    public PageResult<OperationLogVO> listPage(String actionType, String operator, String barcode,
                                              String startDate, String endDate,
                                              String sortFields, String sortOrders,
                                              Integer pageNum, Integer pageSize) {
        List<OperationLogVO> all = baseMapper.selectLogWithJoin(actionType, operator, barcode, startDate, endDate);
        int total = all.size();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(from + pageSize, total);
        List<OperationLogVO> pageData = from < total ? all.subList(from, to) : List.of();
        return new PageResult<>(pageData, (long) total, pageNum, pageSize);
    }
}