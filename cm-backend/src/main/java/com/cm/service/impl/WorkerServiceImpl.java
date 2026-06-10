package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.exception.BusinessException;
import com.cm.common.result.PageResult;
import com.cm.dto.WorkerDTO;
import com.cm.entity.Worker;
import com.cm.mapper.WorkerMapper;
import com.cm.service.WorkerService;
import com.cm.vo.WorkerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cm.common.util.SortUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerServiceImpl extends ServiceImpl<WorkerMapper, Worker> implements WorkerService {

    @Override
    public void create(WorkerDTO dto) {
        Worker worker = new Worker();
        BeanUtils.copyProperties(dto, worker);
        save(worker);
    }

    @Override
    public void update(Long id, WorkerDTO dto) {
        Worker worker = getById(id);
        if (worker == null) throw new BusinessException(404, "师傅不存在");
        BeanUtils.copyProperties(dto, worker);
        worker.setId(id);
        updateById(worker);
    }

    @Override
    public void delete(Long id) {
        Worker worker = getById(id);
        if (worker == null) throw new BusinessException(404, "师傅不存在");
        worker.setStatus(0);
        updateById(worker);
    }

    @Override
    public PageResult<WorkerVO> listPage(String keyword, String sortFields, String sortOrders,
                                         Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Worker> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Worker::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Worker::getName, keyword)
                    .or().like(Worker::getPhone, keyword)
                    .or().like(Worker::getJobNo, keyword));
        }
        Map<String, SFunction<Worker, ?>> sortMapper = new HashMap<>();
        sortMapper.put("name", Worker::getName);
        sortMapper.put("jobNo", Worker::getJobNo);
        sortMapper.put("phone", Worker::getPhone);
        sortMapper.put("remark", Worker::getRemark);
        sortMapper.put("createTime", Worker::getCreateTime);
        SortUtils.applyMultiSort(wrapper, sortFields, sortOrders, sortMapper, Worker::getId, true);
        Page<Worker> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<WorkerVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<WorkerVO> listAll() {
        List<Worker> workers = list(new LambdaQueryWrapper<Worker>()
                .eq(Worker::getStatus, 1).orderByAsc(Worker::getId));
        return workers.stream().map(this::toVO).collect(Collectors.toList());
    }

    private WorkerVO toVO(Worker w) {
        WorkerVO vo = new WorkerVO();
        BeanUtils.copyProperties(w, vo);
        return vo;
    }
}