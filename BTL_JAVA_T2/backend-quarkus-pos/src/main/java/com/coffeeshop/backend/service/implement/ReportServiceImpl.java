package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.dto.report.SalesReportDTO;
import com.coffeeshop.backend.entity.Order;
import com.coffeeshop.backend.entity.OrderDetail;
import com.coffeeshop.backend.repository.OrderDetailRepository;
import com.coffeeshop.backend.repository.OrderRepository;
import com.coffeeshop.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public SalesReportDTO getSalesReport(String period, Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;

        switch (period.toLowerCase()) {
            case "daily":
                startDate = now.toLocalDate().atStartOfDay();
                break;
            case "weekly":
                startDate = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();
                break;
            case "monthly":
                startDate = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        List<Order> orders;
        if (storeId != null) {
            orders = orderRepository.findAllByOrderDateBetweenAndStoreId(startDate, now, storeId);
        } else {
            orders = orderRepository.findAllByOrderDateBetween(startDate, now);
        }

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();

        List<Map<String, Object>> revenueOverTime = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getOrderDate().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalPrice, BigDecimal::add)))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey().toString());
                    map.put("revenue", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        // Correctly get order details from the filtered orders
        List<OrderDetail> filteredOrderDetails = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .collect(Collectors.toList());

        List<Map<String, Object>> topSellingProducts = filteredOrderDetails.stream()
                .collect(Collectors.groupingBy(od -> od.getProductVariant().getProduct().getName() + " (" + od.getProductVariant().getSize() + ")",
                        Collectors.summarizingInt(OrderDetail::getQuantity)))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", entry.getKey());
                    map.put("quantity", entry.getValue().getSum());
                    // Calculate revenue for this specific product group from the filtered details
                    BigDecimal revenue = filteredOrderDetails.stream()
                            .filter(od -> (od.getProductVariant().getProduct().getName() + " (" + od.getProductVariant().getSize() + ")").equals(entry.getKey()))
                            .map(od -> od.getUnitPrice().multiply(new BigDecimal(od.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    map.put("revenue", revenue);
                    return map;
                })
                .sorted((a, b) -> Long.compare((long) b.get("quantity"), (long) a.get("quantity")))
                .limit(5)
                .collect(Collectors.toList());


        SalesReportDTO report = new SalesReportDTO();
        report.setTotalRevenue(totalRevenue);
        report.setTotalOrders(totalOrders);
        report.setRevenueOverTime(revenueOverTime);
        report.setTopSellingProducts(topSellingProducts);

        return report;
    }
}
