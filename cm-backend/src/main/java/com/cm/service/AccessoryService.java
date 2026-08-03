package com.cm.service;

import com.cm.common.result.PageResult;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.dto.AccessoryUpdateDTO;
import com.cm.vo.AccessoryVO;
import com.cm.vo.InventoryGroupVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface AccessoryService {
    /** 入库：创建工件，自动生成工件编号，返回所有创建的工件 */
    List<AccessoryVO> inbound(AccessoryInboundDTO dto, String operator);
    AccessoryVO findById(Long id);
    List<AccessoryVO> findByIds(List<Long> ids);
    List<AccessoryVO> findByItemCodes(List<String> itemCodes);
    AccessoryVO findByBarcode(String barcode);
    List<AccessoryVO> importFromCsv(MultipartFile file, Long categoryId, String remark, String operator);
    PageResult<AccessoryVO> search(String barcode, String exactBarcode, Long categoryId, Integer status, Integer statusNot,
                                    Long workerId, String keyword,
                                    String operator, String remark, Integer highValue, Long shelfId,
                                    String startDate, String endDate,
                                    String sortFields, String sortOrders,
                                    Integer pageNum, Integer pageSize);
    void deleteById(Long id, String operator);
    AccessoryVO update(AccessoryUpdateDTO dto, String operator);
    PageResult<InventoryGroupVO> inventoryGroup(String barcode, Long categoryId,
                                                  Integer statusFilter, Long workerId,
                                                  String remark, Integer highValue, Long shelfId,
                                                  String startDate, String endDate,
                                                  Integer pageNum, Integer pageSize);
        Map<String, Object> importFromExcel(MultipartFile file, String operator);
}

