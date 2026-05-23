package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.ShippingInfoDTO;
import com.coffeeshop.backend.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/calculate")
    public ResponseEntity<ShippingInfoDTO> calculateShippingFee(@RequestBody Map<String, Double> body) {
        double latitude = body.get("latitude");
        double longitude = body.get("longitude");
        ShippingInfoDTO shippingInfo = shippingService.calculateShippingFee(latitude, longitude);
        return ResponseEntity.ok(shippingInfo);
    }

    @PostMapping("/calculate-for-store")
    public ResponseEntity<ShippingInfoDTO> calculateShippingFeeForStore(@RequestBody Map<String, Number> body) {
        Long storeId = body.get("storeId").longValue();
        double latitude = body.get("latitude").doubleValue();
        double longitude = body.get("longitude").doubleValue();
        ShippingInfoDTO shippingInfo = shippingService.calculateShippingFeeForStore(storeId, latitude, longitude);
        return ResponseEntity.ok(shippingInfo);
    }
}
