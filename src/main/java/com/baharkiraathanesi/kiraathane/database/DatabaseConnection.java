package com.baharkiraathanesi.kiraathane.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Veritabanı bağlantı yöneticisi
 * Singleton pattern kullanarak MySQL bağlantısı sağlar
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Veritabanı Yapılandırması
    private static final String DB_HOST = getEnv("DB_HOST", "localhost");
    private static final String DB_PORT = getEnv("DB_PORT", "3306");
    private static final String DB_NAME = getEnv("DB_NAME", "bahar_db");
    private static final String DB_USER = getEnv("DB_USER", "root");
    private static final String DB_PASSWORD = getEnv("DB_PASSWORD", "selamveduaile");

    // Bağlantı URL'i
    private static final String DB_URL = String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
        DB_HOST, DB_PORT, DB_NAME
    );

    // Singleton instance
    private static Connection connection = null;

    /**
     * Private constructor - Singleton pattern
     */
    private DatabaseConnection() {
        // Utility class, instantiation engellendi
    }

    /**
     * Environment variable veya default değer döndürür
     */
    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Veritabanı bağlantısı sağlar
     *
     * @return MySQL bağlantısı, başarısızsa null
     */
    public static Connection getConnection() {
        try {
            // Mevcut bağlantı kapalı veya yoksa yeni oluştur
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LOGGER.info("✅ Veritabanı bağlantısı başarılı: " + DB_NAME);
            }
            return connection;

        } catch (ClassNotFoundException e) {
            LOGGER.info("⚠️ MySQL Driver bulunamadı! pom.xml'i kontrol edin.");
            return null;

        } catch (SQLException e) {
            LOGGER.info("⚠️ Veritabanı bağlantı hatası: " + e.getMessage());
            System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
            System.out.println("│ ⚠️ VERİTABANI BAĞLANTISI KURULAMADI!");
            System.out.println("│");
            System.out.println("│ Hata: " + e.getMessage());
            System.out.println("│");
            System.out.println("│ ✅ ÇÖZÜM ADIMLARı:");
            System.out.println("│ 1. MySQL'in çalıştığını kontrol edin:");
            System.out.println("│    sudo /usr/local/mysql/support-files/mysql.server status");
            System.out.println("│");
            System.out.println("│ 2. Veritabanını kurun:");
            System.out.println("│    /usr/local/mysql/bin/mysql -u root -p < setup_database.sql");
            System.out.println("│");
            System.out.println("│ 3. Bağlantı bilgilerini kontrol edin:");
            System.out.println("│    Host: " + DB_HOST);
            System.out.println("│    Port: " + DB_PORT);
            System.out.println("│    Database: " + DB_NAME);
            System.out.println("│    User: " + DB_USER);
            System.out.println("└─────────────────────────────────────────────────────────────┘\n");
            return null;
        }
    }

    /**
     * Bağlantıyı kapatır
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("✅ Veritabanı bağlantısı kapatıldı");
            } catch (SQLException e) {
                LOGGER.info("⚠️ Bağlantı kapatılırken hata oluştu: " + e.getMessage());
            }
        }
    }

    /**
     * Bağlantı test metodu
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}