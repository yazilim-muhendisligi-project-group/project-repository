package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.database.DatabaseUpdater;
import com.baharkiraathanesi.kiraathane.model.Table;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    // 1. Tüm Masaları Getir
    public List<Table> getAllTables() {
        List<Table> tableList = new ArrayList<>();
        String sql = "SELECT * FROM tables";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Table table = new Table(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBoolean("is_occupied")
                );
                tableList.add(table);
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            e.printStackTrace();
            if (msg.contains("doesn't exist") || msg.toLowerCase().contains("does not exist") || msg.toLowerCase().contains("unknown table")) {
                System.err.println("❌ 'tables' tablosu bulunamadı - veritabanı güncellemesi çalıştırılıyor...");
                try {
                    DatabaseUpdater.updateDatabase();
                } catch (Exception ex) {
                    System.err.println("❌ Veritabanı güncellemesi başarısız: " + ex.getMessage());
                }

                // Tekrar dene
                try (Connection conn2 = DatabaseConnection.getConnection();
                     Statement stmt2 = conn2.createStatement();
                     ResultSet rs2 = stmt2.executeQuery(sql)) {

                    while (rs2.next()) {
                        Table table = new Table(
                                rs2.getInt("id"),
                                rs2.getString("name"),
                                rs2.getBoolean("is_occupied")
                        );
                        tableList.add(table);
                    }

                } catch (SQLException ex2) {
                    System.err.println("❌ Yeniden deneme sırasında hata: " + ex2.getMessage());
                    ex2.printStackTrace();
                }
            }
        }
        return tableList;
    }

    // 2. Masa Durumunu Güncelle (Dolu/Boş Yap)
    public void updateTableStatus(int tableId, boolean isOccupied) {
        String sql = "UPDATE tables SET is_occupied = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, isOccupied);
            stmt.setInt(2, tableId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Yeni Masa Ekle
    public boolean addTable(String tableName) {
        String sql = "INSERT INTO tables (name, is_occupied) VALUES (?, FALSE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tableName);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            System.err.println("❌ Masa eklenirken hata: " + msg);
            if (msg.contains("doesn't exist") || msg.toLowerCase().contains("does not exist") || msg.toLowerCase().contains("unknown table")) {
                System.err.println("❌ 'tables' tablosu bulunamadı - veritabanı güncellemesi çalıştırılıyor...");
                try {
                    DatabaseUpdater.updateDatabase();
                } catch (Exception ex) {
                    System.err.println("❌ Veritabanı güncellemesi başarısız: " + ex.getMessage());
                }

                // Tekrar dene
                try (Connection conn2 = DatabaseConnection.getConnection();
                     PreparedStatement stmt2 = conn2.prepareStatement(sql)) {
                    stmt2.setString(1, tableName);
                    int affectedRows = stmt2.executeUpdate();
                    return affectedRows > 0;
                } catch (SQLException ex2) {
                    System.err.println("❌ Yeniden deneme sırasında hata: " + ex2.getMessage());
                    ex2.printStackTrace();
                    return false;
                }
            }

            e.printStackTrace();
            return false;
        }
    }

    // 4. Masa Sil
    public boolean deleteTable(int tableId) {
        String sql = "DELETE FROM tables WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Masa silinirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}