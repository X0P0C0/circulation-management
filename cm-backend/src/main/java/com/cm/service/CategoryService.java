package com.cm.service;

import com.cm.entity.Category;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CategoryService extends com.baomidou.mybatisplus.extension.service.IService<Category> {
    void create(com.cm.dto.CategoryDTO dto);
    void update(Long id, com.cm.dto.CategoryDTO dto);
    void delete(Long id);
    void moveSort(Long id, String direction);
    void resort();
    List<Category> listAll(String keyword, String sortFields, String sortOrders);
    List<Category> getTopCategories();
    void batchSort(List<Long> ids);

}

