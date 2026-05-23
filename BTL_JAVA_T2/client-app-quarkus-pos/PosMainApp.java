import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class PosMainApp extends JFrame {

    // -------------------------------------------------------------------------
    // Bảng màu
    // -------------------------------------------------------------------------
    private static final Color COLOR_BG_MAIN = new Color(245, 246, 250);
    private static final Color COLOR_BG_PANEL = new Color(255, 255, 255);
    private static final Color COLOR_BG_HEADER = new Color(44, 62, 80);
    private static final Color COLOR_ACCENT = new Color(41, 128, 185);
    private static final Color COLOR_ACCENT_HOVER = new Color(21, 101, 156);
    private static final Color COLOR_DRINK_BTN = new Color(255, 255, 255);
    private static final Color COLOR_DRINK_HOVER = new Color(232, 244, 253);
    private static final Color COLOR_DRINK_BORDER = new Color(189, 195, 199);
    private static final Color COLOR_TOTAL_RED = new Color(192, 57, 43);
    private static final Color COLOR_CASH_BTN = new Color(39, 174, 96);
    private static final Color COLOR_CASH_HOVER = new Color(27, 142, 73);
    private static final Color COLOR_TABLE_STRIPE = new Color(248, 249, 250);
    private static final Color COLOR_TABLE_HEADER = new Color(52, 73, 94);
    private static final Color COLOR_CLEAR_BTN = new Color(231, 76, 60);
    private static final Color COLOR_CLEAR_HOVER = new Color(192, 57, 43);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DRINK_BTN = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TABLE_HDR = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_TOTAL = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_PAY_BTN = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_CLEAR_BTN = new Font("Segoe UI", Font.BOLD, 13);

    private static class DrinkItem {
        String name;
        long price;
        String category;

        public DrinkItem(String name, long price, String category) {
            this.name = name;
            this.price = price;
            this.category = category;
        }
    }

    private java.util.List<DrinkItem> dynamicMenu = new java.util.ArrayList<>();
    private java.util.List<String> dynamicCategories = new java.util.ArrayList<>();
    private String currentCategory = "Tất cả";
    private JPanel categoryContainer;

    private void playTingSound() {
        try {
            java.io.File soundFile = new java.io.File("ping.wav");
            if (soundFile.exists()) {
                javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem
                        .getAudioInputStream(soundFile);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchMenuDataFromApi() {
        try {
            URL catUrl = new URL("http://localhost:8081/api/v1/categories/all");
            Scanner s1 = new Scanner(catUrl.openStream(), "UTF-8").useDelimiter("\\A");
            String catJson = s1.hasNext() ? s1.next() : "[]";

            dynamicCategories.clear();
            dynamicCategories.add("Tất cả");
            java.util.regex.Matcher mCat = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"").matcher(catJson);
            while (mCat.find()) {
                dynamicCategories.add(mCat.group(1));
            }

            URL prodUrl = new URL("http://localhost:8081/api/v1/products/all");
            Scanner s2 = new Scanner(prodUrl.openStream(), "UTF-8").useDelimiter("\\A");
            String prodJson = s2.hasNext() ? s2.next() : "[]";

            dynamicMenu.clear();
            Pattern p = Pattern.compile(
                    "\"name\":\"([^\"]+)\".*?\"category\":\\{.*?\"name\":\"([^\"]+)\"\\}.*?\"price\":([\\d\\.]+)");
            java.util.regex.Matcher mProd = p.matcher(prodJson);

            while (mProd.find()) {
                String name = mProd.group(1);
                String cat = mProd.group(2);
                long price = (long) Double.parseDouble(mProd.group(3));
                dynamicMenu.add(new DrinkItem(name, price, cat));
            }

            if (dynamicMenu.isEmpty()) {
                dynamicMenu.add(new DrinkItem("Dữ liệu mẫu: Cà phê", 20000, "Cà phê"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi kết nối API: " + e.getMessage());
        }
    }

    private DefaultTableModel tableModel;
    private JTable cartTable;
    private JLabel totalLabel;
    private JPanel menuGrid;
    private LoginDialog loginDlg;

    public JButton btnDashboard;
    public JButton btnManageProduct;
    public JButton btnInventory;
    public JButton btnHR;
    public JButton btnShift;
    public JButton btnAudit;

    public static boolean isShiftOpen = false;
    public static long startingCash = 0;

    private static final NumberFormat VND_FORMAT;
    static {
        VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));
        VND_FORMAT.setGroupingUsed(true);
    }

    public PosMainApp() {
        setTitle("POS - Hệ thống Bán hàng");
        java.awt.Image appIcon = new javax.swing.ImageIcon("logo.png").getImage();
        setIconImage(appIcon);
        fetchMenuDataFromApi();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setPreferredSize(new Dimension(1400, 800));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG_MAIN);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(COLOR_BG_MAIN);
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(root);

        root.add(buildHeaderBar(), BorderLayout.NORTH);

        JPanel posPanel = new JPanel(new BorderLayout(15, 15));
        posPanel.setBackground(COLOR_BG_MAIN);
        posPanel.add(buildMenuPanel(), BorderLayout.WEST);
        posPanel.add(buildCartPanel(), BorderLayout.CENTER);

        root.add(posPanel, BorderLayout.CENTER);
        this.getRootPane().putClientProperty("posPanel", posPanel);
        pack();
    }

    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(COLOR_BG_HEADER);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel(" QUÁN CÀ PHÊ - HỆ THỐNG BÁN HÀNG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Point of Sale");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(189, 195, 199));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);
        bar.add(left, BorderLayout.WEST);

        JButton btnHistory = buildStyledButton("Lịch sử", new Color(52, 73, 94), COLOR_ACCENT, Color.WHITE,
                new Font("Segoe UI", Font.BOLD, 14));
        btnHistory.addActionListener(e -> new OrderHistoryDialog(this).setVisible(true));

        btnDashboard = buildStyledButton("Dashboard", new Color(52, 73, 94), COLOR_ACCENT, Color.WHITE,
                new Font("Segoe UI", Font.BOLD, 14));
        btnDashboard.addActionListener(e -> new ReportDialog(this).setVisible(true));

        btnManageProduct = buildStyledButton(" Sản phẩm", new Color(52, 73, 94), COLOR_ACCENT, Color.WHITE,
                new Font("Segoe UI", Font.BOLD, 14));
        btnManageProduct.addActionListener(e -> new ProductManagerDialog(this).setVisible(true));

        btnInventory = buildStyledButton("Kho", new Color(52, 73, 94), COLOR_ACCENT, Color.WHITE,
                new Font("Segoe UI", Font.BOLD, 14));
        btnInventory.addActionListener(e -> new InventoryManagerDialog(this).setVisible(true));

        btnHR = buildStyledButton("Nhân sự", new Color(52, 73, 94), COLOR_ACCENT, Color.WHITE,
                new Font("Segoe UI", Font.BOLD, 14));
        btnHR.addActionListener(e -> new HRManagerDialog(this).setVisible(true));

        btnShift = buildStyledButton(" Ca", new Color(52, 73, 94), COLOR_ACCENT, Color.WHITE,
                new Font("Segoe UI", Font.BOLD, 14));
        btnShift.addActionListener(e -> new ShiftManagerDialog(this).setVisible(true));

        btnAudit = buildStyledButton("Nhật ký", new Color(192, 57, 43), new Color(231, 76, 60), Color.WHITE,
                new Font("Segoe UI", Font.BOLD, 14));
        btnAudit.addActionListener(e -> new AuditManagerDialog(this).setVisible(true));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnShift);
        right.add(btnHistory);
        right.add(btnDashboard);
        right.add(btnManageProduct);
        right.add(btnInventory);
        right.add(btnHR);
        right.add(btnAudit);

        bar.add(right, BorderLayout.CENTER);
        return bar;
    }

    private JPanel buildMenuPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(COLOR_BG_PANEL);
        wrapper.setPreferredSize(new Dimension(700, 0));
        wrapper.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setBackground(COLOR_BG_PANEL);

        JLabel menuTitle = new JLabel("🥤 DANH MỤC ĐỒ UỐNG");
        menuTitle.setFont(FONT_TITLE);
        menuTitle.setForeground(COLOR_BG_HEADER);
        northPanel.add(menuTitle, BorderLayout.NORTH);

        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loadMenu(txtSearch.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loadMenu(txtSearch.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                loadMenu(txtSearch.getText());
            }
        });

        JPanel searchBox = new JPanel(new BorderLayout(5, 0));
        searchBox.setBackground(COLOR_BG_PANEL);
        searchBox.add(new JLabel("🔍 Nhập món cần tìm: "), BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        categoryContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        categoryContainer.setBackground(COLOR_BG_PANEL);
        updateCategoryButtons(txtSearch);

        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBackground(COLOR_BG_PANEL);
        filterPanel.add(searchBox, BorderLayout.NORTH);
        filterPanel.add(categoryContainer, BorderLayout.SOUTH);

        northPanel.add(filterPanel, BorderLayout.SOUTH);
        wrapper.add(northPanel, BorderLayout.NORTH);

        menuGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        menuGrid.setBackground(COLOR_BG_PANEL);
        loadMenu("");

        JScrollPane scroll = new JScrollPane(menuGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(COLOR_BG_PANEL);
        scroll.getViewport().setBackground(COLOR_BG_PANEL);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private void updateCategoryButtons(JTextField txtSearch) {
        categoryContainer.removeAll();
        for (String cat : dynamicCategories) {
            JButton btnCat = buildStyledButton(cat, new Color(236, 240, 241), COLOR_ACCENT, COLOR_BG_HEADER,
                    new Font("Segoe UI", Font.BOLD, 13));
            btnCat.setPreferredSize(new Dimension(100, 35));
            btnCat.addActionListener(e -> {
                currentCategory = cat;
                loadMenu(txtSearch.getText());
            });
            categoryContainer.add(btnCat);
        }
        categoryContainer.revalidate();
    }

    private void loadMenu(String keyword) {
        menuGrid.removeAll();
        String lowerKeyword = keyword.toLowerCase();

        for (DrinkItem item : dynamicMenu) {
            boolean matchSearch = keyword.isEmpty() || item.name.toLowerCase().contains(lowerKeyword);
            boolean matchCategory = currentCategory.equals("Tất cả") || currentCategory.equals(item.category);

            if (matchSearch && matchCategory) {
                menuGrid.add(buildDrinkButton(item.name, item.price));
            }
        }
        menuGrid.revalidate();
        menuGrid.repaint();
    }

    private JButton buildDrinkButton(String name, long price) {
        String labelHtml = "<html><center><b style='font-size:14px;'>" + name
                + "</b><br/><span style='font-size:12px;color:#2980b9;'>" + formatVnd(price)
                + " VND</span></center></html>";
        JButton btn = new JButton(labelHtml) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 80;
                return d;
            }
        };

        btn.setBackground(COLOR_DRINK_BTN);
        btn.setForeground(COLOR_BG_HEADER);
        btn.setFont(FONT_DRINK_BTN);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(COLOR_DRINK_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_DRINK_HOVER);
                btn.setBorder(new CompoundBorder(new LineBorder(COLOR_ACCENT, 2, true),
                        BorderFactory.createEmptyBorder(7, 7, 7, 7)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_DRINK_BTN);
                btn.setBorder(new CompoundBorder(new LineBorder(COLOR_DRINK_BORDER, 1, true),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
            }
        });

        btn.addActionListener(e -> addToCart(name, price));
        return btn;
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(COLOR_BG_PANEL);
        panel.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel northBar = new JPanel(new BorderLayout());
        northBar.setBackground(COLOR_BG_PANEL);

        JLabel cartTitle = new JLabel("HÓA ĐƠN HIỆN TẠI");
        cartTitle.setFont(FONT_TITLE);
        cartTitle.setForeground(COLOR_BG_HEADER);
        northBar.add(cartTitle, BorderLayout.WEST);

        JButton clearBtn = buildStyledButton("Làm mới", COLOR_CLEAR_BTN, COLOR_CLEAR_HOVER, Color.WHITE,
                FONT_CLEAR_BTN);
        clearBtn.setPreferredSize(new Dimension(110, 36));
        clearBtn.addActionListener(e -> clearCart());
        northBar.add(clearBtn, BorderLayout.EAST);

        panel.add(northBar, BorderLayout.NORTH);
        panel.add(buildCartTable(), BorderLayout.CENTER);
        panel.add(buildCheckoutPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JScrollPane buildCartTable() {
        String[] columns = { "Tên món", "SL", "Đơn giá (VNĐ)", "Thành tiền (VNĐ)" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1)
                    return Integer.class;
                if (columnIndex == 2 || columnIndex == 3)
                    return Long.class;
                return String.class;
            }
        };

        cartTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_TABLE_STRIPE);
                }
                return c;
            }
        };

        cartTable.setFont(FONT_TABLE_CELL);
        cartTable.setRowHeight(40);
        cartTable.setGridColor(new Color(230, 230, 230));
        cartTable.setSelectionBackground(new Color(210, 234, 255));
        cartTable.setSelectionForeground(COLOR_BG_HEADER);

        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Long) {
                    setText(formatVnd((Long) value));
                }
                return c;
            }
        };
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        cartTable.getColumnModel().getColumn(1).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        cartTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JTableHeader header = cartTable.getTableHeader();
        header.setPreferredSize(new Dimension(100, 35));

        javax.swing.table.DefaultTableCellRenderer headerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        headerRenderer.setBackground(COLOR_TABLE_HEADER);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(FONT_TABLE_HDR);
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < cartTable.getColumnModel().getColumnCount(); i++) {
            cartTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        return new JScrollPane(cartTable);
    }

    private JPanel buildCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_BG_PANEL);
        panel.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)));

        totalLabel = new JLabel("Tổng tiền: 0 VNĐ");
        totalLabel.setFont(FONT_TOTAL);
        totalLabel.setForeground(COLOR_TOTAL_RED);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(totalLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(COLOR_BG_PANEL);

        JButton cashBtn = buildStyledButton(" Thanh toán Tiền mặt", COLOR_CASH_BTN, COLOR_CASH_HOVER, Color.WHITE,
                FONT_PAY_BTN);
        cashBtn.addActionListener(e -> handlePayment("Tien mat"));

        JButton qrBtn = buildStyledButton(" Thanh toán VietQR", COLOR_ACCENT, COLOR_ACCENT_HOVER, Color.WHITE,
                FONT_PAY_BTN);
        qrBtn.addActionListener(e -> handleVietQRPayment());

        btnPanel.add(cashBtn);
        btnPanel.add(qrBtn);

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setBackground(COLOR_BG_PANEL);
        btnWrapper.setPreferredSize(new Dimension(0, 60));
        btnWrapper.add(btnPanel, BorderLayout.CENTER);

        panel.add(btnWrapper, BorderLayout.SOUTH);
        return panel;
    }

    private void addToCart(String name, long unitPrice) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (tableModel.getValueAt(row, 0).equals(name)) {
                int qty = (Integer) tableModel.getValueAt(row, 1);
                qty++;
                tableModel.setValueAt(qty, row, 1);
                tableModel.setValueAt(unitPrice * qty, row, 3);
                updateTotalAmount();
                return;
            }
        }
        tableModel.addRow(new Object[] { name, 1, unitPrice, unitPrice });
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        totalLabel.setText("Tong tien: " + formatVnd(calcTotal()) + " VND");
    }

    private void clearCart() {
        tableModel.setRowCount(0);
        totalLabel.setText("Tong tien: 0 VND");
    }

    private void handlePayment(String method) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống! Vui lòng chọn đồ uống trước.", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        long total = calcTotal();
        StringBuilder itemsJson = new StringBuilder("[");
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            int qty = (Integer) tableModel.getValueAt(i, 1);
            long price = (Long) tableModel.getValueAt(i, 2);
            itemsJson.append(String.format("{\"name\":\"%s\", \"quantity\":%d, \"price\":%d}", name, qty, price));
            if (i < tableModel.getRowCount() - 1)
                itemsJson.append(",");
        }
        itemsJson.append("]");
        String jsonPayload = String.format("{\"totalPrice\": %d, \"storeId\": 1, \"items\": %s}", total,
                itemsJson.toString());

        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        new javax.swing.SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                java.net.URL url = new java.net.URL("http://localhost:8081/api/reports/save-order");
                java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                try (java.io.OutputStream os = con.getOutputStream()) {
                    os.write(jsonPayload.getBytes("UTF-8"));
                }

                if (con.getResponseCode() == 200) {
                    java.util.Scanner s = new java.util.Scanner(con.getInputStream(), "UTF-8").useDelimiter("\\A");
                    String orderId = extractVal(s.hasNext() ? s.next() : "", "id").trim();
                    java.net.URL urlItems = new java.net.URL(
                            "http://localhost:8081/api/reports/history/" + orderId + "/items");
                    java.util.Scanner s2 = new java.util.Scanner(urlItems.openStream(), "UTF-8").useDelimiter("\\A");
                    return new String[] { orderId, s2.hasNext() ? s2.next() : "[]" };
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(java.awt.Cursor.getDefaultCursor());
                try {
                    String[] result = get();
                    if (result != null && result.length == 2) {
                        playTingSound();
                        JOptionPane.showMessageDialog(PosMainApp.this, "Thanh toán thành công!\nPhương thức: " + method
                                + "\nTổng tiền: " + formatVnd(total) + " VND");
                        exportInvoice(result[0], result[1], total);
                        clearCart();
                    } else {
                        JOptionPane.showMessageDialog(PosMainApp.this, "Lỗi: Không thể lưu đơn hàng lên Server!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PosMainApp.this, "Lỗi kết nối Backend!");
                }
            }
        }.execute();
    }

    private void handleVietQRPayment() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!");
            return;
        }

        long total = calcTotal();
        long orderCode = System.currentTimeMillis() / 1000;

        new Thread(() -> {
            try {
                String jsonInputString = String.format(
                        "{\"orderCode\": %d, \"amount\": %d, \"description\": \"Thanh toan don %d\"}", orderCode, total,
                        orderCode);
                java.net.URL url = new java.net.URL("http://localhost:8081/api/payments/payos/create");
                java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                try (java.io.OutputStream os = con.getOutputStream()) {
                    os.write(jsonInputString.getBytes("utf-8"));
                }

                java.util.Scanner s = new java.util.Scanner(con.getInputStream());
                String response = s.useDelimiter("\\A").hasNext() ? s.next() : "";

                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"qrCode\"\\s*:\\s*\"([^\"]+)\"")
                        .matcher(response);
                if (matcher.find()) {
                    String encodedQrData = java.net.URLEncoder.encode(matcher.group(1), "UTF-8").replace("+", "%20");
                    String finalImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=350x350&data="
                            + encodedQrData;
                    java.awt.Image image = javax.imageio.ImageIO.read(new java.net.URL(finalImageUrl));

                    SwingUtilities.invokeLater(() -> {
                        JDialog qrDialog = new JDialog((Frame) null, "Thanh toán qua VietQR", true);
                        qrDialog.setLayout(new BorderLayout());
                        qrDialog.getContentPane().setBackground(Color.WHITE);

                        JPanel topPanel = new JPanel(new GridLayout(3, 1, 0, 5));
                        topPanel.setBackground(Color.WHITE);
                        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

                        JLabel lblTitle = new JLabel("QUÉT MÃ ĐỂ THANH TOÁN", SwingConstants.CENTER);
                        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                        JLabel lblAmount = new JLabel("Số tiền: " + formatVnd(total) + " VND", SwingConstants.CENTER);
                        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 26));
                        lblAmount.setForeground(new Color(192, 57, 43));
                        JLabel lblDesc = new JLabel("Nội dung: Thanh toan don " + orderCode, SwingConstants.CENTER);
                        topPanel.add(lblTitle);
                        topPanel.add(lblAmount);
                        topPanel.add(lblDesc);

                        JLabel lblQr = new JLabel(new ImageIcon(image), SwingConstants.CENTER);
                        lblQr.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

                        JPanel bottomPanel = new JPanel(new BorderLayout());
                        bottomPanel.setBackground(Color.WHITE);
                        JLabel lblStatus = new JLabel("⏳ Đang chờ khách hàng quét mã...", SwingConstants.CENTER);
                        bottomPanel.add(lblStatus, BorderLayout.CENTER);

                        qrDialog.add(topPanel, BorderLayout.NORTH);
                        qrDialog.add(lblQr, BorderLayout.CENTER);
                        qrDialog.add(bottomPanel, BorderLayout.SOUTH);
                        qrDialog.pack();
                        qrDialog.setLocationRelativeTo(PosMainApp.this);

                        javax.swing.Timer pollingTimer = new javax.swing.Timer(3000, e -> {
                            try {
                                java.net.URL statusUrl = new java.net.URL(
                                        "http://localhost:8081/api/payments/payos/status/" + orderCode);
                                java.net.HttpURLConnection statusCon = (java.net.HttpURLConnection) statusUrl
                                        .openConnection();
                                statusCon.setRequestMethod("GET");
                                java.util.Scanner scanner = new java.util.Scanner(statusCon.getInputStream());
                                String res = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

                                if (res.contains("\"status\":\"PAID\"")) {
                                    ((javax.swing.Timer) e.getSource()).stop();
                                    qrDialog.dispose();
                                    saveOrderToBackend(total);
                                    playTingSound();
                                    JOptionPane.showMessageDialog(PosMainApp.this, "Giao dịch thành công!");
                                    clearCart();
                                }
                            } catch (Exception ex) {
                            }
                        });

                        qrDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                                pollingTimer.stop();
                            }
                        });
                        pollingTimer.start();
                        qrDialog.setVisible(true);
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(this, "Lỗi kết nối PayOS!"));
            }
        }, "payos-thread").start();
    }

    private String[] saveOrderToBackend(long total) {
        try {
            StringBuilder itemsJson = new StringBuilder("[");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String name = (String) tableModel.getValueAt(i, 0);
                int qty = (Integer) tableModel.getValueAt(i, 1);
                long price = (Long) tableModel.getValueAt(i, 2);
                itemsJson.append(String.format("{\"name\":\"%s\", \"quantity\":%d, \"price\":%d}", name, qty, price));
                if (i < tableModel.getRowCount() - 1)
                    itemsJson.append(",");
            }
            itemsJson.append("]");
            String jsonPayload = String.format("{\"totalPrice\": %d, \"storeId\": 1, \"items\": %s}", total,
                    itemsJson.toString());

            java.net.URL url = new java.net.URL("http://localhost:8081/api/reports/save-order");
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            try (java.io.OutputStream os = con.getOutputStream()) {
                os.write(jsonPayload.getBytes("UTF-8"));
            }

            if (con.getResponseCode() == 200) {
                java.util.Scanner s = new java.util.Scanner(con.getInputStream(), "UTF-8").useDelimiter("\\A");
                String orderId = extractVal(s.hasNext() ? s.next() : "", "id").trim();
                java.net.URL urlItems = new java.net.URL(
                        "http://localhost:8081/api/reports/history/" + orderId + "/items");
                java.util.Scanner s2 = new java.util.Scanner(urlItems.openStream(), "UTF-8").useDelimiter("\\A");
                return new String[] { orderId, s2.hasNext() ? s2.next() : "[]" };
            }
        } catch (Exception e) {
        }
        return null;
    }

    private long calcTotal() {
        long total = 0;
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Object val = tableModel.getValueAt(row, 3);
            if (val instanceof Long)
                total += (Long) val;
        }
        return total;
    }

    private String formatVnd(long amount) {
        return VND_FORMAT.format(amount);
    }

    private JButton buildStyledButton(String text, Color bg, Color hover, Color fg, Font font) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(font);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.repaint();
            }
        });
        return btn;
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> {
            PosMainApp app = new PosMainApp();
            if (app.btnDashboard != null)
                app.btnDashboard.setVisible(false);
            if (app.btnManageProduct != null)
                app.btnManageProduct.setVisible(false);
            if (app.btnHR != null)
                app.btnHR.setVisible(false);
            if (app.btnInventory != null)
                app.btnInventory.setVisible(false);
            if (app.btnShift != null)
                app.btnShift.setVisible(true);
            if (app.btnAudit != null)
                app.btnAudit.setVisible(false);

            app.loginDlg = app.new LoginDialog(app);
            app.loginDlg.setVisible(true);

            if (app.loginDlg.isSucceeded()) {
                String role = app.loginDlg.getRole();
                if (role != null && role.toUpperCase().contains("ADMIN")) {
                    if (app.btnDashboard != null)
                        app.btnDashboard.setVisible(true);
                    if (app.btnManageProduct != null)
                        app.btnManageProduct.setVisible(true);
                    if (app.btnHR != null)
                        app.btnHR.setVisible(true);
                    if (app.btnInventory != null)
                        app.btnInventory.setVisible(true);
                    if (app.btnShift != null)
                        app.btnShift.setVisible(true);
                    if (app.btnAudit != null)
                        app.btnAudit.setVisible(true);

                    JPanel posPanel = (JPanel) app.getRootPane().getClientProperty("posPanel");
                    if (posPanel != null) {
                        app.getContentPane().remove(posPanel);
                    }

                    app.setTitle("QUẢN TRỊ VIÊN - " + app.loginDlg.getStoreName() + " | Admin: "
                            + app.loginDlg.getFullName());
                    app.getContentPane().add(app.new ReportDialog(app).getContentPane(), BorderLayout.CENTER);

                    app.getContentPane().revalidate();
                    app.getContentPane().repaint();
                    SwingUtilities.updateComponentTreeUI(app);
                } else {
                    app.setTitle(
                            "POS - " + app.loginDlg.getStoreName() + " | Nhân viên: " + app.loginDlg.getFullName());
                }
                app.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }

    class LoginDialog extends JDialog {
        private JTextField txtEmail = new JTextField("admin@gmail.com", 20); // Mặc định mở tài khoản admin cho bạn test
        private JPasswordField txtPass = new JPasswordField("123456", 20);
        private boolean succeeded = false;
        private String userRole;
        private String storeName;
        private String fullName;

        public LoginDialog(Frame parent) {
            super(parent, "Đăng nhập hệ thống POS", true);
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();
            cs.fill = GridBagConstraints.HORIZONTAL;
            cs.insets = new java.awt.Insets(10, 10, 10, 10);
            cs.gridx = 0;
            cs.gridy = 0;
            panel.add(new JLabel("Email: "), cs);
            cs.gridx = 1;
            cs.gridy = 0;
            panel.add(txtEmail, cs);
            cs.gridx = 0;
            cs.gridy = 1;
            panel.add(new JLabel("Mật khẩu: "), cs);
            cs.gridx = 1;
            cs.gridy = 1;
            panel.add(txtPass, cs);

            JButton btnLogin = new JButton("Đăng nhập");
            btnLogin.addActionListener(e -> authenticate());
            JPanel bp = new JPanel();
            bp.add(btnLogin);

            getContentPane().add(panel, BorderLayout.CENTER);
            getContentPane().add(bp, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
        }

        private void authenticate() {
            try {
                String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", txtEmail.getText(),
                        new String(txtPass.getPassword()));
                URL url = new URL("http://localhost:8081/api/v1/auth/login");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }

                int status = con.getResponseCode();
                if (status == 200) {
                    Scanner s = new Scanner(con.getInputStream(), "UTF-8").useDelimiter("\\A");
                    String resp = s.hasNext() ? s.next() : "";
                    this.fullName = extractJsonString(resp, "username");
                    this.storeName = extractJsonString(resp, "storeName");
                    this.userRole = extractJsonArray(resp, "roles");
                    if (this.userRole == null || this.userRole.isEmpty())
                        this.userRole = extractJsonString(resp, "role");
                    if (this.userRole == null || this.userRole.isEmpty())
                        this.userRole = resp.toUpperCase();
                    succeeded = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Đăng nhập thất bại (HTTP " + status + ")", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không kết nối được Backend!");
            }
        }

        private String extractJsonString(String json, String key) {
            Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
            return m.find() ? m.group(1) : "";
        }

        private String extractJsonArray(String json, String key) {
            Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[\\s*\"([^\"]*)\"").matcher(json);
            return m.find() ? m.group(1) : "";
        }

        public boolean isSucceeded() {
            return succeeded;
        }

        public String getStoreName() {
            return storeName;
        }

        public String getFullName() {
            return fullName;
        }

        public String getRole() {
            return userRole;
        }
    }

    class ReportDialog extends JDialog {
        java.util.List<String> storeNames = new java.util.ArrayList<>();
        java.util.List<Long> revenues = new java.util.ArrayList<>();
        long maxRevenue = 0;

        public ReportDialog(Frame parent) {
            super(parent, "Dashboard Quản trị: Doanh Thu Chuỗi", true);
            setSize(700, 500);
            setLocationRelativeTo(parent);
            fetchData();
            add(new ChartPanel());
        }

        private void fetchData() {
            try {
                java.net.URL url = new java.net.URL("http://localhost:8081/api/reports/store-comparison");
                java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                java.util.Scanner s = new java.util.Scanner(con.getInputStream());
                String json = s.useDelimiter("\\A").hasNext() ? s.next() : "";

                java.util.regex.Matcher mName = java.util.regex.Pattern.compile("\"storeName\":\"([^\"]+)\"")
                        .matcher(json);
                java.util.regex.Matcher mRev = java.util.regex.Pattern.compile("\"totalRevenue\":(\\d+)").matcher(json);
                while (mName.find() && mRev.find()) {
                    storeNames.add(mName.group(1));
                    long rev = Long.parseLong(mRev.group(1));
                    revenues.add(rev);
                    if (rev > maxRevenue)
                        maxRevenue = rev;
                }
            } catch (Exception e) {
            }
        }

        class ChartPanel extends JPanel {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth(), height = getHeight(), padding = 60, labelPadding = 30;
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, width, height);
                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(padding, height - padding - labelPadding, padding, padding);
                g2.drawLine(padding, height - padding - labelPadding, width - padding, height - padding - labelPadding);
                if (revenues.isEmpty() || maxRevenue == 0) {
                    g2.drawString("Đang tải dữ liệu...", width / 2 - 50, height / 2);
                    return;
                }
                int barWidth = (width - 2 * padding) / revenues.size() - 40;
                for (int i = 0; i < revenues.size(); i++) {
                    int x = padding + i * (barWidth + 40) + 20;
                    int barHeight = (int) ((double) revenues.get(i) / maxRevenue
                            * (height - 2 * padding - labelPadding - 40));
                    int y = height - padding - labelPadding - barHeight;
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(x + 5, y + 5, barWidth, barHeight);
                    g2.setColor(new Color(41, 128, 185));
                    g2.fillRect(x, y, barWidth, barHeight);
                    g2.setColor(Color.RED);
                    g2.setFont(new Font("Arial", Font.BOLD, 14));
                    g2.drawString(String.format("%,d đ", revenues.get(i)), x, y - 10);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Arial", Font.PLAIN, 13));
                    g2.drawString(storeNames.get(i), x, height - padding);
                }
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.setColor(new Color(44, 62, 80));
                g2.drawString("BIỂU ĐỒ SO SÁNH DOANH THU CHUỖI", width / 2 - 180, 30);
            }
        }
    }

    class OrderHistoryDialog extends JDialog {
        private DefaultTableModel tableModel;

        public OrderHistoryDialog(Frame parent) {
            super(parent, "Lịch sử Giao dịch", true);
            setSize(800, 500);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout(10, 10));
            getContentPane().setBackground(COLOR_BG_MAIN);
            String[] columns = { "Mã Đơn", "Thời gian", "Cửa hàng", "Tổng tiền", "Trạng thái" };
            tableModel = new DefaultTableModel(columns, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            JTable historyTable = new JTable(tableModel);
            historyTable.setRowHeight(30);
            historyTable.getTableHeader().setBackground(new Color(52, 73, 94));
            historyTable.getTableHeader().setForeground(Color.WHITE);
            add(new JScrollPane(historyTable), BorderLayout.CENTER);
            JButton btnRefresh = buildStyledButton("Làm mới dữ liệu", COLOR_ACCENT, COLOR_ACCENT_HOVER, Color.WHITE,
                    new Font("Segoe UI", Font.BOLD, 14));
            btnRefresh.addActionListener(e -> fetchHistoryData());
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.add(btnRefresh);
            add(bottomPanel, BorderLayout.SOUTH);
            fetchHistoryData();
        }

        private void fetchHistoryData() {
            tableModel.setRowCount(0);
            try {
                java.net.URL url = new java.net.URL("http://localhost:8081/api/reports/history");
                java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                java.util.Scanner s = new java.util.Scanner(con.getInputStream());
                String json = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                java.util.regex.Matcher mObj = java.util.regex.Pattern.compile("\\{(.*?)\\}").matcher(json);
                while (mObj.find()) {
                    String obj = mObj.group(1);
                    tableModel.addRow(new Object[] { "#" + extractVal(obj, "id"), extractVal(obj, "orderDate"),
                            extractVal(obj, "storeName"),
                            formatVnd((long) Double.parseDouble(extractVal(obj, "totalPrice"))),
                            extractVal(obj, "status") });
                }
            } catch (Exception e) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Chức năng Xuất Hóa Đơn (BẢN NÂNG CẤP CHI TIẾT CHUẨN SIÊU THỊ/NHÀ HÀNG)
    // -------------------------------------------------------------------------
    private void exportInvoice(String orderId, String json, long total) {
        try {
            // 1. Xây dựng nội dung hóa đơn căn lề chuẩn 42 ký tự máy in nhiệt
            StringBuilder bill = new StringBuilder();
            bill.append("==========================================\n");
            bill.append("          " + (loginDlg != null ? loginDlg.getStoreName().toUpperCase() : "COFFEE POS SYSTEM")
                    + "\n");
            bill.append("      Địa chỉ: 123 Đường Láng, Đống Đa, Hà Nội\n");
            bill.append("          Hotline: 1900.6789 - 0987.654.321\n");
            bill.append("==========================================\n\n");

            bill.append(String.format("Mã hóa đơn: #%-30s\n", orderId));
            bill.append(String.format("Ngày bán:   %-30s\n", java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
            bill.append(String.format("Thu ngân:   %-30s\n",
                    (loginDlg != null ? loginDlg.getFullName() : "Nhân viên quầy")));
            bill.append(String.format("Hình thức:  %-30s\n", "Thanh toán tại quầy"));

            bill.append("------------------------------------------\n");
            bill.append(String.format("%-22s %3s %14s\n", "TÊN MÓN", "SL", "THÀNH TIỀN"));
            bill.append("------------------------------------------\n");

            // Bóc tách JSON để hiển thị cả số lượng, đơn giá gốc và tổng dòng món ăn
            java.util.regex.Matcher mObj = java.util.regex.Pattern.compile("\\{(.*?)\\}").matcher(json);
            while (mObj.find()) {
                String obj = mObj.group(1);
                String name = extractVal(obj, "name");
                int qty = Integer.parseInt(extractVal(obj, "quantity"));
                long price = (long) Double.parseDouble(extractVal(obj, "price"));
                long itemTotal = qty * price;

                // Áp dụng thiết kế dòng in 2 hàng chuyên nghiệp chống tràn/lệch chữ khi tên món
                // quá dài
                bill.append(String.format("%-42s\n", name));
                bill.append(String.format("  %d x %-15s %20s\n", qty, formatVnd(price) + " đ",
                        formatVnd(itemTotal) + " đ"));
            }

            bill.append("------------------------------------------\n");
            bill.append(String.format("Tạm tính: %32s\n", formatVnd(total) + " đ"));
            bill.append(String.format("Giảm giá: %32s\n", "0 đ"));
            bill.append(String.format("Thuế GTGT (Đã gồm VAT 8%): %15s\n", "0 đ"));
            bill.append("------------------------------------------\n");
            bill.append(String.format("TỔNG CỘNG THANH TOÁN: %20s\n", formatVnd(total) + " đ"));
            bill.append("==========================================\n");
            bill.append("          CUSTOMER CARE & SERVICES\n");
            bill.append("      Wifi: CoffeeShop_Free / Pass: 88888888\n");
            bill.append("    Mọi ý kiến đóng góp xin gửi về Hotline.\n");
            bill.append("       CẢM ƠN QUÝ KHÁCH - HẸN GẶP LẠI!\n");
            bill.append("==========================================\n");

            // 2. Tự động lưu vào thư mục "HoaDon_Xuat" để tối ưu UX cho Thu ngân
            java.io.File folder = new java.io.File("HoaDon_Xuat");
            if (!folder.exists()) {
                folder.mkdir();
            }

            java.io.File file = new java.io.File(folder, "HoaDon_" + orderId + ".txt");
            try (java.io.PrintWriter out = new java.io.PrintWriter(file, "UTF-8")) {
                out.print(bill.toString());
            }

            // 3. Tự động bật file vừa xuất lên bằng Notepad (Bật lên ngay lập tức để thu
            // ngân in)
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(this, "Đã lưu hóa đơn tự động tại:\n" + file.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất hóa đơn: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String extractVal(String json, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + key + "\":\"?([^\",}]+)\"?").matcher(json);
        return m.find() ? m.group(1).replace("\"", "") : "";
    }

    // =========================================================
    // CLASS GIAO DIỆN QUẢN LÝ SẢN PHẨM (MASTER DATA)
    // =========================================================
    class ProductManagerDialog extends JDialog {
        private DefaultTableModel tableModel;
        private JTable table;

        class CategoryItem {
            Long id;
            String name;

            public CategoryItem(Long id, String name) {
                this.id = id;
                this.name = name;
            }

            @Override
            public String toString() {
                return name;
            }
        }

        private java.util.List<CategoryItem> categoryList = new java.util.ArrayList<>();

        public ProductManagerDialog(JFrame parent) {
            super(parent, "Trang Quản Trị - Danh mục Sản phẩm", true);
            setSize(900, 550);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            String[] columns = { "ID", "Tên món", "Danh mục", "Giá bán (VND)" };
            tableModel = new DefaultTableModel(columns, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            table.setRowHeight(30);
            table.getTableHeader().setBackground(new Color(52, 73, 94));
            table.getTableHeader().setForeground(Color.WHITE);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.WHITE);
            JButton btnAdd = buildStyledButton("➕ Thêm mới", new Color(39, 174, 96), new Color(27, 142, 73),
                    Color.WHITE, new Font("Segoe UI", Font.BOLD, 13));
            JButton btnEdit = buildStyledButton("✏️ Sửa món", new Color(41, 128, 185), new Color(21, 101, 156),
                    Color.WHITE, new Font("Segoe UI", Font.BOLD, 13));
            JButton btnDelete = buildStyledButton("❌ Ngừng bán", new Color(231, 76, 60), new Color(192, 57, 43),
                    Color.WHITE, new Font("Segoe UI", Font.BOLD, 13));

            btnAdd.addActionListener(e -> showProductForm(null, "", "", ""));
            btnEdit.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0)
                    return;
                showProductForm(tableModel.getValueAt(row, 0).toString(), tableModel.getValueAt(row, 1).toString(),
                        tableModel.getValueAt(row, 3).toString().replaceAll("[^\\d]", ""),
                        tableModel.getValueAt(row, 2).toString());
            });
            btnDelete.addActionListener(e -> apiDisableProduct());

            bottomPanel.add(btnAdd);
            bottomPanel.add(btnEdit);
            bottomPanel.add(btnDelete);
            add(bottomPanel, BorderLayout.SOUTH);
            loadCategoriesFromApi();
            loadProductsFromApi();
        }

        private void loadCategoriesFromApi() {
            categoryList.clear();
            try {
                URL url = new URL("http://localhost:8081/api/v1/categories/all");
                Scanner s = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A");
                Matcher m = Pattern.compile("\"id\":(\\d+).*?\"name\":\"([^\"]+)\"")
                        .matcher(s.hasNext() ? s.next() : "[]");
                while (m.find())
                    categoryList.add(new CategoryItem(Long.parseLong(m.group(1)), m.group(2)));
            } catch (Exception e) {
            }
        }

        private void loadProductsFromApi() {
            tableModel.setRowCount(0);
            try {
                URL url = new URL("http://localhost:8081/api/v1/products/all");
                Scanner s = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A");
                Matcher m = Pattern.compile(
                        "\"id\":(\\d+).*?\"name\":\"([^\"]+)\".*?\"category\":\\{.*?\"name\":\"([^\"]+)\"\\}.*?\"price\":([\\d\\.]+)")
                        .matcher(s.hasNext() ? s.next() : "[]");
                while (m.find())
                    tableModel.addRow(new Object[] { m.group(1), m.group(2), m.group(3),
                            formatVnd((long) Double.parseDouble(m.group(4))) });
            } catch (Exception e) {
            }
        }

        private void showProductForm(String productId, String oldName, String oldPrice, String oldCatName) {
            JTextField txtName = new JTextField(oldName);
            JTextField txtPrice = new JTextField(oldPrice);
            JComboBox<CategoryItem> cbCategory = new JComboBox<>(categoryList.toArray(new CategoryItem[0]));
            if (oldCatName != null) {
                for (int i = 0; i < cbCategory.getItemCount(); i++) {
                    if (cbCategory.getItemAt(i).name.equals(oldCatName)) {
                        cbCategory.setSelectedIndex(i);
                        break;
                    }
                }
            }
            if (JOptionPane.showConfirmDialog(this,
                    new Object[] { "Tên SP:", txtName, "Giá:", txtPrice, "Danh mục:", cbCategory }, "Sản Phẩm",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String jsonPayload = String.format(
                            "{\"name\":\"%s\", \"categoryId\":%d, \"variants\":[{\"sku\":\"SP-%d\", \"size\":\"Mặc định\", \"price\":%d}]}",
                            txtName.getText().trim(), ((CategoryItem) cbCategory.getSelectedItem()).id,
                            System.currentTimeMillis(), Long.parseLong(txtPrice.getText().trim()));
                    HttpURLConnection con = (HttpURLConnection) new URL(
                            "http://localhost:8081/api/v1/products" + (productId == null ? "" : "/" + productId))
                            .openConnection();
                    con.setRequestMethod(productId == null ? "POST" : "PUT");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(jsonPayload.getBytes("UTF-8"));
                    }
                    if (con.getResponseCode() == 200 || con.getResponseCode() == 201) {
                        loadProductsFromApi();
                        fetchMenuDataFromApi();
                        JOptionPane.showMessageDialog(this, "Lưu thành công!");
                    }
                } catch (Exception ex) {
                }
            }
        }

        private void apiDisableProduct() {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            if (JOptionPane.showConfirmDialog(this, "Ngừng bán món này?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://localhost:8081/api/v1/products/"
                                    + tableModel.getValueAt(row, 0) + "/status"))
                            .method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString("{\"isActive\": false}"))
                            .header("Content-Type", "application/json").build();
                    if (client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode() == 200) {
                        loadProductsFromApi();
                        fetchMenuDataFromApi();
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    // =========================================================
    // CLASS QUẢN LÝ NHÂN SỰ
    // =========================================================
    class HRManagerDialog extends JDialog {
        private DefaultTableModel tableModel;
        private JTable table;

        public HRManagerDialog(JFrame parent) {
            super(parent, "Trang Quản Trị - Quản lý Nhân sự", true);
            setSize(900, 550);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            tableModel = new DefaultTableModel(new String[] { "ID", "Họ & Tên", "Email", "Chức vụ", "Trạng thái" }, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            table.setRowHeight(30);
            table.getTableHeader().setBackground(new Color(52, 73, 94));
            table.getTableHeader().setForeground(Color.WHITE);
            add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.WHITE);

            JButton btnAdd = buildStyledButton("➕ Thêm mới", new Color(39, 174, 96), new Color(27, 142, 73),
                    Color.WHITE, new Font("Segoe UI", Font.BOLD, 13));
            JButton btnLock = buildStyledButton("🔒 Khóa User", new Color(231, 76, 60), new Color(192, 57, 43),
                    Color.WHITE, new Font("Segoe UI", Font.BOLD, 13));

            btnAdd.addActionListener(e -> showStaffForm(null, "", "", "CASHIER"));
            btnLock.addActionListener(e -> apiLockUser());
            bottomPanel.add(btnAdd);
            bottomPanel.add(btnLock);
            add(bottomPanel, BorderLayout.SOUTH);
            loadUsersFromApi();
        }

        private void loadUsersFromApi() {
            tableModel.setRowCount(0);
            try {
                Scanner s = new Scanner(new URL("http://localhost:8081/api/v1/users").openStream(), "UTF-8")
                        .useDelimiter("\\A");
                Matcher mObj = Pattern.compile("\\{(.*?)\\}").matcher(s.hasNext() ? s.next() : "[]");
                while (mObj.find()) {
                    String obj = mObj.group(1);
                    tableModel.addRow(new Object[] { extractVal(obj, "id"), extractVal(obj, "fullName"),
                            extractVal(obj, "email"), extractVal(obj, "role"),
                            obj.contains("\"isActive\":true") ? "Đang hoạt động" : "Đã khóa" });
                }
            } catch (Exception e) {
            }
        }

        private void showStaffForm(String userId, String name, String email, String role) {
            JTextField txtName = new JTextField(name);
            JTextField txtEmail = new JTextField(email);
            JComboBox<String> cbRole = new JComboBox<>(new String[] { "CASHIER", "ADMIN" });
            cbRole.setSelectedItem(role);
            if (JOptionPane.showConfirmDialog(this,
                    new Object[] { "Tên:", txtName, "Email:", txtEmail, "Quyền:", cbRole }, "Nhân viên",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String payload = String.format("{\"fullName\":\"%s\", \"email\":\"%s\", \"role\":\"%s\"}",
                            txtName.getText(), txtEmail.getText(), cbRole.getSelectedItem());
                    HttpURLConnection con = (HttpURLConnection) new URL(
                            "http://localhost:8081/api/v1/users" + (userId == null ? "" : "/" + userId))
                            .openConnection();
                    con.setRequestMethod(userId == null ? "POST" : "PUT");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(payload.getBytes("UTF-8"));
                    }
                    if (con.getResponseCode() == 200 || con.getResponseCode() == 201)
                        loadUsersFromApi();
                } catch (Exception ex) {
                }
            }
        }

        private void apiLockUser() {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(
                                "http://localhost:8081/api/v1/users/" + tableModel.getValueAt(row, 0) + "/lock"))
                        .method("PATCH", java.net.http.HttpRequest.BodyPublishers.noBody()).build();
                if (client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode() == 200)
                    loadUsersFromApi();
            } catch (Exception e) {
            }
        }
    }

    // =========================================================
    // CLASS QUẢN LÝ KHO (INVENTORY MODULE)
    // =========================================================
    class InventoryManagerDialog extends JDialog {
        public InventoryManagerDialog(JFrame parent) {
            super(parent, "Trang Quản Trị - Quản lý Kho Nguyên Vật Liệu", true);
            setSize(900, 550);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            DefaultTableModel model = new DefaultTableModel(
                    new String[] { "Mã NVL", "Tên NVL", "Tồn kho", "ĐVT", "Cảnh báo" }, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.getTableHeader().setBackground(new Color(52, 73, 94));
            table.getTableHeader().setForeground(Color.WHITE);
            add(new JScrollPane(table), BorderLayout.CENTER);
            model.addRow(new Object[] { "NVL-001", "Hạt Cà Phê Robusta", "45.5", "Kg", "Bình thường" });
            model.addRow(new Object[] { "NVL-002", "Sữa đặc Ông Thọ", "2", "Hộp", "⚠️ Sắp hết" });
        }
    }

    // =========================================================
    // CLASS QUẢN LÝ CA LÀM VIỆC (SHIFT MODULE)
    // =========================================================
    class ShiftManagerDialog extends JDialog {
        private DefaultTableModel tableModel;
        private JTable table;

        public ShiftManagerDialog(JFrame parent) {
            super(parent, "Trang Quản Trị - Ca làm việc", true);
            setSize(900, 550);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            tableModel = new DefaultTableModel(
                    new String[] { "ID Ca", "Thu Ngân", "Giờ Mở", "Giờ Đóng", "Tiền đầu ca", "Lệch quỹ", "Trạng thái" },
                    0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            table.setRowHeight(30);
            table.getTableHeader().setBackground(new Color(52, 73, 94));
            table.getTableHeader().setForeground(Color.WHITE);
            add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.WHITE);
            JButton btnOpen = buildStyledButton("🔓 Mở Ca", new Color(39, 174, 96), new Color(27, 142, 73), Color.WHITE,
                    new Font("Segoe UI", Font.BOLD, 13));
            JButton btnClose = buildStyledButton("🔒 Đóng Ca", new Color(231, 76, 60), new Color(192, 57, 43),
                    Color.WHITE, new Font("Segoe UI", Font.BOLD, 13));
            btnOpen.addActionListener(e -> apiOpenShift());
            btnClose.addActionListener(e -> apiCloseShift());
            bottomPanel.add(btnOpen);
            bottomPanel.add(btnClose);
            add(bottomPanel, BorderLayout.SOUTH);
            loadShiftsFromApi();
        }

        private void loadShiftsFromApi() {
            tableModel.setRowCount(0);
            try {
                Scanner s = new Scanner(new URL("http://localhost:8081/api/v1/shifts").openStream(), "UTF-8")
                        .useDelimiter("\\A");
                Matcher mObj = Pattern.compile("\\{(.*?)\\}").matcher(s.hasNext() ? s.next() : "[]");
                while (mObj.find()) {
                    String obj = mObj.group(1);
                    tableModel.addRow(new Object[] { extractVal(obj, "id"), extractVal(obj, "cashierName"),
                            extractVal(obj, "startTime"),
                            extractVal(obj, "endTime").equals("null") ? "---" : extractVal(obj, "endTime"),
                            formatVnd((long) Double.parseDouble(
                                    extractVal(obj, "startingCash").isEmpty() ? "0" : extractVal(obj, "startingCash"))),
                            formatVnd((long) Double.parseDouble(extractVal(obj, "difference").isEmpty()
                                    || extractVal(obj, "difference").equals("null") ? "0"
                                            : extractVal(obj, "difference"))),
                            "OPEN".equals(extractVal(obj, "status")) ? " Đang mở" : "Đã chốt" });
                }
            } catch (Exception e) {
            }
        }

        private void apiOpenShift() {
            try {
                String cash = JOptionPane.showInputDialog(this, "Tiền đầu ca:", "500000");
                if (cash == null)
                    return;
                HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8081/api/v1/shifts/open")
                        .openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(String.format("{\"userId\": 1, \"startingCash\": %s}", cash).getBytes("UTF-8"));
                }
                if (con.getResponseCode() == 200 || con.getResponseCode() == 201) {
                    isShiftOpen = true;
                    loadShiftsFromApi();
                    JOptionPane.showMessageDialog(this, "Mở ca thành công!");
                }
            } catch (Exception ex) {
            }
        }

        private void apiCloseShift() {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            try {
                String actual = JOptionPane.showInputDialog(this, "Đếm số TIỀN MẶT THỰC TẾ trong két:");
                if (actual == null)
                    return;
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(
                                "http://localhost:8081/api/v1/shifts/" + tableModel.getValueAt(row, 0) + "/close"))
                        .method("POST",
                                java.net.http.HttpRequest.BodyPublishers
                                        .ofString(String.format("{\"actualCash\": %s}", actual)))
                        .header("Content-Type", "application/json").build();
                if (client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode() == 200) {
                    isShiftOpen = false;
                    loadShiftsFromApi();
                    JOptionPane.showMessageDialog(this, "Đóng ca thành công!");
                }
            } catch (Exception ex) {
            }
        }
    }

    // =========================================================
    // CLASS NHẬT KÝ HỆ THỐNG (AUDIT TRAIL)
    // =========================================================
    class AuditManagerDialog extends JDialog {
        public AuditManagerDialog(JFrame parent) {
            super(parent, "Trang Quản Trị - Nhật ký Hệ thống (Audit Trail)", true);
            setSize(950, 600);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            DefaultTableModel tableModel = new DefaultTableModel(new String[] { "Thời gian", "Người thực hiện",
                    "Hành động", "Bảng dữ liệu", "Dữ liệu CŨ", "Dữ liệu MỚI" }, 0) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable table = new JTable(tableModel);
            table.setRowHeight(35);
            table.getTableHeader().setBackground(new Color(192, 57, 43));
            table.getTableHeader().setForeground(Color.WHITE);
            add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.WHITE);
            JButton btnExport = buildStyledButton("⬇ Xuất báo cáo", new Color(41, 128, 185), new Color(21, 101, 156),
                    Color.WHITE, new Font("Segoe UI", Font.BOLD, 13));
            bottomPanel.add(btnExport);
            add(bottomPanel, BorderLayout.SOUTH);
            tableModel.addRow(new Object[] { "19/05/2026 14:05:12", "admin@gmail.com", "UPDATE", "Product (Gà rán)",
                    "isActive: true", "isActive: false" });
            tableModel.addRow(new Object[] { "19/05/2026 18:00:10", "admin@gmail.com", "UPDATE", "Product (Cà phê đen)",
                    "Giá: 20.000đ", "Giá: 15.000đ (NGHI VẤN)" });
        }
    }
}