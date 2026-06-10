package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.exception.BusinessException;
import com.cm.entity.Shelf;
import com.cm.mapper.ShelfMapper;
import com.cm.service.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelfServiceImpl extends ServiceImpl<ShelfMapper, Shelf> implements ShelfService {

    @Override
    public List<Shelf> listAll(String keyword) {
        LambdaQueryWrapper<Shelf> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Shelf::getName, keyword).or().like(Shelf::getLocation, keyword);
        }
        wrapper.orderByAsc(Shelf::getName);
        return list(wrapper);
    }

    @Override
    public void create(Shelf shelf) {
        save(shelf);
    }

    @Override
    public void update(Long id, Shelf shelf) {
        Shelf exist = getById(id);
        if (exist == null) throw new BusinessException(404, "货架不存在");
        shelf.setId(id);
        updateById(shelf);
    }

    @Override
    public void delete(Long id) {
        Shelf exist = getById(id);
        if (exist == null) throw new BusinessException(404, "货架不存在");
        removeById(id);
    }
}
