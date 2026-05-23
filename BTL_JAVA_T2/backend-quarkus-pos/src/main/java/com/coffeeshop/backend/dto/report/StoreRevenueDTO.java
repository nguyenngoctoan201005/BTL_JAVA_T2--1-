package com.coffeeshop.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreRevenueDTO {
    private String storeName;
    private Long totalRevenue;
    private Long orderCount;
}