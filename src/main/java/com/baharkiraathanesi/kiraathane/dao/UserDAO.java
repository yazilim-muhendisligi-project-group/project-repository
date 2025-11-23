package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Giriş Kontrolü Yapan Metot
    public boolean login(String username, String password) {
        // SQL Sorgumuz: "Bu isimde ve bu şifrede bir kullanıcı var mı?"
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Soru işaretleri yerine gelen verileri koyuyoruz
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            // rs.next() true dönerse, böyle bir kullanıcı bulundu demektir
            if (rs.next()) {
                return true; // Giriş Başarılı
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Giriş Başarısız
    }
}