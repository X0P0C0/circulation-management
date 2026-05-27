package com.cm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cm.common.result.PageResult;
import com.cm.dto.WorkerDTO;
import com.cm.entity.Worker;
import com.cm.vo.WorkerVO;
import java.util.List;

public interface WorkerService extends IService<Worker> {
    void create(WorkerDTO dto);
    void update(Long id, WorkerDTO dto);
    void delete(Long id);
    PageResult<WorkerVO> listPage(String keyword, Integer pageNum, Integer pageSize);
    List<WorkerVO> listAll();
}