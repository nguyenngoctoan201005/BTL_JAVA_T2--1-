package com.coffeeshop.backend.dto.product;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name cannot be empty")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    private Boolean isActive = true; // Default to active

    private List<ProductVariantRequest> variants;
}