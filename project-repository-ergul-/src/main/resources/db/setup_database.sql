-- Bahar Kıraathanesi Veritabanı Kurulum Scripti
-- Bu script tüm tabloları ve başlangıç verilerini oluşturur

-- Veritabanını oluştur (eğer yoksa)
CREATE DATABASE IF NOT EXISTS bahar_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bahar_db;

-- 1. USERS Tablosu
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Varsayılan kullanıcıları ekle (varsa güncelle)
INSERT INTO users (username, password, role)
VALUES ('yonetici', 'YOUR_PASSWORD_HERE', 'admin')
ON DUPLICATE KEY UPDATE password='YOUR_PASSWORD_HERE', role='admin';

INSERT INTO users (username, password, role)
VALUES ('admin', 'YOUR_PASSWORD_HERE', 'admin')
ON DUPLICATE KEY UPDATE password='YOUR_PASSWORD_HERE', role='admin';

-- 2. PRODUCTS Tablosu
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock_qty INT DEFAULT 0,
    unit VARCHAR(20) DEFAULT 'adet',
    critical_level INT DEFAULT 10,
    stock_package INT DEFAULT 0 COMMENT 'Paket sayısı',
    portions_per_package INT DEFAULT 1 COMMENT 'Paket başına porsiyon',
    stock_display VARCHAR(100) DEFAULT NULL COMMENT 'Stok gösterimi',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. TABLES Tablosu (Kıraathane masaları) - SADECE 15 MASA
CREATE TABLE IF NOT EXISTS tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    is_occupied BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Varsayılan 15 masayı ekle (eğer yoksa)
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 1' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 2' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 3' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 4' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 5' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 6' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 7' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 8' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 9' , FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 10', FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 11', FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 12', FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 13', FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 14', FALSE);
INSERT IGNORE INTO tables (name, is_occupied) VALUES ('Masa 15', FALSE);

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

-- 6. REPORTS Tablosu (İsteğe bağlı, günlük raporlar için)
CREATE TABLE IF NOT EXISTS reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    report_date DATE NOT NULL UNIQUE,
    total_revenue DECIMAL(10, 2) DEFAULT 0.00,
    total_orders INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Başlangıç ürünlerini ekle
DELETE FROM products;

INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
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

-- Fazla masaları sil (sadece 15 masa kalmalı)
DELETE FROM tables WHERE id > 15;

-- Kurulum tamamlandı mesajı
SELECT '✅ Veritabanı başarıyla kuruldu!' AS status;
SELECT COUNT(*) AS total_products FROM products;
SELECT COUNT(*) AS total_tables FROM tables;
SELECT COUNT(*) AS total_users FROM users;
