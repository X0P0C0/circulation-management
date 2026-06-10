package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.result.PageResult;
import com.cm.entity.OperationLog;
import com.cm.mapper.OperationLogMapper;
import com.cm.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.cm.common.util.SortUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void log(String actionType, String content, String barcode, Long workerId, String operator, String ip) {
        OperationLog log = new OperationLog();
        log.setActionType(actionType);
        log.setContent(content);
        log.setRelatedBarcode(barcode);
        log.setRelatedWorkerId(workerId);
        log.setOperator(operator);
        log.setIp(ip);
        save(log);
    }

    @Override
    public PageResult<OperationLog> listPage(String actionType, String operator, String barcode,
                                              String startDate, String endDate,
                                              String sortFields, String sortOrders,
                                              Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(actionType)) {
            wrapper.eq(OperationLog::getActionType, actionType);
        }
        if (StringUtils.hasText(operator)) {
            wrapper.like(OperationLog::getOperator, operator);
        }
        if (StringUtils.hasText(barcode)) {
            wrapper.like(OperationLog::getRelatedBarcode, barcode);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(OperationLog::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(OperationLog::getCreateTime, endDate + " 23:59:59");
        }
        Map<String, SFunction<OperationLog, ?>> sortMapper = new HashMap<>();
        sortMapper.put("actionType", OperationLog::getActionType);
        sortMapper.put("content", OperationLog::getContent);
        sortMapper.put("relatedBarcode", OperationLog::getRelatedBarcode);
        sortMapper.put("operator", OperationLog::getOperator);
        sortMapper.put("createTime", OperationLog::getCreateTime);
        SortUtils.applyMultiSort(wrapper, sortFields, sortOrders, sortMapper, OperationLog::getCreateTime, false);
        Page<OperationLog> page = page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }
}