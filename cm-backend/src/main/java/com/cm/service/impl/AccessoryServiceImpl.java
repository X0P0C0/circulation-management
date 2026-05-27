package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.exception.BusinessException;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.entity.Accessory;
import com.cm.entity.Inventory;
import com.cm.mapper.AccessoryMapper;
import com.cm.mapper.InventoryMapper;
import com.cm.service.AccessoryService;
import com.cm.service.OperationLogService;
import com.cm.vo.AccessoryVO;
import com.cm.entity.Category;
import com.cm.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessoryServiceImpl extends ServiceImpl<AccessoryMapper, Accessory> implements AccessoryService {

    private final InventoryMapper inventoryMapper;
    private final CategoryMapper categoryMapper;
    private final OperationLogService operationLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inbound(AccessoryInboundDTO dto, String operator) {
        Accessory existing = getOne(new LambdaQueryWrapper<Accessory>()
                .eq(Accessory::getBarcode, dto.getBarcode()));
        if (existing != null) {
            throw new BusinessException(409, "该条码已入库，配件名称：" + existing.getName());
        }

        Accessory accessory = new Accessory();
        BeanUtils.copyProperties(dto, accessory);
        save(accessory);

        Inventory inventory = new Inventory();
        inventory.setAccessoryId(accessory.getId());
        inventory.setTotalQty(1);
        inventory.setAvailableQty(1);
        inventoryMapper.insert(inventory);

        operationLogService.log("INBOUND", "配件入库：" + dto.getName() + "（" + dto.getBarcode() + "）",
                dto.getBarcode(), null, operator, null);
    }

    @Override
    public AccessoryVO findByBarcode(String barcode) {
        Accessory accessory = getOne(new LambdaQueryWrapper<Accessory>()
                .eq(Accessory::getBarcode, barcode));
        if (accessory == null) {
            throw new BusinessException(404, "条码不存在");
        }

        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getAccessoryId, accessory.getId()));

        AccessoryVO vo = new AccessoryVO();
        BeanUtils.copyProperties(accessory, vo);
        if (inventory != null) {
            vo.setAvailableQty(inventory.getAvailableQty());
        }
        if (accessory.getCategoryId() != null) {
            Category category = categoryMapper.selectById(accessory.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }
}