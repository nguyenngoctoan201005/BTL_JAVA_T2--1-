package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.report.SalesReportDTO;

public interface ReportService {
    SalesReportDTO getSalesReport(String period, Long storeId);
}

