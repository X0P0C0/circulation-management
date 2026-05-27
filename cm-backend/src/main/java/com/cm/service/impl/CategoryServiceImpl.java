package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cm.common.exception.BusinessException;
import com.cm.dto.CategoryDTO;
import com.cm.entity.Category;
import com.cm.mapper.CategoryMapper;
import com.cm.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public void create(CategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        save(category);
    }

    @Override
    public void update(Long id, CategoryDTO dto) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        BeanUtils.copyProperties(dto, category);
        updateById(category);
    }

    @Override
    public void delete(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        removeById(id);
    }

    @Override
    public List<Category> listAll() {
        return list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1).orderByAsc(Category::getSort));
    }
}