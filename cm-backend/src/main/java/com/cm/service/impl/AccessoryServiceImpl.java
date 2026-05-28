package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.exception.BusinessException;
import com.cm.common.result.PageResult;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.entity.Accessory;
import com.cm.enums.AccessoryStatusEnum;
import com.cm.mapper.AccessoryMapper;
import com.cm.mapper.CategoryMapper;
import com.cm.mapper.WorkerMapper;
import com.cm.service.AccessoryService;
import com.cm.vo.AccessoryVO;
import com.cm.vo.InventoryGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessoryServiceImpl extends ServiceImpl<AccessoryMapper, Accessory> implements AccessoryService {

    private final CategoryMapper categoryMapper;
    private final WorkerMapper workerMapper;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessoryVO inbound(AccessoryInboundDTO dto, String operator) {
        Accessory acc = new Accessory();
        acc.setBarcode(dto.getBarcode());
        acc.setItemCode(generateItemCode());
        acc.setCategoryId(dto.getCategoryId());
        acc.setRemark(dto.getRemark());
        acc.setStatus(AccessoryStatusEnum.IN_STOCK.getCode());
        acc.setOperator(operator);
        save(acc);
        return toVO(acc);
    }

    @Override
    public AccessoryVO findById(Long id) {
        Accessory acc = getById(id);
        if (acc == null) throw new BusinessException(404, "工件不存在");
        return toVO(acc);
    }

    @Override
    public AccessoryVO findByBarcode(String barcode) {
        // 返回该条码下第一个在库的工件（用于扫码场景）
        Accessory acc = getOne(new LambdaQueryWrapper<Accessory>()
                .eq(Accessory::getBarcode, barcode)
                .eq(Accessory::getStatus, AccessoryStatusEnum.IN_STOCK.getCode())
                .last("LIMIT 1"));
        if (acc == null) throw new BusinessException(404, "未找到该条码的在库工件");
        return toVO(acc);
    }

    @Override
    public PageResult<AccessoryVO> search(String barcode, Long categoryId, Integer status,
                                            Long workerId, String keyword,
                                            String sortField, String sortOrder,
                                            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Accessory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(barcode)) wrapper.eq(Accessory::getBarcode, barcode);
        if (categoryId != null) wrapper.eq(Accessory::getCategoryId, categoryId);
        if (status != null) wrapper.eq(Accessory::getStatus, status);
        if (workerId != null) wrapper.eq(Accessory::getWorkerId, workerId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Accessory::getBarcode, keyword)
                    .or().like(Accessory::getItemCode, keyword)
                    .or().like(Accessory::getRemark, keyword));
        }
        // 排序
        if (StringUtils.hasText(sortField) && StringUtils.hasText(sortOrder)) {
            boolean asc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "barcode" -> wrapper.orderBy(true, asc, Accessory::getBarcode);
                case "itemCode" -> wrapper.orderBy(true, asc, Accessory::getItemCode);
                case "createTime" -> wrapper.orderBy(true, asc, Accessory::getCreateTime);
                case "status" -> wrapper.orderBy(true, asc, Accessory::getStatus);
                default -> wrapper.orderByDesc(Accessory::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Accessory::getCreateTime);
        }

        Page<Accessory> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<AccessoryVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<InventoryGroupVO> inventoryGroup(String barcode, Long categoryId,
                                                         Integer statusFilter, Long workerId,
                                                         Integer pageNum, Integer pageSize) {
        // 使用Mapper自定义查询做分组统计
        // 简化实现：用Java层面做分组（数据量小可接受）
        LambdaQueryWrapper<Accessory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(barcode)) wrapper.like(Accessory::getBarcode, barcode);
        if (categoryId != null) wrapper.eq(Accessory::getCategoryId, categoryId);
        if (statusFilter != null) wrapper.eq(Accessory::getStatus, statusFilter);
        if (workerId != null) wrapper.eq(Accessory::getWorkerId, workerId);

        List<Accessory> all = list(wrapper);

        // 按barcode分组统计
        var grouped = all.stream().collect(Collectors.groupingBy(Accessory::getBarcode));
        List<InventoryGroupVO> groups = grouped.entrySet().stream().map(entry -> {
            InventoryGroupVO vo = new InventoryGroupVO();
            vo.setBarcode(entry.getKey());
            List<Accessory> items = entry.getValue();
            vo.setTotalCount(items.size());
            vo.setAvailableCount((int) items.stream()
                    .filter(a -> a.getStatus() == AccessoryStatusEnum.IN_STOCK.getCode()).count());
            if (!items.isEmpty()) {
                vo.setCategoryId(items.get(0).getCategoryId());
                if (items.get(0).getCategoryId() != null) {
                    var cat = categoryMapper.selectById(items.get(0).getCategoryId());
                    if (cat != null) vo.setCategoryName(cat.getName());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        // 分页
        int total = groups.size();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(from + pageSize, total);
        List<InventoryGroupVO> pageData = from < total ? groups.subList(from, to) : List.of();
        return new PageResult<>(pageData, (long) total, pageNum, pageSize);
    }

    /** 生成工件编号：AC + yyyyMMdd + - + NNN */
    private String generateItemCode() {
        String dateStr = LocalDate.now().format(DATE_FMT);
        String prefix = "AC" + dateStr + "-";

        // 查找今天最大的编号
        String maxCode = baseMapper.selectMaxItemCodeByPrefix(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.contains("-")) {
            try {
                seq = Integer.parseInt(maxCode.substring(maxCode.lastIndexOf("-") + 1)) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return prefix + String.format("%03d", seq);
    }

    private AccessoryVO toVO(Accessory acc) {
        AccessoryVO vo = new AccessoryVO();
        vo.setId(acc.getId());
        vo.setBarcode(acc.getBarcode());
        vo.setItemCode(acc.getItemCode());
        vo.setCategoryId(acc.getCategoryId());
        vo.setRemark(acc.getRemark());
        vo.setStatus(acc.getStatus());
        vo.setStatusDesc(AccessoryStatusEnum.of(acc.getStatus()).getDesc());
        vo.setWorkerId(acc.getWorkerId());
        vo.setRelatedItemCode(acc.getRelatedItemCode());
        vo.setIsHighValue(acc.getIsHighValue());
        vo.setCreateTime(acc.getCreateTime());

        if (acc.getCategoryId() != null) {
            var cat = categoryMapper.selectById(acc.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        if (acc.getWorkerId() != null) {
            var worker = workerMapper.selectById(acc.getWorkerId());
            if (worker != null) vo.setWorkerName(worker.getName());
        }
        return vo;
    }
}