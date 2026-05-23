package com.coffeeshop.backend.mapper;

import org.mapstruct.Mapper;
import com.coffeeshop.backend.dto.category.CategoryDTO;
import com.coffeeshop.backend.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toCategoryDTO(Category category);
    Category toCategory(CategoryDTO categoryDTO);
}
