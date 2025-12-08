package com.baharkiraathanesi.kiraathane.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static String DB_HOST;
    private static String DB_PORT;
    private static String DB_NAME;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_URL;

    private static Connection connection = null;

    static {
        loadProperties();
    }

    private DatabaseConnection() {
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
                DB_HOST = props.getProperty("db.host", "localhost");
                DB_PORT = props.getProperty("db.port", "3306");
                DB_NAME = props.getProperty("db.name", "bahar_db");
                DB_USER = props.getProperty("db.user", "root");
                DB_PASSWORD = props.getProperty("db.password", "");
                LOGGER.info("db.properties dosyasından ayarlar yüklendi");
            } else {
                // Fallback to environment variables
                DB_HOST = getEnv("DB_HOST", "localhost");
                DB_PORT = getEnv("DB_PORT", "3306");
                DB_NAME = getEnv("DB_NAME", "bahar_db");
                DB_USER = getEnv("DB_USER", "root");
                DB_PASSWORD = getEnv("DB_PASSWORD", "");
                LOGGER.warning("db.properties bulunamadı, ortam değişkenleri kullanılıyor");
            }
        } catch (IOException e) {
            LOGGER.warning("db.properties okunamadı: " + e.getMessage());
            DB_HOST = getEnv("DB_HOST", "localhost");
            DB_PORT = getEnv("DB_PORT", "3306");
            DB_NAME = getEnv("DB_NAME", "bahar_db");
            DB_USER = getEnv("DB_USER", "root");
            DB_PASSWORD = getEnv("DB_PASSWORD", "");
        }

        DB_URL = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul&characterEncoding=UTF-8",
            DB_HOST, DB_PORT, DB_NAME
        );
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LOGGER.info("Veritabanı bağlantısı başarılı: " + DB_NAME);
            }
            return connection;

        } catch (ClassNotFoundException e) {
            LOGGER.info("MySQL Driver bulunamadı! pom.xml'i kontrol edin.");
            return null;

        } catch (SQLException e) {
            LOGGER.info("Veritabanı bağlantı hatası: " + e.getMessage());
            System.out.println("\nVERİTABANI BAĞLANTISI KURULAMADI!");
            System.out.println("Hata: " + e.getMessage());
            System.out.println("\nÇÖZÜM ADIMLARI:");
            System.out.println("1. MySQL'in çalıştığını kontrol edin");
            System.out.println("2. Veritabanını kurun: /usr/local/mysql/bin/mysql -u root -p < setup_database.sql");
            System.out.println("3. Bağlantı bilgilerini kontrol edin:");
            System.out.println("   Host: " + DB_HOST);
            System.out.println("   Port: " + DB_PORT);
            System.out.println("   Database: " + DB_NAME);
            System.out.println("   User: " + DB_USER);
            return null;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Veritabanı bağlantısı kapatıldı");
            } catch (SQLException e) {
                LOGGER.info("Bağlantı kapatılırken hata oluştu: " + e.getMessage());
            }
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}