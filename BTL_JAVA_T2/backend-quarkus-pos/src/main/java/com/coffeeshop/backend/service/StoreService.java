package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.StoreDTO;

import java.util.List;

public interface StoreService {
    List<StoreDTO> getAllActiveStores();
}
