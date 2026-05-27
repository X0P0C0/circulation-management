package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.exception.BusinessException;
import com.cm.common.result.PageResult;
import com.cm.dto.WorkerDTO;
import com.cm.entity.InventoryOwner;
import com.cm.entity.Worker;
import com.cm.mapper.InventoryOwnerMapper;
import com.cm.mapper.WorkerMapper;
import com.cm.service.WorkerService;
import com.cm.vo.WorkerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerServiceImpl extends ServiceImpl<WorkerMapper, Worker> implements WorkerService {

    private final InventoryOwnerMapper inventoryOwnerMapper;

    @Override
    public void create(WorkerDTO dto) {
        Worker worker = new Worker();
        BeanUtils.copyProperties(dto, worker);
        save(worker);
    }

    @Override
    public void update(Long id, WorkerDTO dto) {
        Worker worker = getById(id);
        if (worker == null) {
            throw new BusinessException(404, "师傅不存在");
        }
        BeanUtils.copyProperties(dto, worker);
        updateById(worker);
    }

    @Override
    public void delete(Long id) {
        Worker worker = getById(id);
        if (worker == null) {
            throw new BusinessException(404, "师傅不存在");
        }
        long count = inventoryOwnerMapper.selectCount(
                new LambdaQueryWrapper<InventoryOwner>()
                        .eq(InventoryOwner::getWorkerId, id)
                        .gt(InventoryOwner::getQty, 0)
        );
        if (count > 0) {
            throw new BusinessException(409, "该师傅名下有未归还配件，无法删除");
        }
        removeById(id);
    }

    @Override
    public PageResult<WorkerVO> listPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Worker> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Worker::getName, keyword)
                    .or().like(Worker::getJobNo, keyword)
                    .or().like(Worker::getPhone, keyword);
        }
        wrapper.orderByDesc(Worker::getCreateTime);
        Page<Worker> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<WorkerVO> records = page.getRecords().stream().map(w -> {
            WorkerVO vo = new WorkerVO();
            BeanUtils.copyProperties(w, vo);
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<WorkerVO> listAll() {
        List<Worker> workers = list(new LambdaQueryWrapper<Worker>()
                .eq(Worker::getStatus, 1).orderByAsc(Worker::getName));
        return workers.stream().map(w -> {
            WorkerVO vo = new WorkerVO();
            BeanUtils.copyProperties(w, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}