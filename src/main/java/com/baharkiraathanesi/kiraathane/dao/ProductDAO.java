package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Tüm ürünleri listeleme metodu
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Veritabanından gelen her satırı bir Java nesnesine çeviriyoruz
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock_qty"),
                        rs.getString("unit"),
                        rs.getInt("critical_level")
                );
                productList.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    // Yeni ürün ekleme
    public boolean addProduct(String name, String category, double price, int stockQty, String unit) {
        String sql = "INSERT INTO products (name, category, price, stock_qty, unit, critical_level) VALUES (?, ?, ?, ?, ?, 10)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setDouble(3, price);
            stmt.setInt(4, stockQty);
            stmt.setString(5, unit);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Ürün eklenirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Ürün silme
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Ürün silinirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Stok güncelleme
    public boolean updateProductStock(int productId, int newStockQty) {
        String sql = "UPDATE products SET stock_qty = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStockQty);
            stmt.setInt(2, productId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Stok güncellenirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}