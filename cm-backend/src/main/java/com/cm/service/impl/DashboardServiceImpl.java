package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cm.entity.Accessory;
import com.cm.entity.Category;
import com.cm.entity.FlowRecord;
import com.cm.entity.Worker;
import com.cm.enums.AccessoryStatusEnum;
import com.cm.mapper.AccessoryMapper;
import com.cm.mapper.CategoryMapper;
import com.cm.mapper.FlowRecordMapper;
import com.cm.mapper.WorkerMapper;
import com.cm.service.DashboardService;
import com.cm.vo.DashboardVO;
import com.cm.vo.FlowRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AccessoryMapper accessoryMapper;
    private final WorkerMapper workerMapper;
    private final CategoryMapper categoryMapper;
    private final FlowRecordMapper flowRecordMapper;

    @Override
    public DashboardVO getStats() {
        DashboardVO vo = new DashboardVO();

        vo.setTotalAccessories(accessoryMapper.selectCount(null));
        vo.setInStockCount(accessoryMapper.selectCount(
                new LambdaQueryWrapper<Accessory>().eq(Accessory::getStatus, AccessoryStatusEnum.IN_STOCK.getCode())));
        vo.setOutStockCount(accessoryMapper.selectCount(
                new LambdaQueryWrapper<Accessory>().eq(Accessory::getStatus, AccessoryStatusEnum.OUTBOUND.getCode())));
        vo.setSoldCount(accessoryMapper.selectCount(
                new LambdaQueryWrapper<Accessory>().eq(Accessory::getStatus, AccessoryStatusEnum.SOLD.getCode())));
        vo.setWorkerCount(workerMapper.selectCount(
                new LambdaQueryWrapper<Worker>().eq(Worker::getStatus, 1)));
        vo.setCategoryCount(categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1)));

        // Recent 8 flow records
        List<FlowRecord> records = flowRecordMapper.selectList(
                new LambdaQueryWrapper<FlowRecord>().orderByDesc(FlowRecord::getCreateTime).last("LIMIT 8"));
        List<FlowRecordVO> recentFlows = records.stream().map(r -> {
            FlowRecordVO f = new FlowRecordVO();
            f.setId(r.getId());
            f.setAccessoryId(r.getAccessoryId());
            f.setItemCode(r.getItemCode());
            f.setBarcode(r.getBarcode());
            f.setFlowType(r.getFlowType());
            f.setFromWorkerId(r.getFromWorkerId());
            f.setFromWorkerName(r.getFromWorkerName());
            f.setToWorkerId(r.getToWorkerId());
            f.setToWorkerName(r.getToWorkerName());
            f.setCustomerName(r.getCustomerName());
            f.setPrice(r.getPrice());
            f.setCustomerPhone(r.getCustomerPhone());
            f.setRemark(r.getRemark());
            f.setOperator(r.getOperator());
            f.setCreateTime(r.getCreateTime());
            return f;
        }).collect(Collectors.toList());
        vo.setRecentFlows(recentFlows);

        // Category distribution: group accessories by category
        List<Accessory> all = accessoryMapper.selectList(null);
        Map<Long, Long> countByCategory = all.stream()
                .filter(a -> a.getCategoryId() != null)
                .collect(Collectors.groupingBy(Accessory::getCategoryId, Collectors.counting()));
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1));
        List<Map<String, Object>> dist = new ArrayList<>();
        for (Category c : categories) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", c.getName());
            m.put("count", countByCategory.getOrDefault(c.getId(), 0L));
            dist.add(m);
        }
        dist.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        vo.setCategoryDistribution(dist);

        return vo;
    }
}