package com.coffeeshop.backend.repository;

import com.coffeeshop.backend.entity.ProductStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {
    Optional<ProductStock> findByProductVariantIdAndStoreId(Long productVariantId, Long storeId);

    List<ProductStock> findAllByProductVariantId(Long productVariantId);

    @Query("SELECT ps FROM ProductStock ps WHERE ps.productVariant.id = :productVariantId AND ps.store.id = :storeId")
    Optional<ProductStock> findAndLockByProductVariantIdAndStoreId(Long productVariantId, Long storeId);
}
