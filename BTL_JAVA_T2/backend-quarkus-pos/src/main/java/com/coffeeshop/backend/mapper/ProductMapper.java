package com.coffeeshop.backend.mapper;

import org.mapstruct.Mapper;

import com.coffeeshop.backend.dto.product.ProductDTO;
import com.coffeeshop.backend.entity.Product;


@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {

    ProductDTO toProductDTO(Product product);

    Product toProduct(ProductDTO productDTO);

}
