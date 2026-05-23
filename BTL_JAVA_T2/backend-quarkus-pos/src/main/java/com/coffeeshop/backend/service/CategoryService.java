package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.category.CategoryDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(String categoryName);
    List<CategoryDTO> getAllCategories();
}
