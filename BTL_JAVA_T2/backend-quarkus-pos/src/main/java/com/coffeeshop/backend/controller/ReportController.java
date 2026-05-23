package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.report.StoreRevenueDTO;
import com.coffeeshop.backend.entity.Store;

import jakarta.ws.rs.core.Response;
import com.coffeeshop.backend.entity.Order;
import com.coffeeshop.backend.entity.Store;
import lombok.extern.slf4j.Slf4j;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Path("/api/reports")
@Produces(MediaType.APPLICATION_JSON)
public class ReportController {

    @Inject
    EntityManager em;

    // Thêm các repository để lưu dữ liệu
    @Inject
    com.coffeeshop.backend.repository.OrderRepository orderRepository;

    @POST
    @Path("/save-order")
    @jakarta.transaction.Transactional
    public Response saveOrder(java.util.Map<String, Object> data) {
        try {
            Order order = new Order();
            order.setTotalPrice(new java.math.BigDecimal(data.get("totalPrice").toString()));
            order.setStatus(com.coffeeshop.backend.enums.OrderStatus.PAID);
            order.setOrderDate(java.time.LocalDateTime.now());
            order.setCreatedAt(java.time.LocalDateTime.now());

            Long storeId = Long.parseLong(data.get("storeId").toString());
            Store store = em.find(Store.class, storeId);
            order.setStore(store);

            com.coffeeshop.backend.entity.User user = em.find(com.coffeeshop.backend.entity.User.class, 1L);
            order.setUser(user);

            em.persist(order);

            // --- LƯU CHI TIẾT CÁC MÓN ĂN VÀO BẢNG ORDER_DETAILS ---
            List<java.util.Map<String, Object>> items = (List<java.util.Map<String, Object>>) data.get("items");
            if (items != null) {
                for (java.util.Map<String, Object> item : items) {
                    String name = item.get("name").toString();
                    int qty = Integer.parseInt(item.get("quantity").toString());
                    java.math.BigDecimal price = new java.math.BigDecimal(item.get("price").toString());

                    // Tìm ProductVariant trong DB dựa vào tên món
                    List<com.coffeeshop.backend.entity.ProductVariant> variants = em.createQuery(
                            "SELECT pv FROM ProductVariant pv WHERE pv.product.name = :name",
                            com.coffeeshop.backend.entity.ProductVariant.class)
                            .setParameter("name", name)
                            .setMaxResults(1)
                            .getResultList();

                    if (!variants.isEmpty()) {
                        com.coffeeshop.backend.entity.OrderDetail detail = new com.coffeeshop.backend.entity.OrderDetail();
                        detail.setOrder(order);
                        detail.setProductVariant(variants.get(0));
                        detail.setQuantity(qty);
                        detail.setUnitPrice(price);
                        em.persist(detail);
                    }
                }
            }
            // --------------------------------------------------------

            log.info("Đã lưu đơn hàng #{} thành công", order.getId());
            return Response.ok("{\"message\":\"Success\", \"id\":" + order.getId() + "}").build();
        } catch (Exception e) {
            log.error("Lỗi khi lưu order", e);
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/history/{orderId}/items")
    public List<java.util.Map<String, Object>> getOrderItems(@PathParam("orderId") Long orderId) {
        // LẤY DỮ LIỆU THẬT BẰNG LỆNH JOIN CÁC BẢNG TRONG DATABASE
        String sql = "SELECT p.name, od.quantity, od.unit_price " +
                "FROM order_details od " +
                "JOIN product_variants pv ON od.product_variant_id = pv.id " +
                "JOIN products p ON pv.product_id = p.id " +
                "WHERE od.order_id = :orderId";

        List<Object[]> results = em.createNativeQuery(sql)
                .setParameter("orderId", orderId)
                .getResultList();

        List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        for (Object[] row : results) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", row[0]);
            map.put("quantity", row[1]);
            map.put("price", row[2]);
            items.add(map);
        }
        return items;
    }

    @GET
    @Path("/store-comparison")
    public List<StoreRevenueDTO> getStoreComparison() {
        // Cập nhật lại tên cột: đổi total_amount thành total_price
        String sql = "SELECT s.name, SUM(o.total_price), COUNT(o.id) " +
                "FROM orders o " +
                "JOIN stores s ON o.store_id = s.id " +
                "WHERE o.status = 'PAID' " +
                "GROUP BY s.name";

        List<Object[]> results = em.createNativeQuery(sql).getResultList();

        return results.stream()
                .map(record -> new StoreRevenueDTO(
                        (String) record[0],
                        ((Number) record[1]).longValue(),
                        ((Number) record[2]).longValue()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/history")
    public jakarta.ws.rs.core.Response getOrderHistory() {
        // Lấy 50 đơn hàng mới nhất (Bao gồm cả trạng thái và tên quán)
        String sql = "SELECT o.id, o.order_date, o.total_price, o.status, s.name " +
                "FROM orders o JOIN stores s ON o.store_id = s.id " +
                "ORDER BY o.order_date DESC LIMIT 50";

        List<Object[]> results = em.createNativeQuery(sql).getResultList();

        // Chuyển kết quả sang danh sách Map để tự động biến thành JSON
        List<java.util.Map<String, Object>> historyList = new java.util.ArrayList<>();
        for (Object[] row : results) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", row[0]);

            // Format ngày giờ cho đẹp
            if (row[1] != null) {
                String dateStr = row[1].toString();
                if (dateStr.length() > 19)
                    dateStr = dateStr.substring(0, 19); // Cắt phần mili-giây
                map.put("orderDate", dateStr);
            }

            map.put("totalPrice", row[2]);
            map.put("status", row[3]);
            map.put("storeName", row[4]);
            historyList.add(map);
        }

        return jakarta.ws.rs.core.Response.ok(historyList).build();
    }

}