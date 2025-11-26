package com.baharkiraathanesi.kiraathane.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Veritabanı Bilgileri
    // Eğer kurulumda şifreni farklı yaptıysan aşağıdan değiştir!
    // Default değerler (geliştirme için). Bu değerleri çalıştırma zamanında environment variable ile
    // DB_URL, DB_USER, DB_PASSWORD olarak geçerseniz onlar kullanılacaktır. Böylece kodu her seferinde
    // değiştirmek zorunda kalmazsınız.
    private static final String DEFAULT_URL = "jdbc:mysql://127.0.0.1:3306/bahar_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    // Bağlantı sağlayan metod
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // 1. Adım: Veritabanı Sürücüsünü Çağır
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Allow override via environment variables or system properties
            String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : System.getProperty("DB_URL", DEFAULT_URL);
            String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : System.getProperty("DB_USER", DEFAULT_USER);
            String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : System.getProperty("DB_PASSWORD", DEFAULT_PASSWORD);

            // 2. Adım: Bağlantıyı Kur
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Veritabanı bağlantısı başarılı -> " + url + " (user: " + user + ")");

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Sürücüsü Bulunamadı! pom.xml dosyanı kontrol et.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Veritabanına Bağlanılamadı! Kullanıcı adı, şifre veya sunucuyu kontrol et. (" + e.getMessage() + ")");
            // Daha kısa stacktrace: sadece message gösteriliyor. Tam trace gerekiyorsa developer modu açabilirsiniz.
            e.printStackTrace();
        }
        return connection;
    }
}