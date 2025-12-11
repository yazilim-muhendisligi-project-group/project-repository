-- Bahar Kıraathanesi Veritabanı Kurulum Scripti (Final)
-- Tüm tabloları oluşturur ve başlangıç verilerini sadece bir kere ekler.

-- Veritabanını oluştur
CREATE DATABASE IF NOT EXISTS bahar_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bahar_db;

-- 0. Kurulum kontrol tablosu (setup)
CREATE TABLE IF NOT EXISTS setup (
                                     id INT PRIMARY KEY,
                                     is_done BOOLEAN DEFAULT FALSE
);

-- İlk kayıt yoksa oluştur (tekrar çalışırsa hata vermez)
INSERT IGNORE INTO setup (id, is_done) VALUES (1, FALSE);

-- Eğer kurulum daha önce yapıldıysa script burada durdurulur (uygulama tarafı kontrol eder)
SELECT IF(is_done = TRUE, 'Kurulum zaten yapılmış, işlem sonlandırıldı.', 'Kurulum başlatılıyor...')
           AS setup_status
FROM setup WHERE id = 1;


-- 1. USERS Tablosu
CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     role VARCHAR(20) DEFAULT 'user',
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Varsayılan kullanıcılar
INSERT INTO users (username, password, role)
VALUES ('yonetici', '1234', 'admin')
ON DUPLICATE KEY UPDATE password='1234', role='admin';

INSERT INTO users (username, password, role)
VALUES ('admin', '1234', 'admin')
ON DUPLICATE KEY UPDATE password='1234', role='admin';


-- 2. PRODUCTS Tablosu
CREATE TABLE IF NOT EXISTS products (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(100) NOT NULL UNIQUE,
                                        category VARCHAR(50) NOT NULL,
                                        price DECIMAL(10, 2) NOT NULL,
                                        stock_qty INT DEFAULT 0,
                                        unit VARCHAR(20) DEFAULT 'adet',
                                        critical_level INT DEFAULT 10,
                                        stock_package INT DEFAULT 0,
                                        portions_per_package INT DEFAULT 1,
                                        stock_display VARCHAR(100) DEFAULT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- 3. TABLES Tablosu  (GÜNCELLENDİ — is_deleted EKLENDİ)
CREATE TABLE IF NOT EXISTS tables (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(50) NOT NULL UNIQUE,
                                      is_occupied BOOLEAN DEFAULT FALSE,
                                      is_deleted BOOLEAN DEFAULT FALSE,   -- ✔ EKLENDİ
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Varsayılan 15 masa
INSERT IGNORE INTO tables (name, is_occupied, is_deleted)
VALUES ('Masa 1', FALSE, FALSE),
       ('Masa 2', FALSE, FALSE),
       ('Masa 3', FALSE, FALSE),
       ('Masa 4', FALSE, FALSE),
       ('Masa 5', FALSE, FALSE),
       ('Masa 6', FALSE, FALSE),
       ('Masa 7', FALSE, FALSE),
       ('Masa 8', FALSE, FALSE),
       ('Masa 9', FALSE, FALSE),
       ('Masa 10', FALSE, FALSE),
       ('Masa 11', FALSE, FALSE),
       ('Masa 12', FALSE, FALSE),
       ('Masa 13', FALSE, FALSE),
       ('Masa 14', FALSE, FALSE),
       ('Masa 15', FALSE, FALSE);


-- 4. ORDERS Tablosu
CREATE TABLE IF NOT EXISTS orders (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      table_id INT NOT NULL,
                                      total_amount DECIMAL(10, 2) DEFAULT 0.00,
                                      is_paid BOOLEAN DEFAULT FALSE,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      FOREIGN KEY (table_id) REFERENCES tables(id) ON DELETE CASCADE
) ENGINE=InnoDB;


-- 5. ORDER_ITEMS Tablosu
CREATE TABLE IF NOT EXISTS order_items (
                                           id INT AUTO_INCREMENT PRIMARY KEY,
                                           order_id INT NOT NULL,
                                           product_id INT NOT NULL,
                                           quantity INT NOT NULL,
                                           price_at_order DECIMAL(10, 2) NOT NULL,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                                           FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;


-- 6. REPORTS Tablosu
CREATE TABLE IF NOT EXISTS reports (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       report_date DATE NOT NULL UNIQUE,
                                       total_revenue DECIMAL(10, 2) DEFAULT 0.00,
                                       total_orders INT DEFAULT 0,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- Başlangıç ürünleri
INSERT IGNORE INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES
    ('Çay', 'Sıcak İçecek', 15.00, 1000, 'bardak', 100, 5, 200, '5 paket (1000 bardak)'),
    ('Portakallı Oralet', 'Sıcak İçecek', 25.00, 150, 'bardak', 50, 3, 50, '3 paket (150 bardak)'),
    ('Şeftali Oralet', 'Sıcak İçecek', 25.00, 150, 'bardak', 50, 3, 50, '3 paket (150 bardak)'),
    ('Kuşburnu Oralet', 'Sıcak İçecek', 25.00, 200, 'bardak', 50, 4, 50, '4 paket (200 bardak)'),
    ('Karadut Oralet', 'Sıcak İçecek', 25.00, 150, 'bardak', 50, 3, 50, '3 paket (150 bardak)'),
    ('Muzlu Oralet', 'Sıcak İçecek', 25.00, 150, 'bardak', 50, 3, 50, '3 paket (150 bardak)'),
    ('Türk Kahvesi', 'Sıcak İçecek', 35.00, 80, 'fincan', 20, 4, 20, '4 paket (80 fincan)'),
    ('Ihlamur', 'Sıcak İçecek', 20.00, 100, 'bardak', 50, 2, 50, '2 paket (100 bardak)'),
    ('Kaçak Çay', 'Sıcak İçecek', 20.00, 600, 'bardak', 100, 3, 200, '3 paket (600 bardak)'),
    ('Türk Kahvesi (Sütlü)', 'Sıcak İçecek', 40.00, 60, 'fincan', 20, 3, 20, '3 paket (60 fincan)'),
    ('Sıcak Çikolata', 'Sıcak İçecek', 30.00, 100, 'bardak', 30, 2, 50, '2 paket (100 bardak)'),
    ('Salep', 'Sıcak İçecek', 35.00, 80, 'bardak', 30, 2, 40, '2 paket (80 bardak)');


-- Fazla masaları sil (script tekrar çalışsa bile problem yok)
DELETE FROM tables WHERE id > 15;


-- Kurulum tamamlandı → setup tablosunu işaretle
UPDATE setup SET is_done = TRUE WHERE id = 1;


-- Özet bilgi
SELECT '✅ Veritabanı başarıyla kuruldu!' AS status;
SELECT COUNT(*) AS total_products FROM products;
SELECT COUNT(*) AS total_tables FROM tables WHERE is_deleted = FALSE;
SELECT COUNT(*) AS total_users FROM users;
