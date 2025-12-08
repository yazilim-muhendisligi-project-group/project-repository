package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public boolean authenticate(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            return false;
        }

        final String SQL = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                LOGGER.warning("Veritabani baglantisi kurulamadi");
                return false;
            }

            stmt.setString(1, username.trim());
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    LOGGER.info("Kullanici bulundu: " + username + " (Rol: " + role + ")");
                    return true;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kimlik dogrulama hatasi", e);
        }

        return false;
    }

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
            LOGGER.log(Level.SEVERE, "Kullanici rolu alinirken hata", e);
        }

        return null;
    }
}