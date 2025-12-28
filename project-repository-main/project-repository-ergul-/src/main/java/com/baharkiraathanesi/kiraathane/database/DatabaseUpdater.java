package com.baharkiraathanesi.kiraathane.database;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUpdater {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUpdater.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Veritabani Guncelleme Baslatiliyor...");
        updateDatabase();
        LOGGER.info("Islem Tamamlandi.");
    }

    public static void updateDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Veritabani baglantisi kurulamadi");
                return;
            }

            // 1. Müşteri Tablosu
            createCustomersTable(conn);

            // 2. Kolonlar
            addColumnIfNotExists(conn, "stock_package", "INT DEFAULT 0");
            addColumnIfNotExists(conn, "portions_per_package", "INT DEFAULT 1");
            addColumnIfNotExists(conn, "stock_display", "VARCHAR(100) DEFAULT NULL");

            // 3. --- DÜZELTME: Kategorileri Türkçeleştir ---
            LOGGER.info("Kategoriler düzeltiliyor (Tr Karakter)...");
            fixCategoryNames(conn);

            // 4. Mevcut Ürünler
            updateExistingProducts(conn);

            // 5. Varsayılan Ürünler (Artık Türkçe Karakterli)
            addDefaultProducts(conn);

            LOGGER.info("VERITABANI BASARIYLA GUNCELLENDI");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabani guncelleme hatasi", e);
        }
    }

    // --- YENİ: Kategorileri Birleştiren Metot ---
    private static void fixCategoryNames(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // "Sicak Icecek" -> "Sıcak İçecek" yap
            stmt.executeUpdate("UPDATE products SET category = 'Sıcak İçecek' WHERE category = 'Sicak Icecek'");

            // "Icecek > Soguk Icecek" -> "İçecek > Soğuk İçecek" yap (Hiyerarşiler için)
            stmt.executeUpdate("UPDATE products SET category = REPLACE(category, 'Icecek >', 'İçecek >')");
            stmt.executeUpdate("UPDATE products SET category = REPLACE(category, 'Soguk Icecek', 'Soğuk İçecek')");
        }
    }

    private static void createCustomersTable(Connection conn) {
        final String SQL = "CREATE TABLE IF NOT EXISTS customers (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "phone VARCHAR(20), " +
                "balance DECIMAL(10, 2) DEFAULT 0.0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(SQL);
        } catch (SQLException e) {
            LOGGER.severe("Musteri tablosu hatasi: " + e.getMessage());
        }
    }

    private static void addColumnIfNotExists(Connection conn, String columnName, String columnDefinition) {
        final String ALTER_SQL = "ALTER TABLE products ADD COLUMN " + columnName + " " + columnDefinition;
        try (Statement stmt = conn.createStatement()) { stmt.execute(ALTER_SQL); }
        catch (SQLException ignored) {}
    }

    private static void updateExistingProducts(Connection conn) throws SQLException {
        final String UPDATE_SQL = "UPDATE products SET stock_package = FLOOR(stock_qty / GREATEST(portions_per_package, 1)) WHERE portions_per_package > 0";
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) { stmt.executeUpdate(); }
    }

    private static void addDefaultProducts(Connection conn) throws SQLException {
        // ARTIK TÜRKÇE KARAKTERLERLE EKLİYORUZ
        addProductIfNotExists(conn, "Çay", "Sıcak İçecek", 15.00, 1000, "bardak", 100, 5, 200);
        addProductIfNotExists(conn, "Portakallı Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
        addProductIfNotExists(conn, "Şeftali Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
        addProductIfNotExists(conn, "Kuşburnu Oralet", "Sıcak İçecek", 25.00, 200, "bardak", 50, 4, 50);
        addProductIfNotExists(conn, "Karadut Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
        addProductIfNotExists(conn, "Muzlu Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
        addProductIfNotExists(conn, "Türk Kahvesi", "Sıcak İçecek", 35.00, 80, "fincan", 20, 4, 20);
        addProductIfNotExists(conn, "Ihlamur", "Sıcak İçecek", 20.00, 100, "bardak", 50, 2, 50);
        addProductIfNotExists(conn, "Kaçak Çay", "Sıcak İçecek", 20.00, 600, "bardak", 100, 3, 200);
    }

    private static void addProductIfNotExists(Connection conn, String name, String category,
                                              double price, int stockQty, String unit,
                                              int criticalLevel, int stockPackage, int portionsPerPackage) throws SQLException {
        final String SELECT_SQL = "SELECT id FROM products WHERE name = ?";
        final String UPDATE_SQL = "UPDATE products SET category=?, price=?, unit=?, critical_level=?, stock_package=?, portions_per_package=?, stock_display=? WHERE id=?";
        final String INSERT_SQL = "INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";

        try (PreparedStatement selectStmt = conn.prepareStatement(SELECT_SQL)) {
            selectStmt.setString(1, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_SQL)) {
                        updateStmt.setString(1, category); // Kategoriyi güncelle (Türkçe karakter ile)
                        updateStmt.setDouble(2, price);
                        updateStmt.setString(3, unit);
                        updateStmt.setInt(4, criticalLevel);
                        updateStmt.setInt(5, stockPackage);
                        updateStmt.setInt(6, portionsPerPackage);
                        updateStmt.setString(7, stockDisplay);
                        updateStmt.setInt(8, id);
                        updateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_SQL)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, category);
                        insertStmt.setDouble(3, price);
                        insertStmt.setInt(4, stockQty);
                        insertStmt.setString(5, unit);
                        insertStmt.setInt(6, criticalLevel);
                        insertStmt.setInt(7, stockPackage);
                        insertStmt.setInt(8, portionsPerPackage);
                        insertStmt.setString(9, stockDisplay);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    private static void logCurrentProducts(Connection conn) {}
}