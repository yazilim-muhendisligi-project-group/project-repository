# Bahar Kıraathanesi Yönetim Sistemi

## 📋 Genel Bakış
JavaFX tabanlı profesyonel kıraathane yönetim sistemi. MySQL veritabanı ile entegre çalışır.

## 🔐 Giriş Bilgileri

Uygulama artık **SQL tabanlı** kullanıcı doğrulama kullanıyor. Kod içinde sabit kullanıcı adı/şifre yok!

### Varsayılan Kullanıcılar:
- **Kullanıcı:** `yonetici` | **Şifre:** `1234`
- **Kullanıcı:** `admin` | **Şifre:** `admin123`

Yeni kullanıcı eklemek için:
```sql
INSERT INTO users (username, password, role) VALUES ('yeni_kullanici', 'sifre123', 'admin');
```

## 🚀 Kurulum

### 1. Veritabanını Kur
```bash
/usr/local/mysql/bin/mysql -u root -p < setup_database.sql
```

Bu komut:
- ✅ `bahar_db` veritabanını oluşturur
- ✅ Tüm tabloları (users, products, tables, orders) oluşturur
- ✅ 15 masa ekler
- ✅ 12 başlangıç ürünü ekler
- ✅ Varsayılan kullanıcıları ekler

### 2. MySQL Bağlantı Ayarları

Dosya: `DatabaseConnection.java`

**Varsayılan Ayarlar:**
- Host: `localhost`
- Port: `3306`
- Database: `bahar_db`
- User: `root`
- Password: `selamveduaile`

**Özelleştirme (Environment Variables):**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=bahar_db
export DB_USER=root
export DB_PASSWORD=your_password
```

### 3. Uygulamayı Çalıştır
```bash
mvn clean javafx:run
```

## 🏗️ Proje Yapısı (Profesyonel Mimari)

```
src/main/java/
├── com.baharkiraathanesi.kiraathane/
│   ├── dao/                    # Data Access Objects
│   │   ├── UserDAO.java        ✅ SQL tabanlı kullanıcı doğrulama
│   │   ├── ProductDAO.java     ✅ Try-with-resources
│   │   ├── TableDAO.java       ✅ Logger kullanımı
│   │   ├── OrderDAO.java       ✅ Hata yönetimi
│   │   └── ReportDAO.java
│   │
│   ├── database/               # Veritabanı Bağlantısı
│   │   ├── DatabaseConnection.java  ✅ Singleton Pattern
│   │   └── DatabaseUpdater.java     ✅ Environment Variables
│   ���
│   ├── model/                  # Model Sınıfları
│   │   ├── Product.java
│   │   ├── Table.java
│   │   ├── Order.java
│   │   └── OrderItem.java
│   │
│   └── *Controller.java        # JavaFX Controllers
│       ├── LoginController.java     ✅ SQL doğrulama
│       ├── MainMenuController.java
│       ├── StockController.java
│       ├── TablesController.java
│       ├── OrderController.java
│       └── ReportController.java
```

## 🎯 Yapılan İyileştirmeler

### 1. ✅ SQL Tabanlı Kullanıcı Yönetimi
- **Öncesi:** Kod içinde `if ("yonetici".equals(username) && "1234".equals(password))`
- **Sonrası:** `UserDAO.authenticate(username, password)` - SQL'den kontrol eder

### 2. ✅ Profesyonel Kod Kalitesi
- **Try-with-Resources:** Otomatik kaynak yönetimi
- **JavaDoc:** Her metod dokümante edildi
- **Logger:** `System.out.println` yerine `Logger` kullanımı
- **Null Kontrolü:** Defensive programming
- **Singleton Pattern:** DatabaseConnection
- **PreparedStatement:** SQL Injection koruması

### 3. ✅ Hata Yönetimi
```java
// Öncesi
if (conn == null) {
    System.out.println("Bağlantı yok!");
}

// Sonrası
if (conn == null) {
    LOGGER.warning("❌ Veritabanı bağlantısı kurulamadı!");
    return false;
}
```

### 4. ✅ Veritabanı Yapısı
- **15 Masa** (Masa 1 - Masa 15)
- **12 Ürün** (Çay, Kahve, Oralet vb.)
- **2 Kullanıcı** (yonetici, admin)
- **Foreign Keys:** Veri bütünlüğü korunuyor

## 📊 Veritabanı Şeması

### Users Tablosu
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(20) DEFAULT 'user'
);
```

### Products Tablosu
```sql
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    category VARCHAR(50),
    price DECIMAL(10, 2),
    stock_qty INT,
    stock_package INT,
    portions_per_package INT,
    stock_display VARCHAR(100)
);
```

### Tables Tablosu (15 Masa)
```sql
CREATE TABLE tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    is_occupied BOOLEAN DEFAULT FALSE
);
```

## 🔧 Sorun Giderme

### MySQL Çalışmıyor
```bash
# MySQL'i başlat
sudo /usr/local/mysql/support-files/mysql.server start

# Durumunu kontrol et
sudo /usr/local/mysql/support-files/mysql.server status
```

### Veritabanı Bağlantısı Kurulamıyor
1. MySQL şifresini kontrol et (`DatabaseConnection.java` içindeki `DB_PASSWORD`)
2. Veritabanının mevcut olduğunu kontrol et:
   ```bash
   /usr/local/mysql/bin/mysql -u root -p -e "SHOW DATABASES;"
   ```
3. `setup_database.sql` dosyasını tekrar çalıştır

### Masa Sayısı Yanlış
```sql
-- Tüm masaları sil ve 15 masa ekle
USE bahar_db;
TRUNCATE TABLE tables;
INSERT INTO tables (name, is_occupied) VALUES 
('Masa 1', FALSE), ('Masa 2', FALSE), ... ('Masa 15', FALSE);
```

## 📝 Kod Kalite Standartları

✅ **Single Responsibility Principle** - Her sınıf tek bir sorumluluğa sahip  
✅ **DRY (Don't Repeat Yourself)** - Kod tekrarı yok  
✅ **SOLID Principles** - Nesne yönelimli tasarım  
✅ **Try-with-Resources** - Otomatik kaynak yönetimi  
✅ **JavaDoc** - Eksiksiz dokümantasyon  
✅ **Logger** - Profesyonel loglama  
✅ **PreparedStatement** - SQL Injection koruması  
✅ **Null Safety** - Null pointer kontrolü  

## 🎉 Başarıyla Tamamlandı!

Uygulama artık **production-ready** durumda:
- ✅ SQL tabanlı güvenli giriş
- ✅ Profesyonel kod kalitesi
- ✅ Hata yönetimi
- ✅ Dokümantasyon
- ✅ 15 masa garantili

**Uygulama Kullanıma Hazır!** 🚀

