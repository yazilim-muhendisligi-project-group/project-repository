package com.baharkiraathanesi.kiraathane.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseUpdater {

    public static void main(String[] args) {
        System.out.println("Veritabanı Güncelleme Başlatılıyor");
        updateDatabase();
        System.out.println("İşlem Tamamlandı");
    }

    public static void updateDatabase() {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            if (conn == null) {
                System.out.println("Veritabanı bağlantısı kurulamadı!");
                return;
            }

            stmt = conn.createStatement();

            System.out.println("1/3 - Yeni kolonlar ekleniyor...");

            try {
                stmt.execute("ALTER TABLE products ADD COLUMN stock_package INT DEFAULT 0 COMMENT 'Paket sayısı'");
                System.out.println("   stock_package kolonu eklendi");
            } catch (SQLException e) {
                System.out.println("   stock_package zaten var");
            }

            try {
                stmt.execute("ALTER TABLE products ADD COLUMN portions_per_package INT DEFAULT 1 COMMENT 'Paket başına porsiyon'");
                System.out.println("   portions_per_package kolonu eklendi");
            } catch (SQLException e) {
                System.out.println("   portions_per_package zaten var");
            }

            try {
                stmt.execute("ALTER TABLE products ADD COLUMN stock_display VARCHAR(100) DEFAULT NULL COMMENT 'Stok gösterimi'");
                System.out.println("   stock_display kolonu eklendi");
            } catch (SQLException e) {
                System.out.println("   stock_display zaten var");
            }

            System.out.println("\n2/3 - Mevcut ürünler güncelleniyor/temizleniyor...");

            stmt.executeUpdate("UPDATE products SET stock_package = FLOOR(stock_qty / GREATEST(portions_per_package, 1)) WHERE portions_per_package > 0");

            try {
                int deletedRows = stmt.executeUpdate("DELETE FROM products");
                System.out.println("   " + deletedRows + " eski ürün silindi");
            } catch (SQLException e) {
                System.out.println("   Mevcut ürünler silinemedi (sipariş bağlantısı var), güncelleme yapılacak...");
                stmt.executeUpdate("DELETE FROM products WHERE id NOT IN (SELECT DISTINCT product_id FROM order_items)");
                System.out.println("   Kullanılmayan ürünler silindi");
            }

            System.out.println("\n3/3 - Yeni ürünler ekleniyor...");

            addProductIfNotExists(stmt, "Çay", "Sıcak İçecek", 15.00, 1000, "bardak", 100, 5, 200);
            addProductIfNotExists(stmt, "Portakallı Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
            addProductIfNotExists(stmt, "Şeftali Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
            addProductIfNotExists(stmt, "Kuşburnu Oralet", "Sıcak İçecek", 25.00, 200, "bardak", 50, 4, 50);
            addProductIfNotExists(stmt, "Karadut Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
            addProductIfNotExists(stmt, "Muzlu Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
            addProductIfNotExists(stmt, "Türk Kahvesi", "Sıcak İçecek", 35.00, 80, "fincan", 20, 4, 20);
            addProductIfNotExists(stmt, "Ihlamur", "Sıcak İçecek", 20.00, 100, "bardak", 50, 2, 50);
            addProductIfNotExists(stmt, "Kaçak Çay", "Sıcak İçecek", 20.00, 600, "bardak", 100, 3, 200);

            System.out.println("\nKontrol ediliyor...");

            var rs = stmt.executeQuery("SELECT name, stock_display, price FROM products ORDER BY name");
            System.out.println("\nMevcut Ürünler:");
            System.out.println("─────────────────────────────────────────────");
            int count = 0;
            while (rs.next()) {
                System.out.printf("   %-20s : %-25s : %.2f TL%n",
                    rs.getString("name"),
                    rs.getString("stock_display"),
                    rs.getDouble("price")
                );
                count++;
            }
            System.out.println("─────────────────────────────────────────────");

            System.out.println("\nVERİTABANI BAŞARIYLA GÜNCELLENDİ!");
            System.out.println(count + " ürün mevcut");
            System.out.println("Paket/Porsiyon sistemi aktif");
            System.out.println("\nArtık uygulamayı çalıştırabilirsiniz!");

        } catch (SQLException e) {
            System.out.println("Hata oluştu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addProductIfNotExists(Statement stmt, String name, String category,
                                             double price, int stockQty, String unit,
                                             int criticalLevel, int stockPackage, int portionsPerPackage) throws SQLException {
        var rs = stmt.executeQuery("SELECT id FROM products WHERE name = '" + name.replace("'", "''") + "'");

        if (rs.next()) {
            int id = rs.getInt("id");
            String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";
            stmt.executeUpdate(String.format(
                "UPDATE products SET category='%s', price=%.2f, stock_qty=%d, unit='%s', " +
                "critical_level=%d, stock_package=%d, portions_per_package=%d, stock_display='%s' WHERE id=%d",
                category, price, stockQty, unit, criticalLevel, stockPackage, portionsPerPackage, stockDisplay, id
            ));
            System.out.println("   " + name + " güncellendi");
        } else {
            String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";
            stmt.executeUpdate(String.format(
                "INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display) " +
                "VALUES ('%s', '%s', %.2f, %d, '%s', %d, %d, %d, '%s')",
                name, category, price, stockQty, unit, criticalLevel, stockPackage, portionsPerPackage, stockDisplay
            ));
            System.out.println("   " + name + " eklendi");
        }
    }
}
