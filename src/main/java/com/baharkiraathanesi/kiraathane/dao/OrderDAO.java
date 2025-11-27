package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.database.DatabaseUpdater;
import com.baharkiraathanesi.kiraathane.model.OrderItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // 1. Masada açık bir sipariş var mı? Varsa ID'sini getir, yoksa yeni oluştur.
    public int getOrCreateOrderId(int tableId) {
        int orderId = -1;

        // Önce açık sipariş var mı bakalım (is_paid = 0)
        String checkSql = "SELECT id FROM orders WHERE table_id = ? AND is_paid = FALSE";

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, tableId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Açık sipariş bulduk
                orderId = rs.getInt("id");
            } else {
                // Yokmuş, o zaman yeni sipariş açalım
                String createSql = "INSERT INTO orders (table_id, is_paid) VALUES (?, FALSE)";
                PreparedStatement createStmt = conn.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS);
                createStmt.setInt(1, tableId);
                createStmt.executeUpdate();

                // Yeni oluşan ID'yi al
                ResultSet genKeys = createStmt.getGeneratedKeys();
                if (genKeys.next()) {
                    orderId = genKeys.getInt(1);

                    // Masayı da "DOLU" yapalım
                    TableDAO tableDAO = new TableDAO();
                    tableDAO.updateTableStatus(tableId, true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderId;
    }

    // 2. Siparişe Ürün Ekleme (En Kritik Metot)
    public void addProductToOrder(int tableId, int productId, int quantity) {
        int orderId = getOrCreateOrderId(tableId); // Masa için sipariş fişini bul

        try (Connection conn = DatabaseConnection.getConnection()) {
            // A) Ürünün Fiyatını Bul
            String priceSql = "SELECT price FROM products WHERE id = ?";
            PreparedStatement priceStmt = conn.prepareStatement(priceSql);
            priceStmt.setInt(1, productId);
            ResultSet rs = priceStmt.executeQuery();

            double price = 0;
            if (rs.next()) price = rs.getDouble("price");

            // B) Sipariş Detayına Ekle
            String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price_at_order) VALUES (?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            itemStmt.setInt(1, orderId);
            itemStmt.setInt(2, productId);
            itemStmt.setInt(3, quantity);
            itemStmt.setDouble(4, price);
            itemStmt.executeUpdate();

            // C) Siparişin Toplam Tutarını Güncelle
            String totalSql = "UPDATE orders SET total_amount = total_amount + ? WHERE id = ?";
            PreparedStatement totalStmt = conn.prepareStatement(totalSql);
            totalStmt.setDouble(1, price * quantity);
            totalStmt.setInt(2, orderId);
            totalStmt.executeUpdate();

            // D) Stoğu Düş (Rapor Gereksinimi: Madde 186)
            String stockSql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ?";
            PreparedStatement stockStmt = conn.prepareStatement(stockSql);
            stockStmt.setInt(1, quantity);
            stockStmt.setInt(2, productId);
            stockStmt.executeUpdate();

            System.out.println("✅ Sipariş Eklendi: Masa " + tableId + " -> Ürün ID: " + productId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Hesabı Kapat (Ödeme Al)
    public void closeOrder(int tableId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Açık siparişi bul ve 'Ödendi' yap
            String sql = "UPDATE orders SET is_paid = TRUE WHERE table_id = ? AND is_paid = FALSE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tableId);
            stmt.executeUpdate();

            // Masayı BOŞ yap
            TableDAO tableDAO = new TableDAO();
            tableDAO.updateTableStatus(tableId, false);

            System.out.println("💰 Hesap Kapatıldı! Masa " + tableId + " artık boş.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4. Bugünkü Tamamlanmış Siparişleri Getir (Z Raporu için)
    public java.util.List<com.baharkiraathanesi.kiraathane.model.Order> getTodayCompletedOrders() {
        java.util.List<com.baharkiraathanesi.kiraathane.model.Order> orders = new java.util.ArrayList<>();

        String sql = "SELECT o.id, t.name as table_name, o.total_amount, o.created_at, " +
                     "'Nakit' as payment_type " +
                     "FROM orders o " +
                     "JOIN tables t ON o.table_id = t.id " +
                     "WHERE o.is_paid = TRUE AND DATE(o.created_at) = CURDATE() " +
                     "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                com.baharkiraathanesi.kiraathane.model.Order order = new com.baharkiraathanesi.kiraathane.model.Order();
                order.setId(rs.getInt("id"));
                order.setTableName(rs.getString("table_name"));
                order.setTotal(rs.getDouble("total_amount"));

                // Saat bilgisini formatla
                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    order.setOrderTime(timestamp.toLocalDateTime().toLocalTime().toString());
                }

                order.setPaymentType(rs.getString("payment_type"));
                orders.add(order);
            }

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            System.err.println("❌ Bugünkü siparişler alınırken hata: " + msg);
            // Eğer orders tablosu yoksa, veritabanı güncellemesini dene ve bir kez tekrar sorgula
            if (msg.contains("doesn't exist") || msg.toLowerCase().contains("does not exist") || msg.toLowerCase().contains("unknown table")) {
                System.err.println("❌ 'orders' tablosu bulunamadı - veritabanı güncellemesi çalıştırılıyor...");
                try {
                    DatabaseUpdater.updateDatabase();
                } catch (Exception ex) {
                    System.err.println("❌ Veritabanı güncellemesi başarısız: " + ex.getMessage());
                }

                // Tekrar dene
                try (Connection conn2 = DatabaseConnection.getConnection();
                     PreparedStatement stmt2 = conn2.prepareStatement(sql);
                     ResultSet rs2 = stmt2.executeQuery()) {

                    while (rs2.next()) {
                        com.baharkiraathanesi.kiraathane.model.Order order = new com.baharkiraathanesi.kiraathane.model.Order();
                        order.setId(rs2.getInt("id"));
                        order.setTableName(rs2.getString("table_name"));
                        order.setTotal(rs2.getDouble("total_amount"));

                        Timestamp timestamp = rs2.getTimestamp("created_at");
                        if (timestamp != null) {
                            order.setOrderTime(timestamp.toLocalDateTime().toLocalTime().toString());
                        }
                        order.setPaymentType(rs2.getString("payment_type"));
                        orders.add(order);
                    }

                } catch (SQLException ex2) {
                    System.err.println("❌ Yeniden deneme sırasında hata: " + ex2.getMessage());
                    ex2.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }

        return orders;
    }

    // Yeni metod: Açık (hesabı alınmamış) sipariş/masa var mı?
    public boolean hasOpenTables() {
        String sql = "SELECT COUNT(*) FROM orders WHERE is_paid = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Yeni metod: Veritabanından bir masanın (açık) sipariş kalemlerini getir
    public List<OrderItem> getOrderItemsForTable(int tableId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.id as oi_id, p.name as product_name, oi.quantity, oi.price_at_order " +
                     "FROM order_items oi " +
                     "JOIN orders o ON oi.order_id = o.id " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE o.table_id = ? AND o.is_paid = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("oi_id");
                    String name = rs.getString("product_name");
                    int qty = rs.getInt("quantity");
                    double price = rs.getDouble("price_at_order");
                    items.add(new OrderItem(id, name, qty, price));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Yeni metod: Sipariş kalemini sil (UI'dan silme) -> toplamı ve stoğu güncelle
    public boolean deleteOrderItem(int orderItemId) {
        String selectSql = "SELECT order_id, product_id, quantity, price_at_order FROM order_items WHERE id = ?";
        String deleteSql = "DELETE FROM order_items WHERE id = ?";
        String updateOrderSql = "UPDATE orders SET total_amount = total_amount - ? WHERE id = ?";
        String updateStockSql = "UPDATE products SET stock_qty = stock_qty + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, orderItemId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false; // Kalem bulunamadı
                    }

                    int orderId = rs.getInt("order_id");
                    int productId = rs.getInt("product_id");
                    int qty = rs.getInt("quantity");
                    double price = rs.getDouble("price_at_order");

                    // 1) Sipariş toplamını düş
                    try (PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderSql)) {
                        updateOrderStmt.setDouble(1, price * qty);
                        updateOrderStmt.setInt(2, orderId);
                        updateOrderStmt.executeUpdate();
                    }

                    // 2) Stoğu geri ekle
                    try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql)) {
                        updateStockStmt.setInt(1, qty);
                        updateStockStmt.setInt(2, productId);
                        updateStockStmt.executeUpdate();
                    }

                    // 3) Kalemi sil
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, orderItemId);
                        deleteStmt.executeUpdate();
                    }

                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}