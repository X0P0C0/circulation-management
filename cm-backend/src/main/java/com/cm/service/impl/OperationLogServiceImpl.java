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
import org.springframework.util.StringUtils;

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
    public PageResult<OperationLog> listPage(String actionType, String startDate, String endDate,
                                              Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(actionType)) {
            wrapper.eq(OperationLog::getActionType, actionType);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> page = page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }
}