package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Order;
import com.baharkiraathanesi.kiraathane.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderDAO {

    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    public int getOrCreateOrderId(int tableId) {
        int orderId = -1;
        final String CHECK_SQL = "SELECT id FROM orders WHERE table_id = ? AND is_paid = FALSE";
        final String CREATE_SQL = "INSERT INTO orders (table_id, is_paid) VALUES (?, FALSE)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                LOGGER.warning("Veritabani baglantisi kurulamadi");
                return orderId;
            }

            try (PreparedStatement checkStmt = conn.prepareStatement(CHECK_SQL)) {
                checkStmt.setInt(1, tableId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getInt("id");
                        return orderId;
                    }
                }
            }

            try (PreparedStatement createStmt = conn.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                createStmt.setInt(1, tableId);
                createStmt.executeUpdate();

                try (ResultSet genKeys = createStmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        orderId = genKeys.getInt(1);
                        TableDAO tableDAO = new TableDAO();
                        tableDAO.updateTableStatus(tableId, true);
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Siparis ID alinirken hata", e);
        }
        return orderId;
    }

    public boolean addProductToOrder(int tableId, int productId, int quantity) {
        int orderId = getOrCreateOrderId(tableId);
        if (orderId == -1) {
            return false;
        }

        final String STOCK_CHECK_SQL = "SELECT stock_qty, name FROM products WHERE id = ?";
        final String PRICE_SQL = "SELECT price FROM products WHERE id = ?";
        final String CHECK_ITEM_SQL = "SELECT id FROM order_items WHERE order_id = ? AND product_id = ?";
        final String UPDATE_ITEM_SQL = "UPDATE order_items SET quantity = quantity + ? WHERE id = ?";
        final String INSERT_ITEM_SQL = "INSERT INTO order_items (order_id, product_id, quantity, price_at_order) VALUES (?, ?, ?, ?)";
        final String UPDATE_TOTAL_SQL = "UPDATE orders SET total_amount = total_amount + ? WHERE id = ?";
        final String UPDATE_STOCK_SQL = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stockCheckStmt = conn.prepareStatement(STOCK_CHECK_SQL)) {
                stockCheckStmt.setInt(1, productId);
                try (ResultSet stockRs = stockCheckStmt.executeQuery()) {
                    if (stockRs.next()) {
                        int currentStock = stockRs.getInt("stock_qty");
                        if (currentStock < quantity) {
                            return false;
                        }
                    }
                }
            }

            double price = 0;
            try (PreparedStatement priceStmt = conn.prepareStatement(PRICE_SQL)) {
                priceStmt.setInt(1, productId);
                try (ResultSet rs = priceStmt.executeQuery()) {
                    if (rs.next()) {
                        price = rs.getDouble("price");
                    }
                }
            }

            try (PreparedStatement checkItemStmt = conn.prepareStatement(CHECK_ITEM_SQL)) {
                checkItemStmt.setInt(1, orderId);
                checkItemStmt.setInt(2, productId);
                try (ResultSet itemRs = checkItemStmt.executeQuery()) {
                    if (itemRs.next()) {
                        int existingItemId = itemRs.getInt("id");
                        try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_ITEM_SQL)) {
                            updateStmt.setInt(1, quantity);
                            updateStmt.setInt(2, existingItemId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement itemStmt = conn.prepareStatement(INSERT_ITEM_SQL)) {
                            itemStmt.setInt(1, orderId);
                            itemStmt.setInt(2, productId);
                            itemStmt.setInt(3, quantity);
                            itemStmt.setDouble(4, price);
                            itemStmt.executeUpdate();
                        }
                    }
                }
            }

            try (PreparedStatement totalStmt = conn.prepareStatement(UPDATE_TOTAL_SQL)) {
                totalStmt.setDouble(1, price * quantity);
                totalStmt.setInt(2, orderId);
                totalStmt.executeUpdate();
            }

            try (PreparedStatement stockStmt = conn.prepareStatement(UPDATE_STOCK_SQL)) {
                stockStmt.setInt(1, quantity);
                stockStmt.setInt(2, productId);
                stockStmt.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Urun siparise eklenirken hata", e);
        }

        return false;
    }

    public void closeOrder(int tableId) {
        final String SQL = "UPDATE orders SET is_paid = TRUE WHERE table_id = ? AND is_paid = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, tableId);
            stmt.executeUpdate();

            TableDAO tableDAO = new TableDAO();
            tableDAO.updateTableStatus(tableId, false);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Siparis kapatilirken hata", e);
        }
    }

    public List<Order> getTodayCompletedOrders() {
        List<Order> orders = new ArrayList<>();

        final String SQL = "SELECT o.id, t.name as table_name, o.total_amount, o.created_at " +
                "FROM orders o " +
                "JOIN tables t ON o.table_id = t.id " +
                "WHERE o.is_paid = TRUE AND DATE(o.created_at) = CURDATE() " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setTableName(rs.getString("table_name"));
                order.setTotal(rs.getDouble("total_amount"));

                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    order.setOrderTime(timestamp.toLocalDateTime().toLocalTime().toString());
                }

                orders.add(order);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Bugunku siparisler alinirken hata", e);
        }

        return orders;
    }

    public boolean hasOpenOrders() {
        final String SQL = "SELECT COUNT(*) as open_count FROM orders WHERE is_paid = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int openCount = rs.getInt("open_count");
                return openCount > 0;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Acik siparisler kontrol edilirken hata", e);
        }

        return false;
    }

    public List<OrderItem> getOrderItems(int tableId) {
        List<OrderItem> items = new ArrayList<>();

        final String SQL = "SELECT oi.id, oi.product_id, p.name as product_name, oi.quantity, " +
                "oi.price_at_order, (oi.quantity * oi.price_at_order) as subtotal " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE o.table_id = ? AND o.is_paid = FALSE " +
                "ORDER BY oi.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price_at_order"));
                    item.setSubtotal(rs.getDouble("subtotal"));
                    items.add(item);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Siparis detaylari alinirken hata", e);
        }

        return items;
    }

    /**
     * Sipariş kaleminin miktarını 1 azaltır.
     * Miktar 0'a düşerse ürün siparişten tamamen silinir.
     * @return true: başarılı, false: hata
     */
    public boolean decreaseItemQuantity(int orderItemId, int productId) {
        final String INFO_SQL = "SELECT order_id, quantity, price_at_order FROM order_items WHERE id = ?";
        final String UPDATE_QTY_SQL = "UPDATE order_items SET quantity = quantity - 1 WHERE id = ?";
        final String UPDATE_TOTAL_SQL = "UPDATE orders SET total_amount = total_amount - ? WHERE id = ?";
        final String UPDATE_STOCK_SQL = "UPDATE products SET stock_qty = stock_qty + 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return false;

            int orderId = -1;
            int currentQty = 0;
            double priceAtOrder = 0;

            try (PreparedStatement infoStmt = conn.prepareStatement(INFO_SQL)) {
                infoStmt.setInt(1, orderItemId);
                try (ResultSet rs = infoStmt.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getInt("order_id");
                        currentQty = rs.getInt("quantity");
                        priceAtOrder = rs.getDouble("price_at_order");
                    }
                }
            }

            if (currentQty <= 1) {
                // Miktar 1 veya daha az, ürünü tamamen sil
                removeOrderItem(orderItemId, productId, 1);
            } else {
                // Miktarı 1 azalt
                try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_QTY_SQL)) {
                    updateStmt.setInt(1, orderItemId);
                    updateStmt.executeUpdate();
                }

                try (PreparedStatement totalStmt = conn.prepareStatement(UPDATE_TOTAL_SQL)) {
                    totalStmt.setDouble(1, priceAtOrder);
                    totalStmt.setInt(2, orderId);
                    totalStmt.executeUpdate();
                }

                try (PreparedStatement stockStmt = conn.prepareStatement(UPDATE_STOCK_SQL)) {
                    stockStmt.setInt(1, productId);
                    stockStmt.executeUpdate();
                }
            }

            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Siparis miktari azaltilirken hata", e);
        }
        return false;
    }

    public void removeOrderItem(int orderItemId, int productId, int quantity) {
        final String INFO_SQL = "SELECT order_id, price_at_order FROM order_items WHERE id = ?";
        final String DELETE_ITEM_SQL = "DELETE FROM order_items WHERE id = ?";
        final String UPDATE_TOTAL_SQL = "UPDATE orders SET total_amount = total_amount - ? WHERE id = ?";
        final String UPDATE_STOCK_SQL = "UPDATE products SET stock_qty = stock_qty + ? WHERE id = ?";
        final String COUNT_SQL = "SELECT COUNT(*) FROM order_items WHERE order_id = ?";
        final String TABLE_SQL = "SELECT table_id FROM orders WHERE id = ?";
        final String DELETE_ORDER_SQL = "DELETE FROM orders WHERE id = ?";
        final String UPDATE_TABLE_SQL = "UPDATE tables SET is_occupied = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                return;
            }

            int orderId = -1;
            double priceAtOrder = 0;

            try (PreparedStatement infoStmt = conn.prepareStatement(INFO_SQL)) {
                infoStmt.setInt(1, orderItemId);
                try (ResultSet rs = infoStmt.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getInt("order_id");
                        priceAtOrder = rs.getDouble("price_at_order");
                    }
                }
            }

            try (PreparedStatement deleteStmt = conn.prepareStatement(DELETE_ITEM_SQL)) {
                deleteStmt.setInt(1, orderItemId);
                deleteStmt.executeUpdate();
            }

            if (orderId > 0) {
                try (PreparedStatement totalStmt = conn.prepareStatement(UPDATE_TOTAL_SQL)) {
                    totalStmt.setDouble(1, priceAtOrder * quantity);
                    totalStmt.setInt(2, orderId);
                    totalStmt.executeUpdate();
                }
            }

            try (PreparedStatement stockStmt = conn.prepareStatement(UPDATE_STOCK_SQL)) {
                stockStmt.setInt(1, quantity);
                stockStmt.setInt(2, productId);
                stockStmt.executeUpdate();
            }

            if (orderId > 0) {
                try (PreparedStatement countStmt = conn.prepareStatement(COUNT_SQL)) {
                    countStmt.setInt(1, orderId);
                    try (ResultSet countRs = countStmt.executeQuery()) {
                        if (countRs.next() && countRs.getInt(1) == 0) {
                            int tableId = -1;

                            try (PreparedStatement tableStmt = conn.prepareStatement(TABLE_SQL)) {
                                tableStmt.setInt(1, orderId);
                                try (ResultSet tableRs = tableStmt.executeQuery()) {
                                    if (tableRs.next()) {
                                        tableId = tableRs.getInt("table_id");
                                    }
                                }
                            }

                            try (PreparedStatement deleteOrderStmt = conn.prepareStatement(DELETE_ORDER_SQL)) {
                                deleteOrderStmt.setInt(1, orderId);
                                deleteOrderStmt.executeUpdate();
                            }

                            if (tableId != -1) {
                                try (PreparedStatement updateTableStmt = conn.prepareStatement(UPDATE_TABLE_SQL)) {
                                    updateTableStmt.setInt(1, tableId);
                                    updateTableStmt.executeUpdate();
                                    LOGGER.info("Siparis bosaldigi icin Masa " + tableId + " bosa cikarildi");
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Siparis kalemi silinirken hata", e);
        }
    }

    public boolean moveOrderToTable(int sourceTableId, int targetTableId) {
        // SQL Sorguları
        final String GET_SOURCE_ORDER = "SELECT id, total_amount FROM orders WHERE table_id = ? AND is_paid = FALSE";
        final String GET_TARGET_ORDER = "SELECT id FROM orders WHERE table_id = ? AND is_paid = FALSE";

        // Hedef boşsa sadece masa ID'sini değiştir
        final String MOVE_ORDER_SQL = "UPDATE orders SET table_id = ? WHERE id = ?";

        // Hedef doluysa kalemleri taşı
        final String MOVE_ITEMS_SQL = "UPDATE order_items SET order_id = ? WHERE order_id = ?";
        final String UPDATE_TARGET_TOTAL = "UPDATE orders SET total_amount = total_amount + ? WHERE id = ?";
        final String DELETE_SOURCE_ORDER = "DELETE FROM orders WHERE id = ?";

        // Masa durumlarını güncelle
        final String UPDATE_TABLE_STATUS = "UPDATE tables SET is_occupied = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            int sourceOrderId = -1;
            double sourceTotal = 0;

            // 1. Kaynak masanın siparişini bul
            try (PreparedStatement stmt = conn.prepareStatement(GET_SOURCE_ORDER)) {
                stmt.setInt(1, sourceTableId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    sourceOrderId = rs.getInt("id");
                    sourceTotal = rs.getDouble("total_amount");
                }
            }

            if (sourceOrderId == -1) {
                conn.rollback();
                return false; // Kaynak masada sipariş yok
            }

            // 2. Hedef masada sipariş var mı?
            int targetOrderId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(GET_TARGET_ORDER)) {
                stmt.setInt(1, targetTableId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    targetOrderId = rs.getInt("id");
                }
            }

            if (targetOrderId == -1) {
                // SENARYO A: HEDEF MASA BOŞ (TAŞIMA)
                // Siparişin masa ID'sini değiştir
                try (PreparedStatement stmt = conn.prepareStatement(MOVE_ORDER_SQL)) {
                    stmt.setInt(1, targetTableId);
                    stmt.setInt(2, sourceOrderId);
                    stmt.executeUpdate();
                }

                // Hedef masayı DOLU yap
                try (PreparedStatement stmt = conn.prepareStatement(UPDATE_TABLE_STATUS)) {
                    stmt.setBoolean(1, true);
                    stmt.setInt(2, targetTableId);
                    stmt.executeUpdate();
                }

            } else {
                // SENARYO B: HEDEF MASA DOLU (BİRLEŞTİRME)
                // Kaynak siparişin kalemlerini hedef siparişe aktar
                try (PreparedStatement stmt = conn.prepareStatement(MOVE_ITEMS_SQL)) {
                    stmt.setInt(1, targetOrderId);
                    stmt.setInt(2, sourceOrderId);
                    stmt.executeUpdate();
                }

                // Hedef siparişin tutarını artır
                try (PreparedStatement stmt = conn.prepareStatement(UPDATE_TARGET_TOTAL)) {
                    stmt.setDouble(1, sourceTotal);
                    stmt.setInt(2, targetOrderId);
                    stmt.executeUpdate();
                }

                // Eski boş sipariş kaydını sil
                try (PreparedStatement stmt = conn.prepareStatement(DELETE_SOURCE_ORDER)) {
                    stmt.setInt(1, sourceOrderId);
                    stmt.executeUpdate();
                }
            }

            // 3. Kaynak masayı BOŞA ÇIKAR (Her iki senaryoda da ortak)
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_TABLE_STATUS)) {
                stmt.setBoolean(1, false);
                stmt.setInt(2, sourceTableId);
                stmt.executeUpdate();
            }

            conn.commit(); // Her şey yolunda, kaydet
            LOGGER.info("Masa " + sourceTableId + " -> Masa " + targetTableId + " tasima basarili.");
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Masa tasima hatasi", e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean makeItemTreat(int sourceItemId) {
        // Gerekli SQL Sorguları
        final String GET_SOURCE_INFO = "SELECT order_id, product_id, quantity, price_at_order FROM order_items WHERE id = ?";

        // Ücretsiz satır var mı kontrolü
        final String CHECK_EXISTING_FREE = "SELECT id FROM order_items WHERE order_id = ? AND product_id = ? AND price_at_order = 0";

        // Varsa artır, yoksa ekle
        final String INCREASE_FREE_QTY = "UPDATE order_items SET quantity = quantity + 1 WHERE id = ?";
        final String INSERT_FREE_ITEM = "INSERT INTO order_items (order_id, product_id, quantity, price_at_order) VALUES (?, ?, 1, 0.0)";

        // Kaynak (Ücretli) satırı yönet
        final String DECREASE_PAID_QTY = "UPDATE order_items SET quantity = quantity - 1 WHERE id = ?";
        final String DELETE_PAID_ROW = "DELETE FROM order_items WHERE id = ?"; // Adet 0 olursa sil

        // Toplam tutarı güncelle
        final String UPDATE_ORDER_TOTAL = "UPDATE orders SET total_amount = total_amount - ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false); // Transaction başlat

            // 1. İşlem yapılacak ücretli ürünün bilgilerini al
            int orderId = -1;
            int productId = -1;
            int currentQty = 0;
            double unitPrice = 0.0;

            try (PreparedStatement stmt = conn.prepareStatement(GET_SOURCE_INFO)) {
                stmt.setInt(1, sourceItemId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    orderId = rs.getInt("order_id");
                    productId = rs.getInt("product_id");
                    currentQty = rs.getInt("quantity");
                    unitPrice = rs.getDouble("price_at_order");
                }
            }

            // Hata kontrolü: Ürün yoksa veya zaten ücretsizse işlem yapma
            if (orderId == -1 || unitPrice == 0) {
                conn.rollback();
                return false;
            }

            // 2. ADIM: İKRAM (HEDEF) KISMINI AYARLA
            // Bu siparişte bu üründen daha önce ikram yapılmış mı?
            int existingFreeItemId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(CHECK_EXISTING_FREE)) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, productId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    existingFreeItemId = rs.getInt("id");
                }
            }

            if (existingFreeItemId != -1) {
                // Zaten ikram satırı var, üzerine 1 ekle
                try (PreparedStatement stmt = conn.prepareStatement(INCREASE_FREE_QTY)) {
                    stmt.setInt(1, existingFreeItemId);
                    stmt.executeUpdate();
                }
            } else {
                // İkram satırı yok, yeni oluştur
                try (PreparedStatement stmt = conn.prepareStatement(INSERT_FREE_ITEM)) {
                    stmt.setInt(1, orderId);
                    stmt.setInt(2, productId);
                    stmt.executeUpdate();
                }
            }

            // 3. ADIM: ÜCRETLİ (KAYNAK) KISMINI AYARLA
            if (currentQty > 1) {
                // 1'den fazla varsa sadece adedini düş
                try (PreparedStatement stmt = conn.prepareStatement(DECREASE_PAID_QTY)) {
                    stmt.setInt(1, sourceItemId);
                    stmt.executeUpdate();
                }
            } else {
                // Tam 1 tane varsa (son ürünse), bu satırı tamamen sil!
                // Çünkü bu satır artık ikram satırına taşındı.
                try (PreparedStatement stmt = conn.prepareStatement(DELETE_PAID_ROW)) {
                    stmt.setInt(1, sourceItemId);
                    stmt.executeUpdate();
                }
            }

            // 4. ADIM: PARA HESABI
            // Sipariş toplamından 1 birim fiyat düş
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_ORDER_TOTAL)) {
                stmt.setDouble(1, unitPrice);
                stmt.setInt(2, orderId);
                stmt.executeUpdate();
            }

            conn.commit();
            LOGGER.info("Urun ikram yapildi (Transfer mantigi). ID: " + sourceItemId);
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ikram islemi hatasi", e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}

