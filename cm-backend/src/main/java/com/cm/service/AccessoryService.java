package com.cm.service;

import com.cm.common.result.PageResult;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.vo.AccessoryVO;
import com.cm.vo.InventoryGroupVO;
import java.util.List;

public interface AccessoryService {
    /** 入库：创建工件，自动生成工件编号 */
    AccessoryVO inbound(AccessoryInboundDTO dto, String operator);

    /** 按ID查询工件 */
    AccessoryVO findById(Long id);

    /** 按条码查询工件（用于扫码） */
    AccessoryVO findByBarcode(String barcode);

    /** 统一搜索接口 */
    PageResult<AccessoryVO> search(String barcode, Long categoryId, Integer status,
                                    Long workerId, String keyword,
                                    String sortField, String sortOrder,
                                    Integer pageNum, Integer pageSize);

    /** 按条码分组的库存视图 */
    PageResult<InventoryGroupVO> inventoryGroup(String barcode, Long categoryId,
                                                  Integer statusFilter, Long workerId,
                                                  Integer pageNum, Integer pageSize);
}