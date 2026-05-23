package com.coffeeshop.backend.mapper;

import com.coffeeshop.backend.dto.product.ProductStockDTO;
import com.coffeeshop.backend.entity.ProductStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductStockMapper {
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "productVariant.id", target = "productVariantId")
    ProductStockDTO toProductStockDTO(ProductStock productStock);
}
