package com.coffeeshop.backend.mapper;

import com.coffeeshop.backend.dto.product.ProductVariantDTO;
import com.coffeeshop.backend.entity.ProductVariant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    ProductVariantDTO toProductVariantDTO(ProductVariant productVariant);

    ProductVariant toProductVariant(ProductVariantDTO productVariantDTO);
}
