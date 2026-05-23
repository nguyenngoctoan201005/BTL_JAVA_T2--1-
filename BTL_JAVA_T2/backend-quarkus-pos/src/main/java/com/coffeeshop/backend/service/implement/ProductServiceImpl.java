package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.dto.product.ProductDTO;
import com.coffeeshop.backend.dto.product.ProductRequest;
import com.coffeeshop.backend.dto.product.ProductVariantRequest;
import com.coffeeshop.backend.entity.*;
import com.coffeeshop.backend.exception.ResourceNotFoundException;
import com.coffeeshop.backend.mapper.ProductMapper;
import com.coffeeshop.backend.repository.*;

import com.coffeeshop.backend.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductStockRepository productStockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final StoreRepository storeRepository;
    private final ProductMapper productMapper;
    private final OrderDetailRepository orderDetailRepository;


    @Override
    @Cacheable("products")
    public Page<ProductDTO> getAllProducts(String search, Pageable pageable) {
        log.info("Fetching products from DB with search: {}, pageable: {}", search, pageable);
        Page<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseAndIsActive(search, true, pageable);
        } else {
            products = productRepository.findByIsActive(true, pageable);
        }
        return products.map(productMapper::toProductDTO);
    }

    @Override
    public List<ProductDTO> getAllProductsList() {
        List<Product> products = productRepository.findByIsActive(true);
        return products.stream()
                .map(productMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDTO> getAllProductsForAdmin(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toProductDTO);
    }

    @Override
    @Cacheable("products")
    public Page<ProductDTO> getProductsByCategory(String categoryName, String search, Pageable pageable) {
        log.info("Fetching products by category from DB: {}, search: {}, pageable: {}", categoryName, search, pageable);
        Page<Product> products;
        if ("best-selling".equalsIgnoreCase(categoryName)) {
            // Note: Pagination for this custom logic might require more complex queries.
            // For now, we return it as a single page.
            List<Product> bestSellingProducts = orderDetailRepository.findAll().stream()
                .collect(Collectors.groupingBy(od -> od.getProductVariant().getProduct(),
                    Collectors.summingInt(OrderDetail::getQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            List<ProductDTO> bestSellingProductDTOs = bestSellingProducts.stream()
                .map(productMapper::toProductDTO)
                .collect(Collectors.toList());
            
            return new org.springframework.data.domain.PageImpl<>(bestSellingProductDTOs, pageable, bestSellingProductDTOs.size());
        } else if (search != null && !search.isEmpty()) {
            products = productRepository.findByCategory_NameAndNameContainingIgnoreCaseAndIsActive(categoryName, search, true, pageable);
        } else {
            products = productRepository.findByCategory_NameAndIsActive(categoryName, true, pageable);
        }
        return products.map(productMapper::toProductDTO);
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return productMapper.toProductDTO(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productRequest.getCategoryId()));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setImageUrl(productRequest.getImageUrl());
        product.setCategory(category);
        product.setIsActive(productRequest.getIsActive());

        // We need a default store to create the initial stock.
        Store defaultStore = storeRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Default store not found. Please ensure store with ID 1 exists."));

        productRequest.getVariants().forEach(variantRequest -> {
            ProductVariant variant = new ProductVariant();
            variant.setSku(variantRequest.getSku());
            variant.setSize(variantRequest.getSize());
            variant.setPrice(variantRequest.getPrice());
            variant.setIsActive(variantRequest.getIsActive());
            variant.setProduct(product);
            product.getVariants().add(variant);
        });

        Product savedProduct = productRepository.save(product);

        // After saving the product and variants, create the stock records
        savedProduct.getVariants().forEach(variant -> {
            Integer initialStock = 0; // Default to 0 as stockQuantity is removed from request

            ProductStock productStock = new ProductStock();
            productStock.setProductVariant(variant);
            productStock.setStore(defaultStore);
            productStock.setQuantity(initialStock);
            productStockRepository.save(productStock);

            StockHistory history = new StockHistory();
            history.setProductVariant(variant);
            history.setStore(defaultStore);
            history.setQuantityChanged(initialStock);
            history.setCurrentQuantity(initialStock);
            history.setReason("INITIAL_STOCK");
            // history.setCreatedBy(adminUserId); // Optional: Need to get current admin user
            stockHistoryRepository.save(history);
        });


        return productMapper.toProductDTO(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO updateProduct(Long productId, ProductRequest productRequest) {
        log.info("Updating product with id: {}", productId);
        log.info("Request body: {}", productRequest);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        log.info("Product before update: {}", existingProduct);

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productRequest.getCategoryId()));

        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setImageUrl(productRequest.getImageUrl());
        existingProduct.setCategory(category);
        existingProduct.setIsActive(productRequest.getIsActive());

        // --- REVISED VARIANT MANAGEMENT ---
        Map<String, ProductVariant> existingVariantsMap = existingProduct.getVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getSku, variant -> variant));

        List<ProductVariant> updatedVariants = new ArrayList<>();

        for (ProductVariantRequest variantRequest : productRequest.getVariants()) {
            ProductVariant existingVariant = existingVariantsMap.get(variantRequest.getSku());
            if (existingVariant != null) {
                // Update existing variant
                existingVariant.setSize(variantRequest.getSize());
                existingVariant.setPrice(variantRequest.getPrice());
                existingVariant.setIsActive(variantRequest.getIsActive());
                updatedVariants.add(existingVariant);
                existingVariantsMap.remove(variantRequest.getSku());
            } else {
                // Create new variant
                ProductVariant newVariant = new ProductVariant();
                newVariant.setSku(variantRequest.getSku());
                newVariant.setSize(variantRequest.getSize());
                newVariant.setPrice(variantRequest.getPrice());
                newVariant.setIsActive(variantRequest.getIsActive());
                newVariant.setProduct(existingProduct);
                updatedVariants.add(newVariant);
            }
        }

        // Variants remaining in the map are to be deleted
        existingProduct.getVariants().clear();
        existingProduct.getVariants().addAll(updatedVariants);
        // --- END REVISED VARIANT MANAGEMENT ---

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product after update: {}", updatedProduct);
        return productMapper.toProductDTO(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long productId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Delete image logic removed


        productRepository.delete(existingProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO updateProductStatus(Long productId, Boolean isActive) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        product.setIsActive(isActive);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductDTO(updatedProduct);
    }
}
