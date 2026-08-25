package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cm.common.exception.BusinessException;
import com.cm.common.result.PageResult;
import com.cm.dto.*;
import com.cm.entity.Accessory;
import com.cm.entity.FlowRecord;
import com.cm.entity.Worker;
import com.cm.enums.AccessoryStatusEnum;
import com.cm.enums.FlowTypeEnum;
import com.cm.mapper.AccessoryMapper;
import com.cm.mapper.CategoryMapper;
import com.cm.mapper.FlowRecordMapper;
import com.cm.mapper.WorkerMapper;
import com.cm.service.FlowService;
import com.cm.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cm.vo.FlowTraceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowServiceImpl implements FlowService {

    private final AccessoryMapper accessoryMapper;
    private final FlowRecordMapper flowRecordMapper;
    private final WorkerMapper workerMapper;
    private final CategoryMapper categoryMapper;
    private final OperationLogService operationLogService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outbound(FlowOutboundDTO dto, String operator) {
        Worker worker = workerMapper.selectById(dto.getWorkerId());
        if (worker == null) throw new BusinessException(404, "师傅不存在");

        for (Long accId : dto.getAccessoryIds()) {
            Accessory acc = accessoryMapper.selectById(accId);
            if (acc == null) throw new BusinessException(404, "工件不存在：" + accId);
            if (acc.getStatus() != AccessoryStatusEnum.IN_STOCK.getCode()) {
                throw new BusinessException(400, "工件 " + acc.getItemCode() + " 不在可支配库中");
            }

            acc.setStatus(AccessoryStatusEnum.OUTBOUND.getCode());
            acc.setWorkerId(dto.getWorkerId());
            accessoryMapper.updateById(acc);

            saveFlowRecord(acc, FlowTypeEnum.OUTBOUND.getCode(),
                    null, null, dto.getWorkerId(), worker.getName(), null, null, null, dto.getRemark(), operator);
        }

        operationLogService.log("OUTBOUND", "出库 " + dto.getAccessoryIds().size() + " 个工件给 " + worker.getName(),
                null, toJsonArray(dto.getAccessoryIds()), null, dto.getWorkerId(), operator, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnItem(FlowReturnDTO dto, String operator) {
        for (Long accId : dto.getAccessoryIds()) {
            Accessory acc = accessoryMapper.selectById(accId);
            if (acc == null) throw new BusinessException(404, "工件不存在：" + accId);
            if (acc.getStatus() != AccessoryStatusEnum.OUTBOUND.getCode()) {
                throw new BusinessException(400, "工件 " + acc.getItemCode() + " 不在师傅手中");
            }

            Long fromWorkerId = acc.getWorkerId();
            String fromWorkerName = null;
            if (fromWorkerId != null) {
                Worker w = workerMapper.selectById(fromWorkerId);
                if (w != null) fromWorkerName = w.getName();
            }

            if (dto.getReturnType() == 1) {
                accessoryMapper.update(null, new LambdaUpdateWrapper<Accessory>()
                        .eq(Accessory::getId, acc.getId())
                        .set(Accessory::getStatus, AccessoryStatusEnum.COMPLETED.getCode())
                        .set(Accessory::getWorkerId, null));

                saveFlowRecord(acc, FlowTypeEnum.RETURN_COMPLETE.getCode(),
                        fromWorkerId, fromWorkerName, null, null, null, null, null, dto.getRemark(), operator);
            } else {
                if (Boolean.TRUE.equals(dto.getHighValue())) {
                    accessoryMapper.update(null, new LambdaUpdateWrapper<Accessory>()
                            .eq(Accessory::getId, acc.getId())
                            .set(Accessory::getStatus, AccessoryStatusEnum.RETURNED_FACTORY.getCode())
                            .set(Accessory::getIsHighValue, 1)
                            .set(Accessory::getWorkerId, null));

                    saveFlowRecord(acc, FlowTypeEnum.RETURN_CANCEL_HIGH.getCode(),
                            fromWorkerId, fromWorkerName, null, null, null, null, null, dto.getRemark(), operator);
                } else {
                    accessoryMapper.update(null, new LambdaUpdateWrapper<Accessory>()
                            .eq(Accessory::getId, acc.getId())
                            .set(Accessory::getStatus, AccessoryStatusEnum.IN_STOCK.getCode())
                            .set(Accessory::getWorkerId, null));

                    saveFlowRecord(acc, FlowTypeEnum.RETURN_CANCEL_LOW.getCode(),
                            fromWorkerId, fromWorkerName, null, null, null, null, null, dto.getRemark(), operator);
                }
            }
        }

        operationLogService.log("RETURN", "归还 " + dto.getAccessoryIds().size() + " 个工件",
                null, toJsonArray(dto.getAccessoryIds()), null, null, operator, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sell(FlowSellDTO dto, String operator) {
        for (SellItemDTO item : dto.getItems()) {
            Long accId = item.getAccessoryId();
            Accessory acc = accessoryMapper.selectById(accId);
            if (acc == null) throw new BusinessException(404, "工件不存在：" + accId);
            if (acc.getStatus() != AccessoryStatusEnum.IN_STOCK.getCode()) {
                throw new BusinessException(400, "工件 " + acc.getItemCode() + " 不在可支配库中");
            }

            accessoryMapper.update(null, new LambdaUpdateWrapper<Accessory>()
                    .eq(Accessory::getId, acc.getId())
                    .set(Accessory::getStatus, AccessoryStatusEnum.SOLD.getCode())
                    .set(Accessory::getWorkerId, null));

            saveFlowRecord(acc, FlowTypeEnum.SELL.getCode(),
                    null, null, null, null, item.getPrice(), dto.getCustomerName(), dto.getCustomerPhone(), dto.getRemark(), operator);
        }

        String customerInfo = StringUtils.hasText(dto.getCustomerName()) ? " → " + dto.getCustomerName() : "";
        operationLogService.log("SELL", "售卖 " + dto.getItems().size() + " 个工件" + customerInfo,
                null, toJsonArray(dto.getItems().stream().map(i -> i.getAccessoryId()).collect(java.util.stream.Collectors.toList())), null, null, operator, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(FlowTransferDTO dto, String operator) {
        Worker fromWorker = workerMapper.selectById(dto.getFromWorkerId());
        if (fromWorker == null) throw new BusinessException(404, "源师傅不存在");
        Worker toWorker = workerMapper.selectById(dto.getToWorkerId());
        if (toWorker == null) throw new BusinessException(404, "目标师傅不存在");

        for (Long accId : dto.getAccessoryIds()) {
            Accessory acc = accessoryMapper.selectById(accId);
            if (acc == null) throw new BusinessException(404, "工件不存在：" + accId);
            if (!dto.getFromWorkerId().equals(acc.getWorkerId())) {
                throw new BusinessException(400, "工件 " + acc.getItemCode() + " 不在该师傅手中");
            }

            acc.setWorkerId(dto.getToWorkerId());
            accessoryMapper.updateById(acc);

            saveFlowRecord(acc, FlowTypeEnum.TRANSFER.getCode(),
                    dto.getFromWorkerId(), fromWorker.getName(),
                    dto.getToWorkerId(), toWorker.getName(),
                    null, null, null, dto.getRemark(), operator);
        }

        operationLogService.log("TRANSFER", "转移 " + dto.getAccessoryIds().size() + " 个工件：" +
                fromWorker.getName() + " → " + toWorker.getName(), null, toJsonArray(dto.getAccessoryIds()), null, null, operator, null);
    }

    @Override
    public FlowTraceVO trace(String itemCode) {
        Accessory acc = accessoryMapper.selectOne(
                new LambdaQueryWrapper<Accessory>().eq(Accessory::getItemCode, itemCode));
        if (acc == null) throw new BusinessException(404, "工件编号不存在");
        return buildTrace(acc);
    }

    @Override
    public List<FlowTraceVO> traceByBarcode(String barcode) {
        // 条码 = 分类专用号，同分类的多个工件共用同一条码，不是唯一键，
        // 因此返回该条码对应的全部工件追溯记录，由前端展示多个并切换查看
        List<Accessory> list = accessoryMapper.selectList(
                new LambdaQueryWrapper<Accessory>()
                        .eq(Accessory::getBarcode, barcode)
                        .orderByAsc(Accessory::getDeleted)
                        .orderByDesc(Accessory::getId));
        return list.stream().map(this::buildTrace).collect(Collectors.toList());
    }

    private FlowTraceVO buildTrace(Accessory acc) {
        String itemCode = acc.getItemCode();
        FlowTraceVO vo = new FlowTraceVO();
        vo.setItemCode(itemCode);
        vo.setBarcode(acc.getBarcode());
        vo.setCategoryId(acc.getCategoryId());
        if (acc.getDeleted() != null && acc.getDeleted() == 1) {
            vo.setCurrentStatus(-1);
            vo.setCurrentStatusDesc("已删除");
            vo.setCurrentHolder("已删除");
            vo.setDeleted(1);
        } else {
            vo.setCurrentStatus(acc.getStatus());
            vo.setCurrentStatusDesc(AccessoryStatusEnum.of(acc.getStatus()).getDesc());
            if (acc.getWorkerId() != null) {
                Worker w = workerMapper.selectById(acc.getWorkerId());
                vo.setCurrentHolder(w != null ? w.getName() + "（师傅）" : "未知师傅");
            } else {
                vo.setCurrentHolder(AccessoryStatusEnum.of(acc.getStatus()).getDesc());
            }
        }

        if (acc.getCategoryId() != null) {
            var cat = categoryMapper.selectById(acc.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
        }

        List<FlowRecord> records = flowRecordMapper.selectList(
                new LambdaQueryWrapper<FlowRecord>()
                        .eq(FlowRecord::getItemCode, itemCode)
                        .orderByDesc(FlowRecord::getCreateTime));

        vo.setSteps(records.stream().map(r -> {
            FlowTraceVO.FlowStep step = new FlowTraceVO.FlowStep();
            step.setId(r.getId());
            step.setFlowType(r.getFlowType());
            step.setFlowTypeDesc(FlowTypeEnum.of(r.getFlowType()).getDesc());
            step.setFromWorkerName(r.getFromWorkerName());
            step.setToWorkerName(r.getToWorkerName());
            step.setPrice(r.getPrice());
            step.setCustomerName(r.getCustomerName());
            step.setOperator(r.getOperator());
            step.setRemark(r.getRemark());
            step.setCreateTime(r.getCreateTime() != null ? r.getCreateTime().format(FMT) : "");
            return step;
        }).collect(Collectors.toList()));

        return vo;
    }

    @Override
    public PageResult<?> listRecords(Integer flowType, Long workerId, String keyword,
                                     Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<FlowRecord> wrapper = new LambdaQueryWrapper<>();
        if (flowType != null) wrapper.eq(FlowRecord::getFlowType, flowType);
        if (workerId != null) {
            wrapper.and(w -> w.eq(FlowRecord::getToWorkerId, workerId)
                    .or().eq(FlowRecord::getFromWorkerId, workerId));
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(FlowRecord::getItemCode, keyword)
                    .or().like(FlowRecord::getBarcode, keyword));
        }
        wrapper.orderByDesc(FlowRecord::getCreateTime);
        Page<FlowRecord> page = flowRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    private void saveFlowRecord(Accessory acc, int flowType,
                                Long fromWorkerId, String fromWorkerName,
                                Long toWorkerId, String toWorkerName,
                                BigDecimal price,
                                String customerName, String customerPhone,
                                String remark, String operator) {
        FlowRecord record = new FlowRecord();
        record.setAccessoryId(acc.getId());
        record.setItemCode(acc.getItemCode());
        record.setBarcode(acc.getBarcode());
        record.setFlowType(flowType);
        record.setFromWorkerId(fromWorkerId);
        record.setFromWorkerName(fromWorkerName);
        record.setToWorkerId(toWorkerId);
        record.setToWorkerName(toWorkerName);
        record.setPrice(price);
        record.setCustomerName(customerName);
        record.setCustomerPhone(customerPhone);
        record.setRemark(remark);
        record.setOperator(operator);
        flowRecordMapper.insert(record);
    }

    private String toJsonArray(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return ids.toString();
    }
}