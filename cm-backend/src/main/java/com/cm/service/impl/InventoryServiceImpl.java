package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.result.PageResult;
import com.cm.entity.*;
import com.cm.mapper.*;
import com.cm.service.InventoryService;
import com.cm.vo.InventoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final AccessoryMapper accessoryMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<InventoryVO> listPage(Long categoryId, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Inventory::getAvailableQty, 0);

        if (categoryId != null) {
            List<Accessory> accList = accessoryMapper.selectList(
                    new LambdaQueryWrapper<Accessory>().eq(Accessory::getCategoryId, categoryId));
            if (accList.isEmpty()) return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize);
            List<Long> accIds = accList.stream().map(Accessory::getId).collect(Collectors.toList());
            wrapper.in(Inventory::getAccessoryId, accIds);
        }

        wrapper.orderByDesc(Inventory::getUpdateTime);
        Page<Inventory> page = page(new Page<>(pageNum, pageSize), wrapper);

        List<InventoryVO> records = page.getRecords().stream().map(inv -> {
            Accessory acc = accessoryMapper.selectById(inv.getAccessoryId());
            InventoryVO vo = new InventoryVO();
            vo.setAccessoryId(inv.getAccessoryId());
            if (acc != null) {
                vo.setBarcode(acc.getBarcode());
                vo.setAccessoryName(acc.getName());
                vo.setSpec(acc.getSpec());
                vo.setUnit(acc.getUnit());
                if (acc.getCategoryId() != null) {
                    Category cat = categoryMapper.selectById(acc.getCategoryId());
                    vo.setCategoryName(cat != null ? cat.getName() : "");
                }
            }
            vo.setTotalQty(inv.getTotalQty());
            vo.setAvailableQty(inv.getAvailableQty());
            return vo;
        }).collect(Collectors.toList());

        if (StringUtils.hasText(keyword)) {
            final String kw = keyword.toLowerCase();
            records = records.stream()
                    .filter(r -> (r.getAccessoryName() != null && r.getAccessoryName().toLowerCase().contains(kw))
                            || (r.getBarcode() != null && r.getBarcode().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalAccessories = accessoryMapper.selectCount(new LambdaQueryWrapper<>());
        long totalCategories = categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1));
        List<Inventory> allInv = list(new LambdaQueryWrapper<>());
        long totalQty = allInv.stream().mapToLong(Inventory::getTotalQty).sum();
        long availableQty = allInv.stream().mapToLong(Inventory::getAvailableQty).sum();
        stats.put("totalAccessories", totalAccessories);
        stats.put("totalCategories", totalCategories);
        stats.put("totalQty", totalQty);
        stats.put("availableQty", availableQty);
        stats.put("outQty", totalQty - availableQty);
        return stats;
    }
}