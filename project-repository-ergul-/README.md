# ☕ Bahar Kıraathanesi - Cafe & Tea House Management System

<p align="center">
  <img src="src/main/resources/images/cay_icon.png" alt="Bahar Kıraathanesi Logo" width="120"/>
</p>

<p align="center">
  <strong>Modern, kullanımı kolay kıraathane ve kafe yönetim sistemi</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=flat-square" alt="JavaFX 21"/>
  <img src="https://img.shields.io/badge/MySQL-8.x-blue?style=flat-square&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License"/>
</p>

---

## 📋 İçindekiler

- [✨ Özellikler](#-özellikler)
- [🖥️ Ekran Görüntüleri](#️-ekran-görüntüleri)
- [🚀 Kurulum](#-kurulum)
- [📖 Kullanım Kılavuzu](#-kullanım-kılavuzu)
- [🔧 Sorun Giderme](#-sorun-giderme)
- [📞 Destek](#-destek)

---

## ✨ Özellikler

| Özellik | Açıklama |
|---------|----------|
| 🔐 **Güvenli Giriş** | SQL tabanlı kullanıcı doğrulama sistemi |
| 🪑 **Masa Yönetimi** | Masaları ekleyin, silin ve durumlarını takip edin |
| 🛒 **Sipariş Takibi** | Kolay ve hızlı sipariş alma arayüzü |
| 📦 **Stok Kontrolü** | Ürün ve envanter yönetimi |
| 📊 **Raporlama** | Günlük, haftalık ve aylık satış raporları |
| 🧾 **Z Raporu** | PDF formatında gün sonu raporu oluşturma |

---

## 🖥️ Ne Yapar Bu Uygulama?

Bu uygulama, kafe, kıraathane ve benzeri işletmeler için tasarlanmış bir **satış noktası (POS) sistemidir**.

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   👤 Giriş Yap  →  🏠 Ana Menü  →  📋 İşlem Seç            │
│                                                             │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│   │   📦 Stok   │  │  🪑 Masalar │  │  📊 Rapor   │        │
│   └─────────────┘  └─────────────┘  └─────────────┘        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Kurulum

### 📋 Gereksinimler

| Gereksinim | Minimum Versiyon |
|------------|------------------|
| ☕ Java | 21 veya üzeri |
| 🐬 MySQL | 8.0 veya üzeri |
| 💻 İşletim Sistemi | Windows / macOS / Linux |

---

### 1️⃣ Java Kurulumu

<details>
<summary>🍎 <strong>macOS</strong> (tıklayın)</summary>

```bash
# Homebrew ile kurulum
brew install openjdk@21

# Homebrew yoksa önce onu kurun:
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

</details>

<details>
<summary>🪟 <strong>Windows</strong> (tıklayın)</summary>

1. 🌐 [Adoptium](https://adoptium.net/) sitesine gidin
2. 📥 **"Latest LTS Release"** butonuna tıklayın
3. 📦 `.msi` dosyasını indirin
4. 🖱️ Çift tıklayıp kurulum sihirbazını takip edin

</details>

<details>
<summary>🐧 <strong>Linux</strong> (tıklayın)</summary>

```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

</details>

#### ✅ Kurulum Kontrolü

```bash
java -version
# Çıktı: openjdk version "21.0.x" ...
```

---

### 2️⃣ MySQL Kurulumu

<details>
<summary>🍎 <strong>macOS</strong></summary>

```bash
brew install mysql
brew services start mysql
mysql_secure_installation
```

</details>

<details>
<summary>🪟 <strong>Windows</strong></summary>

1. 🌐 [MySQL Downloads](https://dev.mysql.com/downloads/installer/) sayfasına gidin
2. 📥 "MySQL Installer for Windows" indirin
3. 🖱️ "Developer Default" seçeneğiyle kurun
4. 🔑 Root şifresi belirleyin (unutmayın!)

</details>

<details>
<summary>🐧 <strong>Linux</strong></summary>

```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

</details>

---

### 3️⃣ Veritabanı Kurulumu

```bash
# Proje klasörüne gidin
cd /path/to/project-repository-ergul-

# MySQL'e bağlanın
mysql -u root -p

# Setup script'ini çalıştırın
source setup_database.sql

# Çıkış
exit
```

> 💡 **İpucu:** Bu komut otomatik olarak tüm tabloları, örnek ürünleri ve varsayılan kullanıcıları oluşturur.

---

### 4️⃣ Veritabanı Bağlantısı Ayarları

Eğer MySQL şifreniz varsayılandan farklıysa:

📁 `src/main/java/.../database/DatabaseConnection.java` dosyasını açın:

```java
// Bu satırı bulun ve şifrenizi girin:
private static final String DB_PASSWORD = getEnv("DB_PASSWORD", "sizin_sifreniz");
```

---

### 5️⃣ Uygulamayı Çalıştırma

```bash
# macOS / Linux
./mvnw clean javafx:run

# Windows
mvnw.cmd clean javafx:run
```

🎉 **Tebrikler!** Uygulama başlatıldı.

---

## 📖 Kullanım Kılavuzu

### 🔐 Giriş Bilgileri

| 👤 Kullanıcı Adı | 🔑 Şifre |
|------------------|----------|
| `yonetici` | `1234` |
| `admin` | `admin123` |

---

### 🏠 Ana Menü

Giriş yaptıktan sonra üç ana bölüm görürsünüz:

```
┌────────────────────────────────────────────────┐
│                  ANA MENÜ                      │
├────────────────────────────────────────────────┤
│                                                │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│   │    📦    │  │    🪑    │  │    📊    │    │
│   │   STOK   │  │  MASALAR │  │  RAPOR   │    │
│   └──────────┘  └──────────┘  └──────────┘    │
│                                                │
└────────────────────────────────────────────────┘
```

---

### 🪑 Masa İşlemleri

| Renk | Durum | Eylem |
|------|-------|-------|
| 🔵 Mavi | Boş masa | Tıklayarak sipariş ekranını açın |
| 🔴 Kırmızı | Dolu masa | Mevcut siparişi görüntüleyin |

**İşlemler:**
- 🖱️ **Sol tık** → Sipariş ekranını aç
- 🖱️ **Sağ tık** → Masayı sil (boşsa)
- ➕ **Yeni Masa** → Otomatik numaralandırılmış masa ekle

---

### 🛒 Sipariş Alma

```
1️⃣ Masaya tıklayın
2️⃣ Ürünlere tıklayarak sipariş ekleyin
3️⃣ Toplam otomatik hesaplanır
4️⃣ "Hesabı Kapat" ile ödeme alın
```

| Buton | İşlev |
|-------|-------|
| 🗑️ Seçili Sil | Seçilen ürünü listeden çıkar |
| 🧹 Tümünü Temizle | Tüm siparişi iptal et |
| 💰 Hesabı Kapat | Ödeme al ve masayı boşalt |

---

### 📦 Stok Yönetimi

| İşlem | Nasıl Yapılır |
|-------|---------------|
| ➕ Ürün Ekle | "+" butonuna tıklayın |
| ✏️ Stok Güncelle | Ürüne çift tıklayın |
| 🗑️ Ürün Sil | Ürünü seçip "Sil" butonuna tıklayın |

---

### 📊 Z Raporu

Gün sonu raporu almak için:

```
1️⃣ "Z Raporu" ekranına gidin
2️⃣ Günlük satışları inceleyin
3️⃣ "Z Raporu Al" butonuna tıklayın
4️⃣ PDF dosyasını kaydedin
```

> ⚠️ **Önemli:** Z raporu almadan önce tüm açık hesapları kapatmalısınız!

---

## 🔧 Sorun Giderme

<details>
<summary>❌ <strong>Uygulama açılmıyor</strong></summary>

1. Java kurulumunu kontrol edin: `java -version`
2. MySQL'in çalıştığından emin olun
3. `setup_database.sql` dosyasını tekrar çalıştırın

</details>

<details>
<summary>❌ <strong>Veritabanı bağlantı hatası</strong></summary>

1. MySQL servisinin çalıştığını kontrol edin
2. `DatabaseConnection.java` dosyasındaki şifreyi kontrol edin
3. Veritabanı adının `bahar_db` olduğundan emin olun

</details>

<details>
<summary>❌ <strong>Ürünler görünmüyor</strong></summary>

MySQL'de kontrol edin:
```sql
USE bahar_db;
SELECT * FROM products;
```

Boşsa, `setup_database.sql` tekrar çalıştırın.

</details>

---

## 🛠️ Teknik Bilgiler

| Bileşen | Teknoloji |
|---------|-----------|
| 💻 Programlama Dili | Java 21 |
| 🎨 UI Framework | JavaFX 21 |
| 🗄️ Veritabanı | MySQL 8.x |
| 📄 PDF Oluşturma | Apache PDFBox 3.0 |
| 🔧 Build Tool | Maven |

---

## 📞 Destek

Sorularınız veya önerileriniz için:

- 📧 İletişim: [Proje sahibine ulaşın]
- 🐛 Hata Bildirimi: GitHub Issues kullanın

---

<p align="center">
  <sub>☕ Bahar Kıraathanesi Yönetim Sistemi ile yapıldı</sub>
</p>

<p align="center">
  <sub>© 2025 - Tüm hakları saklıdır</sub>
</p>

