package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.exception.BusinessException;
import com.cm.common.result.PageResult;
import com.cm.entity.*;
import com.cm.dto.FlowTransferDTO;
import com.cm.dto.FlowSellDTO;
import com.cm.enums.FlowTypeEnum;
import com.cm.mapper.*;
import com.cm.service.FlowService;
import com.cm.service.OperationLogService;
import com.cm.vo.FlowTraceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowServiceImpl implements FlowService {

    private final AccessoryMapper accessoryMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryOwnerMapper inventoryOwnerMapper;
    private final FlowRecordMapper flowRecordMapper;
    private final WorkerMapper workerMapper;
    private final OperationLogService operationLogService;
    private final CategoryMapper categoryMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferOut(FlowTransferDTO dto, String operator) {
        Worker worker = workerMapper.selectById(dto.getWorkerId());
        if (worker == null) throw new BusinessException(404, "师傅不存在");

        for (String barcode : dto.getBarcodes()) {
            Accessory acc = accessoryMapper.selectOne(
                    new LambdaQueryWrapper<Accessory>().eq(Accessory::getBarcode, barcode));
            if (acc == null) throw new BusinessException(404, "条码不存在：" + barcode);

            Inventory inv = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<Inventory>().eq(Inventory::getAccessoryId, acc.getId()));
            if (inv == null || inv.getAvailableQty() < 1) {
                throw new BusinessException(409, "库存不足：" + acc.getName() + "（" + barcode + "）");
            }

            inventoryMapper.update(null, new LambdaUpdateWrapper<Inventory>()
                    .eq(Inventory::getId, inv.getId())
                    .setSql("available_qty = available_qty - 1"));

            InventoryOwner owner = inventoryOwnerMapper.selectOne(
                    new LambdaQueryWrapper<InventoryOwner>()
                            .eq(InventoryOwner::getAccessoryId, acc.getId())
                            .eq(InventoryOwner::getWorkerId, dto.getWorkerId()));
            if (owner != null) {
                inventoryOwnerMapper.update(null, new LambdaUpdateWrapper<InventoryOwner>()
                        .eq(InventoryOwner::getId, owner.getId())
                        .setSql("qty = qty + 1"));
            } else {
                InventoryOwner newOwner = new InventoryOwner();
                newOwner.setAccessoryId(acc.getId());
                newOwner.setWorkerId(dto.getWorkerId());
                newOwner.setQty(1);
                inventoryOwnerMapper.insert(newOwner);
            }

            saveFlowRecord(acc, FlowTypeEnum.TRANSFER_OUT.getCode(), dto.getWorkerId(), worker.getName(), null, null, 1, dto.getRemark(), operator);
            operationLogService.log("TRANSFER_OUT", "配件领用：" + acc.getName() + "（" + barcode + "）→ " + worker.getName(),
                    barcode, dto.getWorkerId(), operator, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferIn(FlowTransferDTO dto, String operator) {
        Worker worker = workerMapper.selectById(dto.getWorkerId());
        if (worker == null) throw new BusinessException(404, "师傅不存在");

        for (String barcode : dto.getBarcodes()) {
            Accessory acc = accessoryMapper.selectOne(
                    new LambdaQueryWrapper<Accessory>().eq(Accessory::getBarcode, barcode));
            if (acc == null) throw new BusinessException(404, "条码不存在：" + barcode);

            InventoryOwner owner = inventoryOwnerMapper.selectOne(
                    new LambdaQueryWrapper<InventoryOwner>()
                            .eq(InventoryOwner::getAccessoryId, acc.getId())
                            .eq(InventoryOwner::getWorkerId, dto.getWorkerId()));
            if (owner == null || owner.getQty() < 1) {
                throw new BusinessException(409, "该师傅名下无此配件：" + acc.getName() + "（" + barcode + "）");
            }

            inventoryOwnerMapper.update(null, new LambdaUpdateWrapper<InventoryOwner>()
                    .eq(InventoryOwner::getId, owner.getId())
                    .setSql("qty = qty - 1"));

            inventoryMapper.update(null, new LambdaUpdateWrapper<Inventory>()
                    .eq(Inventory::getAccessoryId, acc.getId())
                    .setSql("available_qty = available_qty + 1"));

            saveFlowRecord(acc, FlowTypeEnum.TRANSFER_IN.getCode(), dto.getWorkerId(), worker.getName(), null, null, 1, dto.getRemark(), operator);
            operationLogService.log("TRANSFER_IN", "配件归还：" + acc.getName() + "（" + barcode + "）← " + worker.getName(),
                    barcode, dto.getWorkerId(), operator, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sell(FlowSellDTO dto, String operator) {
        Accessory acc = accessoryMapper.selectOne(
                new LambdaQueryWrapper<Accessory>().eq(Accessory::getBarcode, dto.getBarcode()));
        if (acc == null) throw new BusinessException(404, "条码不存在");

        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getAccessoryId, acc.getId()));
        if (inv == null || inv.getAvailableQty() < 1) {
            throw new BusinessException(409, "库存不足");
        }

        inventoryMapper.update(null, new LambdaUpdateWrapper<Inventory>()
                .eq(Inventory::getId, inv.getId())
                .setSql("available_qty = available_qty - 1"));

        FlowRecord record = new FlowRecord();
        record.setAccessoryId(acc.getId());
        record.setBarcode(acc.getBarcode());
        record.setAccessoryName(acc.getName());
        record.setFlowType(FlowTypeEnum.SELL.getCode());
        record.setCustomerName(dto.getCustomerName());
        record.setCustomerPhone(dto.getCustomerPhone());
        record.setQty(1);
        record.setRemark(dto.getRemark());
        record.setOperator(operator);
        flowRecordMapper.insert(record);

        String customerInfo = StringUtils.hasText(dto.getCustomerName()) ? " → 客户：" + dto.getCustomerName() : "";
        operationLogService.log("SELL", "配件售卖：" + acc.getName() + "（" + dto.getBarcode() + "）" + customerInfo,
                dto.getBarcode(), null, operator, null);
    }

    @Override
    public FlowTraceVO trace(String barcode) {
        Accessory acc = accessoryMapper.selectOne(
                new LambdaQueryWrapper<Accessory>().eq(Accessory::getBarcode, barcode));
        if (acc == null) throw new BusinessException(404, "条码不存在");

        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getAccessoryId, acc.getId()));

        List<FlowRecord> records = flowRecordMapper.selectList(
                new LambdaQueryWrapper<FlowRecord>()
                        .eq(FlowRecord::getBarcode, barcode)
                        .orderByDesc(FlowRecord::getCreateTime));

        FlowTraceVO vo = new FlowTraceVO();
        vo.setBarcode(barcode);
        vo.setAccessoryName(acc.getName());
        vo.setCurrentQty(inv != null ? inv.getAvailableQty() : 0);

        String currentHolder = "总库存";
        if (inv != null && inv.getAvailableQty() == 0 && !records.isEmpty()) {
            FlowRecord last = records.get(0);
            if (last.getFlowType() == FlowTypeEnum.SELL.getCode()) {
                currentHolder = "已售出";
            } else if (last.getFlowType() == FlowTypeEnum.TRANSFER_OUT.getCode()) {
                currentHolder = last.getWorkerName() + "（师傅）";
            }
        }
        vo.setCurrentHolder(currentHolder);

        List<FlowTraceVO.FlowStep> steps = records.stream().map(r -> {
            FlowTraceVO.FlowStep step = new FlowTraceVO.FlowStep();
            step.setId(r.getId());
            step.setFlowType(r.getFlowType());
            step.setFlowTypeDesc(FlowTypeEnum.of(r.getFlowType()).getDesc());
            step.setWorkerName(r.getWorkerName());
            step.setCustomerName(r.getCustomerName());
            step.setOperator(r.getOperator());
            step.setRemark(r.getRemark());
            step.setCreateTime(r.getCreateTime() != null ? r.getCreateTime().format(FMT) : "");
            return step;
        }).collect(Collectors.toList());
        vo.setSteps(steps);
        return vo;
    }

    @Override
    public PageResult<?> listRecords(Integer flowType, Long workerId, String keyword,
                                     String startDate, String endDate,
                                     Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<FlowRecord> wrapper = new LambdaQueryWrapper<>();
        if (flowType != null) wrapper.eq(FlowRecord::getFlowType, flowType);
        if (workerId != null) wrapper.eq(FlowRecord::getWorkerId, workerId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(FlowRecord::getBarcode, keyword)
                    .or().like(FlowRecord::getAccessoryName, keyword)
                    .or().like(FlowRecord::getWorkerName, keyword));
        }
        wrapper.orderByDesc(FlowRecord::getCreateTime);
        Page<FlowRecord> page = flowRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<?> workerRecords(Long workerId) {
        return flowRecordMapper.selectList(
                new LambdaQueryWrapper<FlowRecord>()
                        .eq(FlowRecord::getWorkerId, workerId)
                        .orderByDesc(FlowRecord::getCreateTime));
    }

    private void saveFlowRecord(Accessory acc, int flowType, Long workerId, String workerName,
                                String customerName, String customerPhone, int qty, String remark, String operator) {
        FlowRecord record = new FlowRecord();
        record.setAccessoryId(acc.getId());
        record.setBarcode(acc.getBarcode());
        record.setAccessoryName(acc.getName());
        record.setFlowType(flowType);
        record.setWorkerId(workerId);
        record.setWorkerName(workerName);
        record.setCustomerName(customerName);
        record.setCustomerPhone(customerPhone);
        record.setQty(qty);
        record.setRemark(remark);
        record.setOperator(operator);
        flowRecordMapper.insert(record);
    }
}