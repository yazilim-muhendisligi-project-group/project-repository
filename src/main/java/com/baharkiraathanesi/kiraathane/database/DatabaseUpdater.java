package com.baharkiraathanesi.kiraathane.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Veritabanını güncellemek için utility sınıfı
 * Bu sınıfı bir kez çalıştırın, sonra silebilirsiniz
 */
public class DatabaseUpdater {

    public static void main(String[] args) {
        System.out.println("=== Veritabanı Güncelleme Başlatılıyor ===");

        updateDatabase();

        System.out.println("=== İşlem Tamamlandı ===");
    }

    public static void updateDatabase() {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            if (conn == null) {
                System.err.println("❌ Veritabanı bağlantısı kurulamadı!");
                return;
            }

            stmt = conn.createStatement();

            System.out.println("📋 1/3 - Yeni kolonlar ekleniyor...");

            // Eğer products tablosu yoksa önce oluştur
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "category VARCHAR(255), " +
                        "price DOUBLE DEFAULT 0, " +
                        "stock_qty INT DEFAULT 0, " +
                        "unit VARCHAR(50), " +
                        "critical_level INT DEFAULT 0" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                System.out.println("   ✅ products tablosu oluşturuldu veya zaten var");
            } catch (SQLException e) {
                System.out.println("   ⚠️ products tablosu oluşturulurken hata: " + e.getMessage());
                e.printStackTrace();
            }

            // Yeni kolonları ekle (hata verirse devam et)
            try {
                stmt.execute("ALTER TABLE products ADD COLUMN stock_package INT DEFAULT 0 COMMENT 'Paket sayısı'");
                System.out.println("   ✅ stock_package kolonu eklendi");
            } catch (SQLException e) {
                System.out.println("   ℹ️  stock_package zaten var");
            }

            try {
                stmt.execute("ALTER TABLE products ADD COLUMN portions_per_package INT DEFAULT 1 COMMENT 'Paket başına porsiyon'");
                System.out.println("   ✅ portions_per_package kolonu eklendi");
            } catch (SQLException e) {
                System.out.println("   ℹ️  portions_per_package zaten var");
                e.printStackTrace();
            }

            try {
                stmt.execute("ALTER TABLE products ADD COLUMN stock_display VARCHAR(100) DEFAULT NULL COMMENT 'Stok gösterimi'");
                System.out.println("   ✅ stock_display kolonu eklendi");
            } catch (SQLException e) {
                System.out.println("   ℹ️  stock_display zaten var");
                e.printStackTrace();
            }

