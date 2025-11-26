package com.baharkiraathanesi.kiraathane.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Veritabanı Bilgileri - ✅ Çalışan Konfigürasyon
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bahar_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "selamveduaile";

    // Bağlantı sağlayan metod
    public static Connection getConnection() {
        Connection connection = null;

        try {
            // 1. Adım: Veritabanı Sürücüsünü Çağır
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Environment variable kontrolü (opsiyonel, production için)
            String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : DB_URL;
            String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DB_USER;
            String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DB_PASSWORD;

            // 2. Adım: Bağlantıyı Kur
            connection = DriverManager.getConnection(url, user, password);

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Sürücüsü Bulunamadı! pom.xml dosyasını kontrol et.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ VERİTABANI BAĞLANTI HATASI!");
            System.err.println("┌─────────────────────────────────────────────────────────────┐");
            System.err.println("│ Hata: " + e.getMessage());
            System.err.println("│");
            System.err.println("│ ÇÖZÜMLERİ DENE:");
            System.err.println("│ 1. MySQL'in çalıştığından emin ol:");
            System.err.println("│    sudo /usr/local/mysql/support-files/mysql.server start");
            System.err.println("│");
            System.err.println("│ 2. Şifreyi kontrol et (şu anki: " + DB_PASSWORD + ")");
            System.err.println("│");
            System.err.println("│ 3. Veritabanını kontrol et:");
            System.err.println("│    /usr/local/mysql/bin/mysql -u root -p");
            System.err.println("└─────────────────────────────────────────────────────────────┘");
        }

        return connection;
    }
}