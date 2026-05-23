package com.coffeeshop.backend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantRequest {
    @NotBlank(message = "SKU cannot be empty")
    private String sku;

    @NotBlank(message = "Size cannot be empty")
    private String size;

    @NotNull(message = "Price cannot be null")
    @PositiveOrZero(message = "Price must be a positive value or zero")
    private BigDecimal price;

    private Boolean isActive = true; // Default to active
}