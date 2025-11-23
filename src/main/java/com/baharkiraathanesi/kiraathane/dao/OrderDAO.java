package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import java.sql.*;

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
            System.err.println("❌ Bugünkü siparişler alınırken hata: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }
}