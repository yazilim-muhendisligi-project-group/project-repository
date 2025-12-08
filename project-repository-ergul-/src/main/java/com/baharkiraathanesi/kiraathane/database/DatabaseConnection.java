package com.baharkiraathanesi.kiraathane.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static String dbHost;
    private static String dbPort;
    private static String dbName;
    private static String dbUser;
    private static String dbPassword;
    private static String dbUrl;

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
                dbHost = props.getProperty("db.host", "localhost");
                dbPort = props.getProperty("db.port", "3306");
                dbName = props.getProperty("db.name", "bahar_db");
                dbUser = props.getProperty("db.user", "root");
                dbPassword = props.getProperty("db.password", "");
                LOGGER.info("db.properties dosyasindan ayarlar yuklendi");
            } else {
                loadFromEnvironmentVariables();
                LOGGER.warning("db.properties bulunamadi, ortam degiskenleri kullaniliyor");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "db.properties okunamadi, ortam degiskenleri kullaniliyor", e);
            loadFromEnvironmentVariables();
        }

        dbUrl = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul&characterEncoding=UTF-8",
            dbHost, dbPort, dbName
        );
    }

    private static void loadFromEnvironmentVariables() {
        dbHost = getEnv("DB_HOST", "localhost");
        dbPort = getEnv("DB_PORT", "3306");
        dbName = getEnv("DB_NAME", "bahar_db");
        dbUser = getEnv("DB_USER", "root");
        dbPassword = getEnv("DB_PASSWORD", "");
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                LOGGER.info("Veritabani baglantisi basarili: " + dbName);
            }
            return connection;

        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL Driver bulunamadi. pom.xml kontrol edin.", e);
            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabani baglanti hatasi: " + e.getMessage(), e);
            logConnectionHelp();
            return null;
        }
    }

    private static void logConnectionHelp() {
        LOGGER.severe("VERITABANI BAGLANTISI KURULAMADI");
        LOGGER.info("COZUM ADIMLARI:");
        LOGGER.info("1. MySQL calistigini kontrol edin");
        LOGGER.info("2. Veritabanini kurun: mysql -u root -p < setup_database.sql");
        LOGGER.info("3. Baglanti bilgilerini kontrol edin:");
        LOGGER.info("   Host: " + dbHost);
        LOGGER.info("   Port: " + dbPort);
        LOGGER.info("   Database: " + dbName);
        LOGGER.info("   User: " + dbUser);
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Veritabani baglantisi kapatildi");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Baglanti kapatilirken hata olustu", e);
            }
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Baglanti testi basarisiz", e);
            return false;
        }
    }
}

