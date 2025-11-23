-- Bahar Kıraathanesi Ürün Veritabanı Güncellemesi
-- Paket ve porsiyon takibi için yeni sistem

USE bahar_db;

-- Önce products tablosuna yeni kolonlar ekle (eğer yoksa)
ALTER TABLE products
ADD COLUMN IF NOT EXISTS stock_package INT DEFAULT 0 COMMENT 'Paket sayısı',
ADD COLUMN IF NOT EXISTS portions_per_package INT DEFAULT 1 COMMENT 'Paket başına porsiyon sayısı',
ADD COLUMN IF NOT EXISTS stock_display VARCHAR(100) DEFAULT NULL COMMENT 'Stok gösterim metni';

-- Mevcut ürünleri temizle (isteğe bağlı)
DELETE FROM products;

-- Yeni ürünleri ekle
-- Format: (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)

-- Çay (1 paket = 200 bardak, 5 paket = 1000 bardak)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Çay', 'Sıcak İçecek', 15.00, 1000, 'bardak', 100, 5, 200, '5 paket (1000 bardak)');

-- Portakallı Oralet (1 paket = 50 bardak, 3 paket = 150 bardak)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Portakallı Oralet', 'Sıcak İçecek', 25.00, 150, 'bardak', 50, 3, 50, '3 paket (150 bardak)');

-- Şeftali Oralet (1 paket = 50 bardak, 3 paket = 150 bardak)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Şeftali Oralet', 'Sıcak İçecek', 25.00, 150, 'bardak', 50, 3, 50, '3 paket (150 bardak)');

-- Kuşburnu Oralet (1 paket = 50 bardak, 4 paket = 200 bardak)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Kuşburnu Oralet', 'Sıcak İçecek', 25.00, 200, 'bardak', 50, 4, 50, '4 paket (200 bardak)');

-- Karadut Oralet (1 paket = 50 bardak, 3 paket = 150 bardak)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Karadut Oralet', 'Sıcak İçecek', 25.00, 150, 'bardak', 50, 3, 50, '3 paket (150 bardak)');

-- Muzlu Oralet (1 paket = 50 bardak, 3 paket = 150 bardak)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Muzlu Oralet', 'Sıcak İçecek', 25.00, 150, 'bardak', 50, 3, 50, '3 paket (150 bardak)');

-- Türk Kahvesi (1 paket = 20 fincan, 4 paket = 80 fincan)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Türk Kahvesi', 'Sıcak İçecek', 35.00, 80, 'fincan', 20, 4, 20, '4 paket (80 fincan)');

-- Ihlamur (1 paket = 50 bardak, 2 paket = 100 bardak)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Ihlamur', 'Sıcak İçecek', 20.00, 100, 'bardak', 50, 2, 50, '2 paket (100 bardak)');

-- Kaçak Çay (1 paket = 200 bardak, 3 paket = 600 bardak)
INSERT INTO products (name, category, price, stock_qty, unit, critical_level, stock_package, portions_per_package, stock_display)
VALUES ('Kaçak Çay', 'Sıcak İçecek', 20.00, 600, 'bardak', 100, 3, 200, '3 paket (600 bardak)');

-- Ürünleri kontrol et
SELECT
    id,
    name as 'Ürün Adı',
    category as 'Kategori',
    price as 'Fiyat (TL)',
    stock_package as 'Paket',
    portions_per_package as 'Paket/Porsiyon',
    stock_qty as 'Toplam Porsiyon',
    stock_display as 'Stok Durumu',
    unit as 'Birim'
FROM products
ORDER BY name;

-- Stok güncelleme için trigger (otomatik hesaplama)
DELIMITER //
CREATE TRIGGER IF NOT EXISTS update_stock_display
BEFORE UPDATE ON products
FOR EACH ROW
BEGIN
    SET NEW.stock_display = CONCAT(NEW.stock_package, ' paket (', NEW.stock_qty, ' ', NEW.unit, ')');
END//
DELIMITER ;
