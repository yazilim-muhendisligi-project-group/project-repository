package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
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
            e.printStackTrace();
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
            System.err.println("❌ Masa eklenirken hata: " + e.getMessage());
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