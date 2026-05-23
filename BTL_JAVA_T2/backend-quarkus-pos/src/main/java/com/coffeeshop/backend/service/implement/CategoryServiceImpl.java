package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.dto.category.CategoryDTO;
import com.coffeeshop.backend.entity.Category;
import com.coffeeshop.backend.repository.CategoryRepository;
import com.coffeeshop.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDTO createCategory(String categoryName) {
        Category newCategory = new Category();
        newCategory.setName(categoryName);
        newCategory.setSlug(categoryName.toLowerCase().replaceAll("\\s+", "-"));
        newCategory.setImage("logo.png");
        Category savedCategory = categoryRepository.save(newCategory);
        return convertToDTO(savedCategory);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        return categoryDTO;
    }
}
