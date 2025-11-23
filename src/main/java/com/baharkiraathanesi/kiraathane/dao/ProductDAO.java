package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Tüm ürünleri listeleme metodu - YENİ: Paket/Porsiyon bilgileriyle
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Yeni kolonlar var mı kontrol et
                try {
                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getInt("stock_qty"),
                            rs.getString("unit"),
                            rs.getInt("critical_level"),
                            rs.getInt("stock_package"),
                            rs.getInt("portions_per_package"),
                            rs.getString("stock_display")
                    );
                    productList.add(product);
                } catch (SQLException e) {
                    // Eski veritabanı yapısı (yeni kolonlar yoksa)
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    // Yeni ürün ekleme - Paket/Porsiyon sistemiyle
    public boolean addProduct(String name, String category, double price, int stockPackage,
                            String unit, int portionsPerPackage) {
        int stockQty = stockPackage * portionsPerPackage;
        String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";

        String sql = "INSERT INTO products (name, category, price, stock_qty, unit, critical_level, " +
                    "stock_package, portions_per_package, stock_display) VALUES (?, ?, ?, ?, ?, 10, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setDouble(3, price);
            stmt.setInt(4, stockQty);
            stmt.setString(5, unit);
            stmt.setInt(6, stockPackage);
            stmt.setInt(7, portionsPerPackage);
            stmt.setString(8, stockDisplay);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Ürün eklenirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ESKİ METOT - Geriye dönük uyumluluk için
    public boolean addProduct(String name, String category, double price, int stockQty, String unit) {
        return addProduct(name, category, price, 1, unit, stockQty);
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

    // Stok güncelleme - PAKET bazlı
    public boolean updateProductStock(int productId, int newStockPackage) {
        // Önce ürünün portions_per_package değerini al
        String selectSql = "SELECT portions_per_package FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setInt(1, productId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int portionsPerPackage = rs.getInt("portions_per_package");
                int newStockQty = newStockPackage * portionsPerPackage;

                // Stok güncelleme
                String updateSql = "UPDATE products SET stock_package = ?, stock_qty = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newStockPackage);
                    updateStmt.setInt(2, newStockQty);
                    updateStmt.setInt(3, productId);

                    int affectedRows = updateStmt.executeUpdate();
                    return affectedRows > 0;
                }
            }
            return false;

        } catch (SQLException e) {
            System.err.println("❌ Stok güncellenirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}