package com.baharkiraathanesi.kiraathane.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Veritabanı Bilgileri
    // Eğer kurulumda şifreni farklı yaptıysan aşağıdan değiştir!
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/bahar_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Ferzub-8webno-wignap";

    // Bağlantı sağlayan metod
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // 1. Adım: Veritabanı Sürücüsünü Çağır
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Adım: Bağlantıyı Kur
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            // System.out.println("Veritabanı bağlantısı başarılı!"); // Test ederken açabilirsin

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Sürücüsü Bulunamadı! pom.xml dosyanı kontrol et.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Veritabanına Bağlanılamadı! Kullanıcı adı, şifre veya sunucuyu kontrol et.");
            e.printStackTrace();
        }
        return connection;
    }
}