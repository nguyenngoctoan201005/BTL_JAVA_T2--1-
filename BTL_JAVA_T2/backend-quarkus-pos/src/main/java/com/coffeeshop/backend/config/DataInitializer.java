package com.coffeeshop.backend.config;

import com.coffeeshop.backend.entity.*;
import com.coffeeshop.backend.enums.UserRole;
import com.coffeeshop.backend.repository.*;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final ProductStockRepository productStockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final jakarta.persistence.EntityManager em;

    // Sự kiện CHUẨN của Quarkus: Tự động chạy ngay khi Server bật lên
    @Transactional
    void onStart(@Observes StartupEvent ev) {
        try {
            log.info("Bắt đầu nạp dữ liệu mẫu (Seed Data)...");
            // Kiểm tra nếu chưa có user nào thì mới nạp, tránh nạp trùng
            if (userRepository.count() == 0) {
                createInitialData();
                log.info("✅ Đã nạp dữ liệu mẫu thành công!");
            } else {
                log.info("Dữ liệu đã tồn tại, bỏ qua nạp mẫu.");
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi nạp dữ liệu", e);
        }
    }

    private void createInitialData() {
        // 1. Tạo Cửa hàng (Stores)
        Store store1 = createStore("Cơ sở 1 - Cầu Giấy", "295 Cầu Giấy, Hà Nội", "0988111111", 21.036643, 105.799447);
        Store store2 = createStore("Cơ sở 2 - Hoàn Kiếm", "99 Hàng Bài, Hà Nội", "0988222222", 21.0285, 105.8542);

        // 2. Tạo Tài khoản (Users) - Đã lưu vào biến để lát nữa gắn cho Hóa đơn ảo
        User staff1 = createUser("Thu Ngân 1", "staff1@gmail.com", "123456", UserRole.STAFF, "0987654322", store1);
        User admin = createUser("Admin Tổng", "admin@gmail.com", "123456", UserRole.ADMIN, "0987654321", null);
        // TẠO THÊM 1 TÀI KHOẢN CHO CƠ SỞ 2
        User staff2 = createUser("Thu Ngân 2", "staff2@gmail.com", "123456", UserRole.STAFF, "0987654323", store2);

        // 3. Tạo Danh mục (Categories)
        Category caPhe = createCategory("Cà phê", "ca-phe", "coffee.png");
        Category tra = createCategory("Trà", "tra", "tea.png");
        Category khac = createCategory("Khác", "khac", "other.png");
        Category doAnVat = createCategory("Đồ ăn vặt", "do-an-vat", "snack.png");

        // 4. Tạo Sản phẩm & Biến thể (Products & Variants)
        createProduct("Món test giao dịch", "Dùng để test API PayOS với giá 2000đ", "test.png", khac, true,
                Arrays.asList(createVariant("Mặc định", "2000", true)), Arrays.asList(store1, store2));

        createProduct("Cà phê đen", "Cà phê đen đá đậm vị", "den.png", caPhe, true,
                Arrays.asList(createVariant("M", "20000", true), createVariant("L", "25000", true)),
                Arrays.asList(store1, store2));

        createProduct("Cà phê sữa", "Cà phê sữa đá truyền thống", "sua.png", caPhe, true,
                Arrays.asList(createVariant("M", "25000", true), createVariant("L", "29000", true)),
                Arrays.asList(store1, store2));

        createProduct("Bạc xỉu", "Bạc xỉu cốt dừa thơm béo", "bacxiu.png", caPhe, true,
                Arrays.asList(createVariant("M", "29000", true), createVariant("L", "35000", true)),
                Arrays.asList(store1, store2));

        createProduct("Americano", "Espresso pha loãng thanh mát", "americano.png", caPhe, true,
                Arrays.asList(createVariant("M", "30000", true), createVariant("L", "35000", true)),
                Arrays.asList(store1, store2));

        createProduct("Trà đào", "Trà đào cam sả thanh mát", "tradao.png", tra, true,
                Arrays.asList(createVariant("M", "35000", true), createVariant("L", "40000", true)),
                Arrays.asList(store1, store2));

        createProduct("Trà vải", "Trà vải hoa hồng", "travai.png", tra, true,
                Arrays.asList(createVariant("M", "35000", true), createVariant("L", "40000", true)),
                Arrays.asList(store1, store2));
        createProduct("Hướng dương", "Hướng dương tẩm vị", "huongduong.png", doAnVat, true,
                Arrays.asList(createVariant("Đĩa", "15000", true)),
                Arrays.asList(store1, store2));

        createProduct("Bim bim khoai tây", "Snack khoai tây chiên giòn", "bimbim.png", doAnVat, true,
                Arrays.asList(createVariant("Gói", "12000", true)),
                Arrays.asList(store1, store2));

        createProduct("Khô gà lá chanh", "Khô gà cay xé sợi", "khoga.png", doAnVat, true,
                Arrays.asList(createVariant("Đĩa", "25000", true)),
                Arrays.asList(store1, store2));

        // ---------------------------------------------------------
        // 5. BƠM 30 ĐƠN HÀNG ẢO ĐỂ VẼ BIỂU ĐỒ DOANH THU & LỊCH SỬ
        // ---------------------------------------------------------
        java.util.Random rand = new java.util.Random();
        for (int i = 1; i <= 30; i++) {
            com.coffeeshop.backend.entity.Order fakeOrder = new com.coffeeshop.backend.entity.Order();

            // Trộn ngẫu nhiên: 60% đơn cho Store 1, 40% đơn cho Store 2
            boolean isStore1 = rand.nextInt(100) < 60;
            fakeOrder.setStore(isStore1 ? store1 : store2);
            fakeOrder.setUser(isStore1 ? staff1 : staff2);

            // Random số tiền từ 30k đến 250k
            long randomAmount = 30000L + (rand.nextInt(22) * 10000L);
            fakeOrder.setTotalPrice(new java.math.BigDecimal(randomAmount));
            fakeOrder.setStatus(com.coffeeshop.backend.enums.OrderStatus.PAID);

            // Lùi thời gian ngẫu nhiên trong vòng 7 ngày qua để lịch sử trông thật hơn
            java.time.LocalDateTime randomTime = java.time.LocalDateTime.now().minusHours(rand.nextInt(100));
            fakeOrder.setOrderDate(randomTime);
            fakeOrder.setCreatedAt(randomTime);

            em.persist(fakeOrder); // Lưu xuống DB
        }
        log.info("✅ Đã bơm xong 30 hóa đơn ảo vào DB!");
    }

    private Store createStore(String name, String address, String phone, double lat, double lng) {
        Store store = new Store();
        store.setName(name);
        store.setAddress(address);
        store.setPhone(phone);
        store.setLatitude(lat);
        store.setLongitude(lng);
        return storeRepository.save(store);
    }

    private User createUser(String fullName, String email, String password, UserRole role, String phone, Store store) {
        User user = new User();
        user.setFullname(fullName);
        user.setEmail(email);
        user.setPassword(password); // Lưu plain-text để test
        user.setRole(role);
        user.setPhone(phone);
        user.setStore(store);
        return userRepository.save(user);
    }

    private Category createCategory(String name, String slug, String image) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setImage("/" + image);
        return categoryRepository.save(category);
    }

    private ProductVariant createVariant(String size, String price, boolean isActive) {
        ProductVariant variant = new ProductVariant();
        variant.setSku(size.toUpperCase() + "-" + price);
        variant.setSize(size);
        variant.setPrice(new BigDecimal(price));
        variant.setIsActive(isActive);
        return variant;
    }

    private void createProduct(String name, String description, String image, Category category,
            boolean isActive, List<ProductVariant> variants, List<Store> stores) {

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl("/" + image);
        product.setCategory(category);
        product.setIsActive(isActive);
        Product savedProduct = productRepository.save(product);

        for (ProductVariant variant : variants) {
            variant.setProduct(savedProduct);
            ProductVariant savedVariant = productVariantRepository.save(variant);

            for (Store store : stores) {
                ProductStock productStock = new ProductStock();
                productStock.setProductVariant(savedVariant);
                productStock.setStore(store);
                productStock.setQuantity(1000);
                productStockRepository.save(productStock);

                StockHistory history = new StockHistory();
                history.setProductVariant(savedVariant);
                history.setStore(store);
                history.setQuantityChanged(1000);
                history.setCurrentQuantity(1000);
                history.setReason("INITIAL_STOCK");
                stockHistoryRepository.save(history);
            }
        }
    }
}