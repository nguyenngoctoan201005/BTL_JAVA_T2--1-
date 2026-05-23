package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.dto.ShippingInfoDTO;
import com.coffeeshop.backend.entity.Store;
import com.coffeeshop.backend.exception.OutOfServiceAreaException;
import com.coffeeshop.backend.exception.ResourceNotFoundException;
import com.coffeeshop.backend.repository.StoreRepository;
import com.coffeeshop.backend.service.ShippingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ShippingServiceImpl implements ShippingService {

    private final StoreRepository storeRepository;

    @Value("${shipping.rate-per-km}")
    private double ratePerKm;

    @Value("${shipping.first-km-rate}")
    private double firstKmRate;

    private static final double MAX_SERVICE_DISTANCE_KM = 20.0;
    private static final double EARTH_RADIUS = 6371; // In kilometers

    public ShippingServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public ShippingInfoDTO calculateShippingFee(double latitude, double longitude) {
        if (ratePerKm <= 0 || firstKmRate <= 0) {
            throw new IllegalStateException("Shipping rates are not configured in application properties.");
        }

        List<Store> activeStores = storeRepository.findAllByIsActive(true);
        if (activeStores.isEmpty()) {
            throw new ResourceNotFoundException("No active stores available for shipping.");
        }

        Store nearestStore = null;
        double minDistance = Double.MAX_VALUE;

        for (Store store : activeStores) {
            if (store.getLatitude() != null && store.getLongitude() != null) {
                double distance = calculateDistance(store.getLatitude(), store.getLongitude(), latitude, longitude);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestStore = store;
                }
            }
        }

        if (nearestStore == null) {
            throw new ResourceNotFoundException("Could not find a store with valid coordinates.");
        }

        if (minDistance > MAX_SERVICE_DISTANCE_KM) {
            throw new OutOfServiceAreaException("Sorry, your location is outside our service area.");
        }

        double fee;
        if (minDistance <= 1) {
            fee = firstKmRate;
        } else {
            fee = firstKmRate + (minDistance - 1) * ratePerKm;
        }
        
        // Round fee to nearest 100
        BigDecimal finalFee = BigDecimal.valueOf(Math.round(fee / 100) * 100);

        return new ShippingInfoDTO(finalFee, minDistance, nearestStore.getId(), nearestStore.getName());
    }

    @Override
    public ShippingInfoDTO calculateShippingFeeForStore(Long storeId, double latitude, double longitude) {
        if (ratePerKm <= 0 || firstKmRate <= 0) {
            throw new IllegalStateException("Shipping rates are not configured in application properties.");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        if (!store.isActive() || store.getLatitude() == null || store.getLongitude() == null) {
            throw new ResourceNotFoundException("Store is not available for shipping.");
        }

        double distance = calculateDistance(store.getLatitude(), store.getLongitude(), latitude, longitude);

        if (distance > MAX_SERVICE_DISTANCE_KM) {
            throw new OutOfServiceAreaException("The selected store is outside our service area for your location.");
        }

        double fee;
        if (distance <= 1) {
            fee = firstKmRate;
        } else {
            fee = firstKmRate + (distance - 1) * ratePerKm;
        }

        BigDecimal finalFee = BigDecimal.valueOf(Math.round(fee / 100) * 100);

        return new ShippingInfoDTO(finalFee, distance, store.getId(), store.getName());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS * c;
    }
}

