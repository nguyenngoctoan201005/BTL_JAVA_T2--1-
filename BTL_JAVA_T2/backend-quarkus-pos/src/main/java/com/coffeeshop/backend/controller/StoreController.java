package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.StoreDTO;
import com.coffeeshop.backend.service.StoreService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping("")
    public ResponseEntity<List<StoreDTO>> getAllActiveStores() {
        return ResponseEntity.ok(storeService.getAllActiveStores());
    }
}
