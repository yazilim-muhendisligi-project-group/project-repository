package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        final String SQL = "SELECT * FROM products ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (conn == null) {
                LOGGER.warning("Veritabani baglantisi kurulamadi");
                return productList;
            }

            while (rs.next()) {
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
            }

            LOGGER.info(productList.size() + " urun getirildi");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Urunler getirilirken hata olustu", e);
        }

        return productList;
    }

    public boolean addProduct(String name, String category, double price, int stockPackage,
                              String unit, int portionsPerPackage) {
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warning("Urun adi bos olamaz");
            return false;
        }

        int stockQty = stockPackage * portionsPerPackage;
        String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";

        final String SQL = "INSERT INTO products (name, category, price, stock_qty, unit, critical_level, " +
                          "stock_package, portions_per_package, stock_display) VALUES (?, ?, ?, ?, ?, 10, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return false;
            }

            stmt.setString(1, name.trim());
            stmt.setString(2, category);
            stmt.setDouble(3, price);
            stmt.setInt(4, stockQty);
            stmt.setString(5, unit);
            stmt.setInt(6, stockPackage);
            stmt.setInt(7, portionsPerPackage);
            stmt.setString(8, stockDisplay);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Urun eklendi: " + name);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Urun eklenirken hata: " + name, e);
        }

        return false;
    }

    public boolean deleteProduct(int productId) {
        final String SQL = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return false;
            }

            stmt.setInt(1, productId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                LOGGER.info("Urun silindi: ID=" + productId);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Urun silinirken hata: ID=" + productId, e);
        }

        return false;
    }

    public boolean updateProductStock(int productId, int newStockPackage) {
        final String SELECT_SQL = "SELECT portions_per_package, name FROM products WHERE id = ?";
        final String UPDATE_SQL = "UPDATE products SET stock_package = ?, stock_qty = ?, " +
                                 "stock_display = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(SELECT_SQL)) {

            if (conn == null) {
                return false;
            }

            selectStmt.setInt(1, productId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int portionsPerPackage = rs.getInt("portions_per_package");
                    String name = rs.getString("name");
                    int newStockQty = newStockPackage * portionsPerPackage;
                    String stockDisplay = newStockPackage + " paket (" + newStockQty + " porsiyon)";

                    try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_SQL)) {
                        updateStmt.setInt(1, newStockPackage);
                        updateStmt.setInt(2, newStockQty);
                        updateStmt.setString(3, stockDisplay);
                        updateStmt.setInt(4, productId);

                        int affectedRows = updateStmt.executeUpdate();
                        if (affectedRows > 0) {
                            LOGGER.info("Stok guncellendi: " + name + " -> " + newStockPackage + " paket");
                            return true;
                        }
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Stok guncellenirken hata: ID=" + productId, e);
        }

        return false;
    }

    public boolean updateProductPrice(int productId, double newPrice) {
        final String SQL = "UPDATE products SET price = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return false;
            }

            stmt.setDouble(1, newPrice);
            stmt.setInt(2, productId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Fiyat guncellendi: ID=" + productId + " -> " + newPrice + " TL");
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fiyat guncellenirken hata: ID=" + productId, e);
        }

        return false;
    }

    public Product getProductById(int productId) {
        final String SQL = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return null;
            }

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
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
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Urun getirilirken hata: ID=" + productId, e);
        }

        return null;
    }
}

