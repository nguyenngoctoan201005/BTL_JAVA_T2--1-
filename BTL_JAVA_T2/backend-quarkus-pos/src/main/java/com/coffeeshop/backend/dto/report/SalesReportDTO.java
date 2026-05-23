package com.coffeeshop.backend.dto.report;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class SalesReportDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private List<Map<String, Object>> revenueOverTime;
    private List<Map<String, Object>> topSellingProducts;
}
