// ProductVariantDTO.java
package com.coffeeshop.backend.dto.product;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductVariantDTO {
    private Long id;
    private String sku;
    private String size;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
}