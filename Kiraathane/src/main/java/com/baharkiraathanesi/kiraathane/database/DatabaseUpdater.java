package com.baharkiraathanesi.kiraathane.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUpdater {

    public static void main(String[] args) {
        updateDatabase();
    }

    public static void updateDatabase() {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            stmt = conn.createStatement();

            System.out.println("=== Veritabanı ve Ürünler Güncelleniyor ===");

            // 1. Tabloları Oluştur (SQL kodunuzdaki yapının aynısı)
            createTables(stmt);

            // 2. Yönetici Hesabını Ekle (SQL kodunuzdaki 'yonetici')
            addAdminUser(stmt);

            // 3. Ürünleri Ekle (SQL kodunuzdaki ürünler)
            // Çay (1 paket = 200 bardak, 5 paket = 1000 bardak)
            addProduct(stmt, "Çay", "Sıcak İçecek", 15.00, 1000, "bardak", 100, 5, 200);

            // Portakallı Oralet
            addProduct(stmt, "Portakallı Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);

            // Şeftali Oralet
            addProduct(stmt, "Şeftali Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);

            // Kuşburnu Oralet
            addProduct(stmt, "Kuşburnu Oralet", "Sıcak İçecek", 25.00, 200, "bardak", 50, 4, 50);

            // Karadut Oralet
            addProduct(stmt, "Karadut Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);

            // Muzlu Oralet
            addProduct(stmt, "Muzlu Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);

            // Türk Kahvesi
            addProduct(stmt, "Türk Kahvesi", "Sıcak İçecek", 35.00, 80, "fincan", 20, 4, 20);

            // Ihlamur
            addProduct(stmt, "Ihlamur", "Sıcak İçecek", 20.00, 100, "bardak", 50, 2, 50);

            // Kaçak Çay
            addProduct(stmt, "Kaçak Çay", "Sıcak İçecek", 20.00, 600, "bardak", 100, 3, 200);

            System.out.println("✅ Tüm işlemler başarıyla tamamlandı!");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (stmt != null) stmt.close(); if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        // Products Tablosu
        stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "category VARCHAR(255), " +
                "price DOUBLE DEFAULT 0, " +
                "stock_qty INT DEFAULT 0, " +
                "unit VARCHAR(50), " +
                "critical_level INT DEFAULT 0, " +
                "stock_package INT DEFAULT 0 COMMENT 'Paket sayısı', " +
                "portions_per_package INT DEFAULT 1 COMMENT 'Paket başına porsiyon', " +
                "stock_display VARCHAR(100) DEFAULT NULL COMMENT 'Stok gösterimi'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // Users Tablosu
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // Diğer tablolar (Tables, Orders, OrderItems) buraya eklenebilir...
        // (Önceki kodunuzda vardı, burası sade kalsın diye kısalttım)
    }

    private static void addAdminUser(Statement stmt) throws SQLException {
        // Kullanıcı var mı kontrol et, yoksa ekle (SQL'deki INSERT INTO mantığı)
        ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE username = 'yonetici'");
        if (!rs.next()) {
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('yonetici', '1234')");
            System.out.println("   👤 Yönetici kullanıcısı oluşturuldu (yonetici / 1234)");
        }
    }

    private static void addProduct(Statement stmt, String name, String category,
                                   double price, int stockQty, String unit,
                                   int criticalLevel, int stockPackage, int portionsPerPackage) throws SQLException {

        // Trigger yerine Java tarafında string oluşturma:
        // "5 paket (1000 bardak)" formatı
        String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";

        // Önce ürün var mı diye bak
        ResultSet rs = stmt.executeQuery("SELECT id FROM products WHERE name = '" + name + "'");

        if (rs.next()) {
            // Varsa GÜNCELLE
            int id = rs.getInt("id");
            String sql = String.format(
                    "UPDATE products SET category='%s', price=%.2f, stock_qty=%d, unit='%s', " +
                            "critical_level=%d, stock_package=%d, portions_per_package=%d, stock_display='%s' WHERE id=%d",
                    category, price, stockQty, unit, criticalLevel, stockPackage, portionsPerPackage, stockDisplay, id
            );
            stmt.executeUpdate(sql);
            System.out.println("   🔄 " + name + " güncellendi.");
        } else {
            // Yoksa EKLE
            String sql = String.format(
                    "INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display) " +
                            "VALUES ('%s', '%s', %.2f, %d, '%s', %d, %d, %d, '%s')",
                    name, category, price, stockQty, unit, criticalLevel, stockPackage, portionsPerPackage, stockDisplay
            );
            stmt.executeUpdate(sql);
            System.out.println("   ✅ " + name + " eklendi.");
        }
    }
}