package com.cm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cm.common.result.PageResult;
import com.cm.entity.Inventory;
import com.cm.vo.InventoryVO;
import java.util.List;
import java.util.Map;

public interface InventoryService extends IService<Inventory> {
    PageResult<InventoryVO> listPage(Long categoryId, String keyword, Integer pageNum, Integer pageSize);
    Map<String, Object> getStats();
    List<InventoryVO> getWorkerInventory(Long workerId);
}