package com.baharkiraathanesi.kiraathane.database;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUpdater {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUpdater.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Veritabani Guncelleme Baslatiliyor");
        updateDatabase();
        LOGGER.info("Islem Tamamlandi");
    }

    public static void updateDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Veritabani baglantisi kurulamadi");
                return;
            }

            LOGGER.info("1/3 - Yeni kolonlar ekleniyor...");
            addColumnIfNotExists(conn, "stock_package", "INT DEFAULT 0");
            addColumnIfNotExists(conn, "portions_per_package", "INT DEFAULT 1");
            addColumnIfNotExists(conn, "stock_display", "VARCHAR(100) DEFAULT NULL");

            LOGGER.info("2/3 - Mevcut urunler guncelleniyor/temizleniyor...");
            updateExistingProducts(conn);

            LOGGER.info("3/3 - Yeni urunler ekleniyor...");
            addDefaultProducts(conn);

            logCurrentProducts(conn);

            LOGGER.info("VERITABANI BASARIYLA GUNCELLENDI");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabani guncelleme hatasi", e);
        }
    }

    private static void addColumnIfNotExists(Connection conn, String columnName, String columnDefinition) {
        final String ALTER_SQL = "ALTER TABLE products ADD COLUMN " + columnName + " " + columnDefinition;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(ALTER_SQL);
            LOGGER.info("   " + columnName + " kolonu eklendi");
        } catch (SQLException e) {
            LOGGER.fine("   " + columnName + " zaten var");
        }
    }

    private static void updateExistingProducts(Connection conn) throws SQLException {
        final String UPDATE_SQL = "UPDATE products SET stock_package = FLOOR(stock_qty / GREATEST(portions_per_package, 1)) WHERE portions_per_package > 0";

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.executeUpdate();
        }

        try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM products")) {
            int deletedRows = deleteStmt.executeUpdate();
            LOGGER.info("   " + deletedRows + " eski urun silindi");
        } catch (SQLException e) {
            LOGGER.info("   Mevcut urunler silinemedi (siparis baglantisi var), guncelleme yapilacak...");
            try (PreparedStatement altDeleteStmt = conn.prepareStatement(
                    "DELETE FROM products WHERE id NOT IN (SELECT DISTINCT product_id FROM order_items)")) {
                altDeleteStmt.executeUpdate();
                LOGGER.info("   Kullanilmayan urunler silindi");
            }
        }
    }

    private static void addDefaultProducts(Connection conn) throws SQLException {
        addProductIfNotExists(conn, "Cay", "Sicak Icecek", 15.00, 1000, "bardak", 100, 5, 200);
        addProductIfNotExists(conn, "Portakalli Oralet", "Sicak Icecek", 25.00, 150, "bardak", 50, 3, 50);
        addProductIfNotExists(conn, "Seftali Oralet", "Sicak Icecek", 25.00, 150, "bardak", 50, 3, 50);
        addProductIfNotExists(conn, "Kusburnu Oralet", "Sicak Icecek", 25.00, 200, "bardak", 50, 4, 50);
        addProductIfNotExists(conn, "Karadut Oralet", "Sicak Icecek", 25.00, 150, "bardak", 50, 3, 50);
        addProductIfNotExists(conn, "Muzlu Oralet", "Sicak Icecek", 25.00, 150, "bardak", 50, 3, 50);
        addProductIfNotExists(conn, "Turk Kahvesi", "Sicak Icecek", 35.00, 80, "fincan", 20, 4, 20);
        addProductIfNotExists(conn, "Ihlamur", "Sicak Icecek", 20.00, 100, "bardak", 50, 2, 50);
        addProductIfNotExists(conn, "Kacak Cay", "Sicak Icecek", 20.00, 600, "bardak", 100, 3, 200);
    }

    private static void addProductIfNotExists(Connection conn, String name, String category,
                                             double price, int stockQty, String unit,
                                             int criticalLevel, int stockPackage, int portionsPerPackage) throws SQLException {

        final String SELECT_SQL = "SELECT id FROM products WHERE name = ?";
        final String UPDATE_SQL = "UPDATE products SET category=?, price=?, stock_qty=?, unit=?, " +
                "critical_level=?, stock_package=?, portions_per_package=?, stock_display=? WHERE id=?";
        final String INSERT_SQL = "INSERT INTO products (name, category, price, stock_qty, unit, critical_level, " +
                "stock_package, portions_per_package, stock_display) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";

        try (PreparedStatement selectStmt = conn.prepareStatement(SELECT_SQL)) {
            selectStmt.setString(1, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_SQL)) {
                        updateStmt.setString(1, category);
                        updateStmt.setDouble(2, price);
                        updateStmt.setInt(3, stockQty);
                        updateStmt.setString(4, unit);
                        updateStmt.setInt(5, criticalLevel);
                        updateStmt.setInt(6, stockPackage);
                        updateStmt.setInt(7, portionsPerPackage);
                        updateStmt.setString(8, stockDisplay);
                        updateStmt.setInt(9, id);
                        updateStmt.executeUpdate();
                    }
                    LOGGER.info("   " + name + " guncellendi");
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
                    LOGGER.info("   " + name + " eklendi");
                }
            }
        }
    }

    private static void logCurrentProducts(Connection conn) throws SQLException {
        final String SQL = "SELECT name, stock_display, price FROM products ORDER BY name";

        try (PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            LOGGER.info("Mevcut Urunler:");
            int count = 0;
            while (rs.next()) {
                LOGGER.info(String.format("   %-20s : %-25s : %.2f TL",
                    rs.getString("name"),
                    rs.getString("stock_display"),
                    rs.getDouble("price")
                ));
                count++;
            }
            LOGGER.info(count + " urun mevcut");
        }
    }
}

