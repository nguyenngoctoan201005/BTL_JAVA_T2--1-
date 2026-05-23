package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.dto.product.ProductStockDTO;
import com.coffeeshop.backend.entity.ProductStock;
import com.coffeeshop.backend.entity.StockHistory;
import com.coffeeshop.backend.entity.Store;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.exception.ResourceNotFoundException;
import com.coffeeshop.backend.mapper.ProductStockMapper;
import com.coffeeshop.backend.repository.ProductStockRepository;
import com.coffeeshop.backend.repository.StockHistoryRepository;
import com.coffeeshop.backend.repository.StoreRepository;
import com.coffeeshop.backend.repository.UserRepository;
import com.coffeeshop.backend.service.ProductStockService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductStockServiceImpl implements ProductStockService {

    private final ProductStockRepository productStockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ProductStockMapper productStockMapper;

    @Override
    public List<ProductStockDTO> getStockByProductVariantId(Long productVariantId) {
        List<Store> allActiveStores = storeRepository.findAllByIsActive(true);
        List<ProductStock> existingStocks = productStockRepository.findAllByProductVariantId(productVariantId);
        
        Map<Long, ProductStock> existingStocksMap = existingStocks.stream()
                .collect(Collectors.toMap(stock -> stock.getStore().getId(), stock -> stock));

        List<ProductStockDTO> fullStockList = new ArrayList<>();

        for (Store store : allActiveStores) {
            ProductStock existingStock = existingStocksMap.get(store.getId());
            if (existingStock != null) {
                fullStockList.add(productStockMapper.toProductStockDTO(existingStock));
            } else {
                // Create a 'virtual' stock DTO for stores with no stock record yet
                ProductStockDTO newStockDto = new ProductStockDTO();
                newStockDto.setId(null); // No ID as it doesn't exist in DB
                newStockDto.setStoreName(store.getName());
                newStockDto.setQuantity(0);
                newStockDto.setProductVariantId(productVariantId);
                fullStockList.add(newStockDto);
            }
        }
        return fullStockList;
    }

    @Override
    @Transactional
    public ProductStockDTO updateStock(Long stockId, int newQuantity) {
        // If stockId is null, it means we need to create a new stock record
        if (stockId == null) {
            // This logic requires more information (productVariantId, storeId) and is not implemented here.
            // A separate createStock method would be needed. This method assumes updates to existing stock.
            throw new IllegalArgumentException("Stock ID cannot be null for an update.");
        }

        ProductStock productStock = productStockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductStock not found with id: " + stockId));

        int oldQuantity = productStock.getQuantity();
        int quantityChanged = newQuantity - oldQuantity;

        productStock.setQuantity(newQuantity);
        ProductStock updatedStock = productStockRepository.save(productStock);

        // Get current user's ID for logging
        String username = "admin@example.com";
        User user = userRepository.findByEmail(username).orElse(null);

        StockHistory history = new StockHistory();
        history.setProductVariant(productStock.getProductVariant());
        history.setStore(productStock.getStore());
        history.setQuantityChanged(quantityChanged);
        history.setCurrentQuantity(newQuantity);
        history.setReason("ADJUSTMENT");
        if (user != null) {
            history.setCreatedBy(user.getId());
        }
        stockHistoryRepository.save(history);

        return productStockMapper.toProductStockDTO(updatedStock);
    }
}

