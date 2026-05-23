package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.product.ProductStockDTO;
import java.util.List;

public interface ProductStockService {
    List<ProductStockDTO> getStockByProductVariantId(Long productVariantId);
    ProductStockDTO updateStock(Long stockId, int quantity);
}
