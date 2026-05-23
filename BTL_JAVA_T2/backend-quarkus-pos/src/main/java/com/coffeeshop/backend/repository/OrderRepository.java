package com.coffeeshop.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coffeeshop.backend.entity.Order;
import java.util.List;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

//...

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
    Page<Order> findByStoreId(Long storeId, Pageable pageable);
    List<Order> findAllByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findAllByOrderDateBetweenAndStoreId(LocalDateTime startDate, LocalDateTime endDate, Long storeId);
}
