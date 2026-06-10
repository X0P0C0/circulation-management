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
import org.springframework.util.StringUtils;

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
    public void moveSort(Long id, String direction) {
        Category cat = getById(id);
        if (cat == null) return;
        LambdaQueryWrapper<Category> w = new LambdaQueryWrapper<>();
        w.eq(Category::getParentId, cat.getParentId()).eq(Category::getStatus, 1);
        if ("up".equals(direction)) {
            w.lt(Category::getSort, cat.getSort()).orderByDesc(Category::getSort).last("LIMIT 1");
        } else {
            w.gt(Category::getSort, cat.getSort()).orderByAsc(Category::getSort).last("LIMIT 1");
        }
        Category target = getOne(w);
        if (target != null) {
            int tmp = cat.getSort(); cat.setSort(target.getSort()); target.setSort(tmp);
            updateById(cat); updateById(target);
        }
    }

    @Override
    public void resort() {
        List<Category> cats = list(new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1).orderByAsc(Category::getSort));
        for (int i = 0; i < cats.size(); i++) { cats.get(i).setSort(i + 1); updateById(cats.get(i)); }
    }

    @Override
    public List<Category> listAll(String keyword, String sortFields, String sortOrders) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Category::getName, keyword).or().like(Category::getPartNumber, keyword));
        }
        wrapper.eq(Category::getStatus, 1).orderByAsc(Category::getSort);
        return list(wrapper);
    }

    @Override
    public List<Category> getTopCategories() {
        return list(new LambdaQueryWrapper<Category>().eq(Category::getParentId, 0).eq(Category::getStatus, 1).orderByAsc(Category::getSort));
    }

    @Override
    public void batchSort(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Category c = getById(ids.get(i));
            if (c != null) { c.setSort(i + 1); updateById(c); }
        }
    }
}