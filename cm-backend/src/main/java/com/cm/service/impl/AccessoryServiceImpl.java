package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.exception.BusinessException;
import com.cm.common.result.PageResult;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.dto.AccessoryUpdateDTO;
import com.cm.entity.Accessory;
import com.cm.entity.FlowRecord;
import com.cm.enums.FlowTypeEnum;
import com.cm.mapper.FlowRecordMapper;
import com.cm.entity.Category;
import com.cm.entity.Worker;
import com.cm.enums.AccessoryStatusEnum;
import com.cm.mapper.AccessoryMapper;
import com.cm.mapper.CategoryMapper;
import com.cm.mapper.ShelfMapper;
import com.cm.mapper.WorkerMapper;
import com.cm.service.AccessoryService;
import com.cm.service.OperationLogService;
import com.cm.vo.AccessoryVO;
import com.cm.vo.InventoryGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.IService;

@Service
@RequiredArgsConstructor
public class AccessoryServiceImpl extends ServiceImpl<AccessoryMapper, Accessory> implements AccessoryService {

    private final CategoryMapper categoryMapper;
    private final WorkerMapper workerMapper;
    private final ShelfMapper shelfMapper;
    private final OperationLogService operationLogService;
    private final FlowRecordMapper flowRecordMapper;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AccessoryVO> inbound(AccessoryInboundDTO dto, String operator) {
        int qty = dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1;
        List<AccessoryVO> result = new ArrayList<>();
        for (int i = 0; i < qty; i++) {
            Accessory acc = new Accessory();
            acc.setBarcode(dto.getBarcode());
            acc.setItemCode(generateItemCode());
            acc.setCategoryId(dto.getCategoryId());
            acc.setShelfId(dto.getShelfId());
            acc.setRemark(dto.getRemark());
            acc.setStatus(AccessoryStatusEnum.IN_STOCK.getCode());
            acc.setOperator(operator);
            acc.setVersion(0);
            save(acc);
            // 创建入库流转记录
            FlowRecord fr = new FlowRecord();
            fr.setAccessoryId(acc.getId());
            fr.setItemCode(acc.getItemCode());
            fr.setBarcode(acc.getBarcode() != null ? acc.getBarcode() : "");
            fr.setFlowType(FlowTypeEnum.INBOUND.getCode());
            fr.setOperator(operator);
            flowRecordMapper.insert(fr);
            result.add(toVO(acc));
        }
        if (!result.isEmpty()) {
            List<Long> inIds = result.stream().map(AccessoryVO::getId).collect(Collectors.toList());
            operationLogService.log("INBOUND", "入库 " + result.size() + " 个工件", null, inIds.toString(), dto.getCategoryId(), null, operator, null);
        }
        return result;
    }
    @Override
    public void deleteById(Long id, String operator) {
        Accessory acc = getById(id);
        if (acc == null) {
            throw new BusinessException(404, "工件不存在");
        }
        if (acc.getStatus() != AccessoryStatusEnum.IN_STOCK.getCode()) {
            throw new BusinessException(400, "只有在库工件才能删除");
        }
        String delBarcode = acc.getBarcode();
        // 创建删除流转记录
        FlowRecord fr = new FlowRecord();
        fr.setAccessoryId(acc.getId());
        fr.setItemCode(acc.getItemCode());
        fr.setBarcode(delBarcode != null ? delBarcode : "");
        fr.setFlowType(FlowTypeEnum.DELETE.getCode());
        fr.setOperator(operator);
        flowRecordMapper.insert(fr);
        // 记录操作日志
        operationLogService.log("DELETE", "删除工件", acc.getId(), null, acc.getCategoryId(), null, operator, null);
        acc.setDeleted(1);
        updateById(acc);
    }

