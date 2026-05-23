package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.product.ProductDTO;
import com.coffeeshop.backend.dto.product.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<ProductDTO> getAllProducts(String search, Pageable pageable);

    List<ProductDTO> getAllProductsList();

    Page<ProductDTO> getAllProductsForAdmin(Pageable pageable);

    Page<ProductDTO> getProductsByCategory(String categoryName, String search, Pageable pageable);

    ProductDTO getProductById(Long productId);

    ProductDTO createProduct(ProductRequest productRequest);

    ProductDTO updateProduct(Long productId, ProductRequest productRequest);

    void deleteProduct(Long productId);

    ProductDTO updateProductStatus(Long productId, Boolean isActive);
}