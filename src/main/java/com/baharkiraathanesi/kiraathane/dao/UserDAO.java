package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User Data Access Object
 * Kullanıcı işlemleri için veritabanı erişim katmanı
 */
public class UserDAO {

    /**
     * Kullanıcı girişini doğrular
     *
     * @param username Kullanıcı adı
     * @param password Şifre
     * @return Giriş başarılıysa true, değilse false
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            return false;
        }

        final String SQL = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                System.out.println("❌ UserDAO: Veritabanı bağlantısı kurulamadı!");
                return false;
            }

            stmt.setString(1, username.trim());
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    System.out.println("✅ Kullanıcı bulundu: " + username + " (Rol: " + role + ")");
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ UserDAO authenticate hatası: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Kullanıcının rolünü getirir
     *
     * @param username Kullanıcı adı
     * @return Kullanıcı rolü (admin, user vb.), bulunamazsa null
     */
    public String getUserRole(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        final String SQL = "SELECT role FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return null;
            }

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ UserDAO getUserRole hatası: " + e.getMessage());
        }

        return null;
    }
}