    @Override
    public AccessoryVO update(AccessoryUpdateDTO dto, String operator) {
        Accessory acc = getById(dto.getId());
        if (acc == null) {
            throw new BusinessException(404, "工件不存在");
        }
        if (dto.getBarcode() != null) acc.setBarcode(dto.getBarcode());
        if (dto.getCategoryId() != null) acc.setCategoryId(dto.getCategoryId());
        if (dto.getRemark() != null) acc.setRemark(dto.getRemark());
        if (dto.getIsHighValue() != null) acc.setIsHighValue(dto.getIsHighValue());
        if (dto.getShelfId() != null) acc.setShelfId(dto.getShelfId());
        updateById(acc);
        operationLogService.log("UPDATE", "编辑工件", acc.getId(), null, acc.getCategoryId(), null, operator, null);
        return toVO(acc);
    }

    @Override
    public AccessoryVO findById(Long id) {
        Accessory acc = getById(id);
        if (acc == null) throw new BusinessException(404, "工件不存在");
        return toVO(acc);
    }

    @Override
    public List<AccessoryVO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Accessory> list = listByIds(ids);
        return toVOList(list);
    }

    @Override
    public List<AccessoryVO> findByItemCodes(List<String> itemCodes) {
        if (itemCodes == null || itemCodes.isEmpty()) return Collections.emptyList();
        List<Accessory> list = list(new LambdaQueryWrapper<Accessory>().in(Accessory::getItemCode, itemCodes));
        return toVOList(list);
    }

    @Override
    public AccessoryVO findByBarcode(String barcode) {
        Accessory acc = getOne(new LambdaQueryWrapper<Accessory>()
                .eq(Accessory::getBarcode, barcode)
                .eq(Accessory::getDeleted, 0)
                .eq(Accessory::getStatus, AccessoryStatusEnum.IN_STOCK.getCode())
                .last("LIMIT 1"));
        if (acc == null) throw new BusinessException(404, "未找到该条码的在库工件");
        return toVO(acc);
    }

    @Override
    public PageResult<AccessoryVO> search(String barcode, String exactBarcode, Long categoryId, Integer status, Integer statusNot,
                                            Long workerId, String keyword,
                                            String operator, String remark, Integer highValue, Long shelfId,
                                            String startDate, String endDate,
                                            String sortFields, String sortOrders,
                                            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Accessory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Accessory::getDeleted, 0);
        if (StringUtils.hasText(exactBarcode)) wrapper.eq(Accessory::getBarcode, exactBarcode);
        else if (StringUtils.hasText(barcode)) wrapper.like(Accessory::getBarcode, barcode);
        if (categoryId != null) wrapper.eq(Accessory::getCategoryId, categoryId);
        if (status != null) wrapper.eq(Accessory::getStatus, status);
        if (statusNot != null) wrapper.ne(Accessory::getStatus, statusNot);
        if (workerId != null) wrapper.eq(Accessory::getWorkerId, workerId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Accessory::getBarcode, keyword)
                    .or().like(Accessory::getItemCode, keyword)
                    .or().like(Accessory::getRemark, keyword));
        }
        if (StringUtils.hasText(operator)) wrapper.like(Accessory::getOperator, operator);
        if (StringUtils.hasText(remark)) wrapper.like(Accessory::getRemark, remark);
        if (highValue != null) wrapper.eq(Accessory::getIsHighValue, highValue);
        if (shelfId != null) wrapper.eq(Accessory::getShelfId, shelfId);
        if (StringUtils.hasText(startDate)) wrapper.ge(Accessory::getCreateTime, startDate + " 00:00:00");
        if (StringUtils.hasText(endDate)) wrapper.le(Accessory::getCreateTime, endDate + " 23:59:59");
        if (StringUtils.hasText(sortFields) && StringUtils.hasText(sortOrders)) {
            String[] fields = sortFields.split(",");
            String[] orders = sortOrders.split(",");
            for (int i = 0; i < fields.length && i < orders.length; i++) {
                boolean asc = "ascending".equalsIgnoreCase(orders[i]);
                switch (fields[i].trim()) {
                    case "barcode" -> wrapper.orderBy(true, asc, Accessory::getBarcode);
                    case "itemCode" -> wrapper.orderBy(true, asc, Accessory::getItemCode);
                    case "createTime" -> wrapper.orderBy(true, asc, Accessory::getCreateTime);
                    case "status" -> wrapper.orderBy(true, asc, Accessory::getStatus);
                    case "categoryName" -> wrapper.orderBy(true, asc, Accessory::getCategoryId);
                    case "workerName" -> wrapper.orderBy(true, asc, Accessory::getWorkerId);
                }
            }
        }
        wrapper.orderByDesc(Accessory::getCreateTime);
        Page<Accessory> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<AccessoryVO> records = toVOList(page.getRecords());
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<InventoryGroupVO> inventoryGroup(String barcode, Long categoryId,
                                                         Integer statusFilter, Long workerId,
                                                         String remark, Integer highValue, Long shelfId,
                                                         String startDate, String endDate,
                                                         Integer pageNum, Integer pageSize) {
        List<InventoryGroupVO> allGroups = baseMapper.selectInventoryGroup(
                barcode, categoryId, statusFilter, workerId, remark, highValue, shelfId, startDate, endDate);

        int total = allGroups.size();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(from + pageSize, total);
        List<InventoryGroupVO> pageData = from < total ? allGroups.subList(from, to) : List.of();
        return new PageResult<>(pageData, (long) total, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AccessoryVO> importFromCsv(MultipartFile file, Long categoryId, String remark, String operator) {
        List<AccessoryVO> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                String barcode = parts[0].trim().replace("\uFEFF", "");
                if (barcode.isEmpty()) continue;
                int qty = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
                Long catId = categoryId;
                if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                    Category cat = categoryMapper.selectOne(
                            new LambdaQueryWrapper<Category>().eq(Category::getName, parts[2].trim()));
                    if (cat != null) catId = cat.getId();
                }
                String itemRemark = remark;
                if (parts.length > 3 && !parts[3].trim().isEmpty()) itemRemark = parts[3].trim();
                Long shelfId = null;
                if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                    com.cm.entity.Shelf shelf = shelfMapper.selectOne(
                            new LambdaQueryWrapper<com.cm.entity.Shelf>().eq(com.cm.entity.Shelf::getName, parts[4].trim()));
                    if (shelf != null) shelfId = shelf.getId();
                }
                for (int j = 0; j < qty; j++) {
                    Accessory acc = new Accessory();
                    acc.setBarcode(barcode);
                    acc.setItemCode(generateItemCode());
                    acc.setCategoryId(catId);
                    acc.setShelfId(shelfId);
                    acc.setRemark(itemRemark);
                    acc.setStatus(AccessoryStatusEnum.IN_STOCK.getCode());
                    acc.setOperator(operator);
                    acc.setVersion(0);
                    save(acc);
                    result.add(toVO(acc));
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "导入失败：" + e.getMessage());
        }
        return result;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file, String operator) {
        int success = 0, fail = 0;
        List<String> errors = new ArrayList<>();
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream())) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            // Find or create parent category "空调"
            Category parentCat = categoryMapper.selectOne(
                    new LambdaQueryWrapper<Category>().eq(Category::getName, "空调").eq(Category::getParentId, 0));
            if (parentCat == null) {
                parentCat = new Category();
                parentCat.setName("空调");
                parentCat.setParentId(0L);
                parentCat.setPartNumber("");
                parentCat.setSort(1);
                parentCat.setStatus(1);
                categoryMapper.insert(parentCat);
            }
            Long parentId = parentCat.getId();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                String partNum = getCellStr(row.getCell(0));
                String name = getCellStr(row.getCell(1));
                String qtyStr = getCellStr(row.getCell(2));
                String shelfName = getCellStr(row.getCell(3));
                String remark = getCellStr(row.getCell(4));

                if (name.isEmpty()) name = "未分类";
                int qty;
                try {
                    qty = (int) Double.parseDouble(qtyStr);
                } catch (Exception e) {
                    errors.add("行" + (i+1) + ": 数量无效[" + qtyStr + "]");
                    fail++;
                    continue;
                }
                if (qty <= 0) {
                    errors.add("行" + (i+1) + ": 数量<=0");
                    fail++;
                    continue;
                }

                // Find or create sub-category (by part_number + name under parent)
                Long catId = null;
                if (!partNum.isEmpty()) {
                    Category existCat = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                            .eq(Category::getPartNumber, partNum).eq(Category::getParentId, parentId));
                    if (existCat != null) {
                        catId = existCat.getId();
                    }
                }
                if (catId == null) {
                    Category existCat = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                            .eq(Category::getName, name).eq(Category::getParentId, parentId)
                            .eq(Category::getPartNumber, partNum.isEmpty() ? "" : partNum));
                    if (existCat != null) {
                        catId = existCat.getId();
                    }
                }
                if (catId == null) {
                    Category newCat = new Category();
                    newCat.setName(name);
                    newCat.setParentId(parentId);
                    newCat.setPartNumber(partNum);
                    Integer maxSort = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                            .eq(Category::getParentId, parentId).orderByDesc(Category::getSort).last("LIMIT 1"))
                            .getSort();
                    newCat.setSort(maxSort != null ? maxSort + 1 : 1);
                    newCat.setStatus(1);
                    categoryMapper.insert(newCat);
                    catId = newCat.getId();
                }

                // Find shelf
                Long shelfId = null;
                if (!shelfName.isEmpty()) {
                    com.cm.entity.Shelf shelf = shelfMapper.selectOne(
                            new LambdaQueryWrapper<com.cm.entity.Shelf>().eq(com.cm.entity.Shelf::getName, shelfName));
                    if (shelf != null) shelfId = shelf.getId();
                }

                // Create accessories
                for (int j = 0; j < qty; j++) {
                    Accessory acc = new Accessory();
                    acc.setBarcode(partNum);
                    acc.setItemCode(generateItemCode());
                    acc.setCategoryId(catId);
                    acc.setShelfId(shelfId);
                    acc.setRemark(remark);
                    acc.setStatus(AccessoryStatusEnum.IN_STOCK.getCode());
                    acc.setOperator(operator);
                    acc.setVersion(0);
                    save(acc);
                    success++;
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "导入失败：" + e.getMessage());
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        result.put("errors", errors);
        return result;
    }

    private String getCellStr(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        return cell.getStringCellValue().trim();
    }
    private String generateItemCode() {
        String dateStr = LocalDate.now().format(DATE_FMT);
        String prefix = "AC" + dateStr + "-";
        String maxCode = baseMapper.selectMaxItemCodeByPrefix(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.contains("-")) {
            try {
                seq = Integer.parseInt(maxCode.substring(maxCode.lastIndexOf("-") + 1)) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return prefix + String.format("%03d", seq);
    }

    /** 单条转换（用于入库、更新等少量场景） */
    private AccessoryVO toVO(Accessory acc) {
        return toVO(acc, null, null, null);
    }

    private AccessoryVO toVO(Accessory acc,
                              Map<Long, Category> catMap,
                              Map<Long, com.cm.entity.Shelf> shelfMap,
                              Map<Long, Worker> workerMap) {
        AccessoryVO vo = new AccessoryVO();
        vo.setId(acc.getId());
        vo.setBarcode(acc.getBarcode());
        vo.setItemCode(acc.getItemCode());
        vo.setCategoryId(acc.getCategoryId());
        vo.setRemark(acc.getRemark());
        vo.setStatus(acc.getStatus());
        vo.setStatusDesc(AccessoryStatusEnum.of(acc.getStatus()).getDesc());
        vo.setWorkerId(acc.getWorkerId());
        vo.setShelfId(acc.getShelfId());
        vo.setRelatedItemCode(acc.getRelatedItemCode());
        vo.setIsHighValue(acc.getIsHighValue());
        vo.setCreateTime(acc.getCreateTime());

        if (catMap != null) {
            Category cat = acc.getCategoryId() != null ? catMap.get(acc.getCategoryId()) : null;
            vo.setCategoryName(cat != null ? cat.getName() : null);
            vo.setPartNumber(cat != null ? cat.getPartNumber() : null);
            if (cat != null && cat.getParentId() != null && cat.getParentId() != 0) {
                Category parent = catMap.get(cat.getParentId());
                vo.setParentCategoryName(parent != null ? parent.getName() : null);
            }
        } else {
            Category cat = acc.getCategoryId() != null ? categoryMapper.selectById(acc.getCategoryId()) : null;
            vo.setCategoryName(cat != null ? cat.getName() : null);
            vo.setPartNumber(cat != null ? cat.getPartNumber() : null);
            if (cat != null && cat.getParentId() != null && cat.getParentId() != 0) {
                Category parent = categoryMapper.selectById(cat.getParentId());
                vo.setParentCategoryName(parent != null ? parent.getName() : null);
            }
        }
        if (shelfMap != null) {
            com.cm.entity.Shelf shelf = acc.getShelfId() != null ? shelfMap.get(acc.getShelfId()) : null;
            vo.setShelfName(shelf != null ? shelf.getName() : null);
        } else {
            com.cm.entity.Shelf shelf = acc.getShelfId() != null ? shelfMapper.selectById(acc.getShelfId()) : null;
            vo.setShelfName(shelf != null ? shelf.getName() : null);
        }
        if (workerMap != null) {
            Worker worker = acc.getWorkerId() != null ? workerMap.get(acc.getWorkerId()) : null;
            vo.setWorkerName(worker != null ? worker.getName() : null);
        } else {
            Worker worker = acc.getWorkerId() != null ? workerMapper.selectById(acc.getWorkerId()) : null;
            vo.setWorkerName(worker != null ? worker.getName() : null);
        }
        return vo;
    }

    /** 批量转换：预加载所有关联数据，避免 N+1 查询 */
    private List<AccessoryVO> toVOList(List<Accessory> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();

        Set<Long> catIds = list.stream().map(Accessory::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> shelfIds = list.stream().map(Accessory::getShelfId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> workerIds = list.stream().map(Accessory::getWorkerId).filter(Objects::nonNull).collect(Collectors.toSet());

        // Load categories and their parents
        List<Category> cats = catIds.isEmpty() ? Collections.emptyList() : categoryMapper.selectBatchIds(catIds);
        Set<Long> parentIds = cats.stream().map(Category::getParentId).filter(pid -> pid != null && pid != 0).collect(Collectors.toSet());
        List<Category> parents = parentIds.isEmpty() ? Collections.emptyList() : categoryMapper.selectBatchIds(parentIds);
        Map<Long, Category> catMap = new HashMap<>();
        cats.forEach(c -> catMap.put(c.getId(), c));
        parents.forEach(p -> catMap.put(p.getId(), p));
        Map<Long, com.cm.entity.Shelf> shelfMap = shelfIds.isEmpty() ? Collections.emptyMap()
                : shelfMapper.selectBatchIds(shelfIds).stream().collect(Collectors.toMap(com.cm.entity.Shelf::getId, s -> s));
        Map<Long, Worker> workerMap = workerIds.isEmpty() ? Collections.emptyMap()
                : workerMapper.selectBatchIds(workerIds).stream().collect(Collectors.toMap(Worker::getId, w -> w));

        return list.stream().map(acc -> toVO(acc, catMap, shelfMap, workerMap)).collect(Collectors.toList());
    }
}