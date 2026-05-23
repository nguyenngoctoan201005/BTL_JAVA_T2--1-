package com.coffeeshop.backend.dto;

import java.math.BigDecimal;

public class ShippingInfoDTO {
    private BigDecimal shippingFee;
    private double distance;
    private Long storeId;
    private String storeName;

    public ShippingInfoDTO(BigDecimal shippingFee, double distance, Long storeId, String storeName) {
        this.shippingFee = shippingFee;
        this.distance = distance;
        this.storeId = storeId;
        this.storeName = storeName;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}

