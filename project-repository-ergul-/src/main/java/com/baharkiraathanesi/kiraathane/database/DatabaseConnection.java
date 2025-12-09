package com.baharkiraathanesi.kiraathane.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
            } else {
                loadFromEnvironmentVariables();
            }
        } catch (IOException e) {
            loadFromEnvironmentVariables();
        }

        // ÖNEMLİ GÜNCELLEME: createDatabaseIfNotExist=true eklendi.
        // Bu, veritabanı yoksa bağlantı hatası vermek yerine oluşturulmasını sağlar.
        dbUrl = String.format(
                "jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul&characterEncoding=UTF-8",
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
                // Driver kontrolü opsiyoneldir modern JDBC'de ama kalabilir
                // Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            }
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabani baglanti hatasi: " + e.getMessage(), e);
            logConnectionHelp();
            return null;
        }
    }

    // --- YENİ EKLENEN METOD: setupDatabase ---
    public static void setupDatabase() {
        LOGGER.info("Veritabani kurulumu baslatiliyor...");

        // setup_database.sql dosyasını oku
        InputStream inputStream = DatabaseConnection.class.getResourceAsStream("/db/setup_database.sql");
        if (inputStream == null) {
            LOGGER.severe("setup_database.sql dosyasi resources klasorunde bulunamadi!");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             Connection conn = getConnection();
             Statement statement = conn.createStatement()) {

            if (conn == null) return;

            // Dosyayı string olarak oku
            String sqlScript = reader.lines().collect(Collectors.joining("\n"));

            // Noktalı virgüle göre komutları ayır
            String[] sqlStatements = sqlScript.split(";");

            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    try {
                        statement.execute(sql);
                    } catch (SQLException e) {
                        // Tablo zaten varsa veya veri duplicate ise logla ama programı durdurma
                        LOGGER.warning("SQL Komut Hatasi (Onemsiz olabilir): " + e.getMessage());
                    }
                }
            }
            LOGGER.info("Veritabani kurulum islemleri tamamlandi.");

        } catch (IOException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabani kurulumunda hata", e);
        }
    }

    private static void logConnectionHelp() {
        LOGGER.severe("VERITABANI BAGLANTISI KURULAMADI");
        LOGGER.info("1. MySQL servisinin calistigindan emin olun.");
        LOGGER.info("2. Host: " + dbHost + ", Port: " + dbPort);
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}