package com.coffeeshop.backend.dto.address;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String addressText;
    private String label;
    private double latitude;
    private double longitude;
    private boolean isDefault;
    private String notes;
}
