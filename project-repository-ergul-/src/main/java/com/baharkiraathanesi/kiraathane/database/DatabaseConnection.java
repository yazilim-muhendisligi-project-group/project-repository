package com.baharkiraathanesi.kiraathane.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
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

    private DatabaseConnection() {}

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

        // createDatabaseIfNotExist= true → veritabanı yoksa bağlantı sırasında otomatik oluşturur
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
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            }
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabani baglanti hatasi: " + e.getMessage(), e);
            logConnectionHelp();
            return null;
        }
    }

    // ===========================
    //   VERITABANI VAR MI KONTROL
    // ===========================
    private static boolean databaseExists() {
        String checkSql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + dbHost + ":" + dbPort + "/?useSSL=false&allowPublicKeyRetrieval=true",
                dbUser,
                dbPassword
        );
             PreparedStatement ps = conn.prepareStatement(checkSql)) {

            ps.setString(1, dbName);
            ResultSet rs = ps.executeQuery();
            return rs.next();  // varsa true döner

        } catch (SQLException e) {
            LOGGER.warning("Veritabani kontrol edilemedi: " + e.getMessage());
            return false;
        }
    }

    // ===========================
    //   VERITABANI KURULUMU
    // ===========================
    public static void setupDatabase() {
        LOGGER.info("Veritabani kurulumu baslatiliyor...");

        // Eğer veritabanı zaten varsa kurulumu çalıştırma
        if (databaseExists()) {
            LOGGER.info(dbName + " veritabani zaten mevcut. Kurulum atlandi.");
            return;
        }

        LOGGER.info(dbName + " bulunamadi. Kurulum SQL dosyasi uygulanacak...");

        InputStream inputStream = DatabaseConnection.class.getResourceAsStream("/db/setup_database.sql");
        if (inputStream == null) {
            LOGGER.severe("setup_database.sql dosyasi resources/db klasorunde bulunamadi!");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // Dosyayı oku
            String sqlScript = reader.lines().collect(Collectors.joining("\n"));

            try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {

                if (conn == null) return;

                // SQL komutlarını ; ile böl
                String[] sqlStatements = sqlScript.split(";");

                for (String sql : sqlStatements) {
                    if (!sql.trim().isEmpty()) {
                        try {
                            statement.execute(sql);
                        } catch (SQLException e) {
                            LOGGER.warning("SQL Komut Hatasi (Onemsiz olabilir): " + e.getMessage());
                        }
                    }
                }

                LOGGER.info("Veritabani kurulum islemleri BASARIYLA tamamlandi.");
            }

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
            try { connection.close(); }
            catch (SQLException ignored) {}
        }
    }
}