            // Diğer gerekli tabloları oluştur (orders, order_items, tables, users)
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS tables (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "is_occupied BOOLEAN DEFAULT FALSE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                System.out.println("   ✅ tables tablosu oluşturuldu veya zaten var");
            } catch (SQLException e) {
                System.out.println("   ⚠️ tables tablosu oluşturulurken hata: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "table_id INT, " +
                        "is_paid BOOLEAN DEFAULT FALSE, " +
                        "total_amount DOUBLE DEFAULT 0, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                System.out.println("   ✅ orders tablosu oluşturuldu veya zaten var");
            } catch (SQLException e) {
                System.out.println("   ⚠️ orders tablosu oluşturulurken hata: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "order_id INT, " +
                        "product_id INT, " +
                        "quantity INT DEFAULT 0, " +
                        "price_at_order DOUBLE DEFAULT 0" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                System.out.println("   ✅ order_items tablosu oluşturuldu veya zaten var");
            } catch (SQLException e) {
                System.out.println("   ⚠️ order_items tablosu oluşturulurken hata: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(100) NOT NULL UNIQUE, " +
                        "password VARCHAR(255) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                System.out.println("   ✅ users tablosu oluşturuldu veya zaten var");
            } catch (SQLException e) {
                System.out.println("   ⚠️ users tablosu oluşturulurken hata: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("\n📋 2/3 - Mevcut ürünler güncelleniyor/temizleniyor...");

            // Önce mevcut ürünleri güncelle (eğer varsa)
            stmt.executeUpdate("UPDATE products SET stock_package = FLOOR(stock_qty / GREATEST(portions_per_package, 1)) WHERE portions_per_package > 0");

            // Eski ürünleri sil (foreign key sorunu yoksa)
            try {
                int deletedRows = stmt.executeUpdate("DELETE FROM products");
                System.out.println("   ✅ " + deletedRows + " eski ürün silindi");
            } catch (SQLException e) {
                System.out.println("   ⚠️  Mevcut ürünler silinemedi (sipariş bağlantısı var), güncelleme yapılacak...");
                // Ürünleri sil yerine güncelle
                stmt.executeUpdate("DELETE FROM products WHERE id NOT IN (SELECT DISTINCT product_id FROM order_items)");
                System.out.println("   ✅ Kullanılmayan ürünler silindi");
            }

            System.out.println("\n📋 3/3 - Yeni ürünler ekleniyor...");

            // Her ürün için önce var mı kontrol et, yoksa ekle
            addProductIfNotExists(stmt, "Çay", "Sıcak İçecek", 15.00, 1000, "bardak", 100, 5, 200);
            addProductIfNotExists(stmt, "Portakallı Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
            addProductIfNotExists(stmt, "Şeftali Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
            addProductIfNotExists(stmt, "Kuşburnu Oralet", "Sıcak İçecek", 25.00, 200, "bardak", 50, 4, 50);
            addProductIfNotExists(stmt, "Karadut Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
            addProductIfNotExists(stmt, "Muzlu Oralet", "Sıcak İçecek", 25.00, 150, "bardak", 50, 3, 50);
            addProductIfNotExists(stmt, "Türk Kahvesi", "Sıcak İçecek", 35.00, 80, "fincan", 20, 4, 20);
            addProductIfNotExists(stmt, "Ihlamur", "Sıcak İçecek", 20.00, 100, "bardak", 50, 2, 50);
            addProductIfNotExists(stmt, "Kaçak Çay", "Sıcak İçecek", 20.00, 600, "bardak", 100, 3, 200);

            // Eğer tables tablosunda hiç kayıt yoksa, birkaç varsayılan masa ekle
            try {
                var rsTables = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM tables");
                if (rsTables.next() && rsTables.getInt("cnt") == 0) {
                    stmt.executeUpdate("INSERT INTO tables (name, is_occupied) VALUES ('Masa 1', FALSE)");
                    stmt.executeUpdate("INSERT INTO tables (name, is_occupied) VALUES ('Masa 2', FALSE)");
                    stmt.executeUpdate("INSERT INTO tables (name, is_occupied) VALUES ('Masa 3', FALSE)");
                    stmt.executeUpdate("INSERT INTO tables (name, is_occupied) VALUES ('Masa 4', FALSE)");
                    System.out.println("   ✅ Varsayılan masalar eklendi");
                }
            } catch (SQLException e) {
                System.out.println("   ⚠️ Varsayılan masa ekleme sırasında hata: " + e.getMessage());
            }

            System.out.println("\n📋 Kontrol ediliyor...");

            // Eklenen ürünleri listele
            var rs = stmt.executeQuery("SELECT name, stock_display, price FROM products ORDER BY name");
            System.out.println("\n🎉 Mevcut Ürünler:");
            System.out.println("─────────────────────────────────────────────");
            int count = 0;
            while (rs.next()) {
                System.out.printf("   • %-20s : %-25s : %.2f TL%n",
                    rs.getString("name"),
                    rs.getString("stock_display"),
                    rs.getDouble("price")
                );
                count++;
            }
            System.out.println("─────────────────────────────────────────────");

            System.out.println("\n✅ VERİTABANI BAŞARIYLA GÜNCELLENDİ!");
            System.out.println("✅ " + count + " ürün mevcut");
            System.out.println("✅ Paket/Porsiyon sistemi aktif");
            System.out.println("\n💡 Artık uygulamayı çalıştırabilirsiniz!");

        } catch (SQLException e) {
            System.err.println("❌ Hata oluştu: " + e.getMessage());
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
        // Önce ürün var mı kontrol et
        var rs = stmt.executeQuery("SELECT id FROM products WHERE name = '" + name.replace("'", "''") + "'");

        if (rs.next()) {
            // Ürün var, güncelle
            int id = rs.getInt("id");
            String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";
            stmt.executeUpdate(String.format(
                "UPDATE products SET category='%s', price=%.2f, stock_qty=%d, unit='%s', " +
                "critical_level=%d, stock_package=%d, portions_per_package=%d, stock_display='%s' WHERE id=%d",
                category, price, stockQty, unit, criticalLevel, stockPackage, portionsPerPackage, stockDisplay, id
            ));
            System.out.println("   🔄 " + name + " güncellendi");
        } else {
            // Ürün yok, ekle
            String stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";
            stmt.executeUpdate(String.format(
                "INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display) " +
                "VALUES ('%s', '%s', %.2f, %d, '%s', %d, %d, %d, '%s')",
                name, category, price, stockQty, unit, criticalLevel, stockPackage, portionsPerPackage, stockDisplay
            ));
            System.out.println("   ✅ " + name + " eklendi");
        }
    }
}
