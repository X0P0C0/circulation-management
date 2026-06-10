package com.cm.service;

import com.cm.entity.Shelf;
import java.util.List;

public interface ShelfService {
    List<Shelf> listAll(String keyword);
    void create(Shelf shelf);
    void update(Long id, Shelf shelf);
    void delete(Long id);
}
