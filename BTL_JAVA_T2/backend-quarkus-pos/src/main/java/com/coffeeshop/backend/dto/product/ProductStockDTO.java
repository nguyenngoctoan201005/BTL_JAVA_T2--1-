package com.coffeeshop.backend.dto.product;

import lombok.Data;

@Data
public class ProductStockDTO {
    private Long id;
    private String storeName;
    private int quantity;
    private Long productVariantId;
}
