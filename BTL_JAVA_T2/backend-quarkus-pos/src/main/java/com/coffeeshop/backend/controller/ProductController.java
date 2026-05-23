package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.product.ProductDTO;
import com.coffeeshop.backend.dto.product.ProductRequest;
import com.coffeeshop.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.coffeeshop.backend.dto.product.UpdateStockRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.coffeeshop.backend.dto.product.ProductStockDTO;
import com.coffeeshop.backend.service.ProductStockService;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductStockService productStockService;

    @PutMapping("/stocks/{stockId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductStockDTO> updateStock(@PathVariable Long stockId,
            @RequestBody UpdateStockRequest request) {
        ProductStockDTO updatedStock = productStockService.updateStock(stockId, request.getQuantity());
        return ResponseEntity.ok(updatedStock);
    }

    @GetMapping("/{productVariantId}/stocks")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductStockDTO>> getProductStock(@PathVariable Long productVariantId) {
        List<ProductStockDTO> stocks = productStockService.getStockByProductVariantId(productVariantId);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProductsList() {
        log.info("Calling getAllProductsList");
        List<ProductDTO> products = productService.getAllProductsList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(@RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Calling getAllProducts with search: {}, page: {}, size: {}", search, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getAllProducts(search, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/admin")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductDTO>> getAllProductsForAdmin(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getAllProductsForAdmin(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(@PathVariable String categoryName,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getProductsByCategory(categoryName, search, pageable);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductRequest productRequest) {
        ProductDTO newProductDTO = productService.createProduct(productRequest);
        return new ResponseEntity<>(newProductDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId,
            @RequestBody ProductRequest productRequest) {
        ProductDTO updatedProduct = productService.updateProduct(productId, productRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/status")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProductStatus(@PathVariable Long productId,
            @RequestBody Map<String, Boolean> body) {
        Boolean isActive = body.get("isActive");
        ProductDTO updatedProduct = productService.updateProductStatus(productId, isActive);
        return ResponseEntity.ok(updatedProduct);
    }
}
