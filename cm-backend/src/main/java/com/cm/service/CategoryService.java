package com.cm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cm.dto.CategoryDTO;
import com.cm.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {
    void create(CategoryDTO dto);
    void update(Long id, CategoryDTO dto);
    void delete(Long id);
    List<Category> listAll();
}