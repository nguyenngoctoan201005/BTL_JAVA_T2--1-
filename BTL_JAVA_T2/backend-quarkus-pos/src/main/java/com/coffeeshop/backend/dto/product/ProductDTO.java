package com.coffeeshop.backend.dto.product;

import java.util.List;

import lombok.Data;

import com.coffeeshop.backend.dto.category.CategoryDTO;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private CategoryDTO category;
    private List<ProductVariantDTO> variants;
    private Boolean isActive;
}
