package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Order;
import com.baharkiraathanesi.kiraathane.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public int getOrCreateOrderId(int tableId) {
        int orderId = -1;
        String checkSql = "SELECT id FROM orders WHERE table_id = ? AND is_paid = FALSE";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.out.println("OrderDAO: Veritabanı bağlantısı kurulamadı!");
                return orderId;
            }

            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, tableId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                orderId = rs.getInt("id");
            } else {
                String createSql = "INSERT INTO orders (table_id, is_paid) VALUES (?, FALSE)";
                PreparedStatement createStmt = conn.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS);
                createStmt.setInt(1, tableId);
                createStmt.executeUpdate();

                ResultSet genKeys = createStmt.getGeneratedKeys();
                if (genKeys.next()) {
                    orderId = genKeys.getInt(1);

                    TableDAO tableDAO = new TableDAO();
                    tableDAO.updateTableStatus(tableId, true);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return orderId;
    }

    public boolean addProductToOrder(int tableId, int productId, int quantity) {
        int orderId = getOrCreateOrderId(tableId);
        boolean isSuccess = false;

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Stok Kontrolü
            String stockCheckSql = "SELECT stock_qty, name FROM products WHERE id = ?";
            PreparedStatement stockCheckStmt = conn.prepareStatement(stockCheckSql);
            stockCheckStmt.setInt(1, productId);
            ResultSet stockRs = stockCheckStmt.executeQuery();

            if (stockRs.next()) {
                int currentStock = stockRs.getInt("stock_qty");
                if (currentStock < quantity) {
                    return false;
                }
            }

            String priceSql = "SELECT price FROM products WHERE id = ?";
            PreparedStatement priceStmt = conn.prepareStatement(priceSql);
            priceStmt.setInt(1, productId);
            ResultSet rs = priceStmt.executeQuery();

            double price = 0;
            if (rs.next()) price = rs.getDouble("price");

            String checkItemSql = "SELECT id FROM order_items WHERE order_id = ? AND product_id = ?";
            PreparedStatement checkItemStmt = conn.prepareStatement(checkItemSql);
            checkItemStmt.setInt(1, orderId);
            checkItemStmt.setInt(2, productId);
            ResultSet itemRs = checkItemStmt.executeQuery();

            if (itemRs.next()) {
                int existingItemId = itemRs.getInt("id");
                String updateItemSql = "UPDATE order_items SET quantity = quantity + ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateItemSql);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, existingItemId);
                updateStmt.executeUpdate();
            } else {
                String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price_at_order) VALUES (?, ?, ?, ?)";
                PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, productId);
                itemStmt.setInt(3, quantity);
                itemStmt.setDouble(4, price);
                itemStmt.executeUpdate();
            }

            String totalSql = "UPDATE orders SET total_amount = total_amount + ? WHERE id = ?";
            PreparedStatement totalStmt = conn.prepareStatement(totalSql);
            totalStmt.setDouble(1, price * quantity);
            totalStmt.setInt(2, orderId);
            totalStmt.executeUpdate();

            String stockSql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ?";
            PreparedStatement stockStmt = conn.prepareStatement(stockSql);
            stockStmt.setInt(1, quantity);
            stockStmt.setInt(2, productId);
            stockStmt.executeUpdate();

            isSuccess = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isSuccess;
    }

    public void closeOrder(int tableId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE orders SET is_paid = TRUE WHERE table_id = ? AND is_paid = FALSE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tableId);
            stmt.executeUpdate();

            TableDAO tableDAO = new TableDAO();
            tableDAO.updateTableStatus(tableId, false);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Order> getTodayCompletedOrders() {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT o.id, t.name as table_name, o.total_amount, o.created_at " +
                "FROM orders o " +
                "JOIN tables t ON o.table_id = t.id " +
                "WHERE o.is_paid = TRUE AND DATE(o.created_at) = CURDATE() " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
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
            System.out.println("Bugünkü siparişler alınırken hata: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public boolean hasOpenOrders() {
        String sql = "SELECT COUNT(*) as open_count FROM orders WHERE is_paid = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int openCount = rs.getInt("open_count");
                return openCount > 0;
            }

        } catch (SQLException e) {
            System.out.println("Açık siparişler kontrol edilirken hata: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public List<OrderItem> getOrderItems(int tableId) {
        List<OrderItem> items = new ArrayList<>();

        String sql = "SELECT oi.id, oi.product_id, p.name as product_name, oi.quantity, " +
                "oi.price_at_order, (oi.quantity * oi.price_at_order) as subtotal " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE o.table_id = ? AND o.is_paid = FALSE " +
                "ORDER BY oi.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableId);
            ResultSet rs = stmt.executeQuery();

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

        } catch (SQLException e) {
            System.out.println("Sipariş detayları alınırken hata: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    public void removeOrderItem(int orderItemId, int productId, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String infoSql = "SELECT order_id, price_at_order FROM order_items WHERE id = ?";
            PreparedStatement infoStmt = conn.prepareStatement(infoSql);
            infoStmt.setInt(1, orderItemId);
            ResultSet rs = infoStmt.executeQuery();

            int orderId = -1;
            double priceAtOrder = 0;
            if (rs.next()) {
                orderId = rs.getInt("order_id");
                priceAtOrder = rs.getDouble("price_at_order");
            }

            String deleteSql = "DELETE FROM order_items WHERE id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, orderItemId);
            deleteStmt.executeUpdate();

            if (orderId > 0) {
                String totalSql = "UPDATE orders SET total_amount = total_amount - ? WHERE id = ?";
                PreparedStatement totalStmt = conn.prepareStatement(totalSql);
                totalStmt.setDouble(1, priceAtOrder * quantity);
                totalStmt.setInt(2, orderId);
                totalStmt.executeUpdate();
            }

            String stockSql = "UPDATE products SET stock_qty = stock_qty + ? WHERE id = ?";
            PreparedStatement stockStmt = conn.prepareStatement(stockSql);
            stockStmt.setInt(1, quantity);
            stockStmt.setInt(2, productId);
            stockStmt.executeUpdate();

            if (orderId > 0) {
                String countSql = "SELECT COUNT(*) FROM order_items WHERE order_id = ?";
                PreparedStatement countStmt = conn.prepareStatement(countSql);
                countStmt.setInt(1, orderId);
                ResultSet countRs = countStmt.executeQuery();

                if (countRs.next() && countRs.getInt(1) == 0) {

                    String tableSql = "SELECT table_id FROM orders WHERE id = ?";
                    PreparedStatement tableStmt = conn.prepareStatement(tableSql);
                    tableStmt.setInt(1, orderId);
                    ResultSet tableRs = tableStmt.executeQuery();

                    int tableId = -1;
                    if (tableRs.next()) {
                        tableId = tableRs.getInt("table_id");
                    }

                    String deleteOrderSql = "DELETE FROM orders WHERE id = ?";
                    PreparedStatement deleteOrderStmt = conn.prepareStatement(deleteOrderSql);
                    deleteOrderStmt.setInt(1, orderId);
                    deleteOrderStmt.executeUpdate();

                    if (tableId != -1) {
                        String updateTableSql = "UPDATE tables SET is_occupied = FALSE WHERE id = ?";
                        PreparedStatement updateTableStmt = conn.prepareStatement(updateTableSql);
                        updateTableStmt.setInt(1, tableId);
                        updateTableStmt.executeUpdate();
                        System.out.println("Sipariş boşaldığı için Masa " + tableId + " boşa çıkarıldı.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Sipariş kalemi silinirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
}