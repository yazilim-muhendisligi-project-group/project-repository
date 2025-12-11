package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TableDAO {

    private static final Logger LOGGER = Logger.getLogger(TableDAO.class.getName());

    public List<Table> getAllTables() {
        List<Table> tableList = new ArrayList<>();
        final String SQL = "SELECT * FROM tables WHERE is_deleted = FALSE ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            if (conn == null) {
                LOGGER.warning("Veritabani baglantisi kurulamadi");
                return tableList;
            }

            while (rs.next()) {
                Table table = new Table(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBoolean("is_occupied")
                );
                tableList.add(table);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Masalar getirilirken hata olustu", e);
        }

        return tableList;
    }

    public boolean updateTableStatus(int tableId, boolean isOccupied) {
        final String SQL = "UPDATE tables SET is_occupied = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) return false;

            stmt.setBoolean(1, isOccupied);
            stmt.setInt(2, tableId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Masa durumu guncellenirken hata: ID=" + tableId, e);
        }

        return false;
    }

    public boolean addTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            LOGGER.warning("Masa adi bos olamaz");
            return false;
        }

        final String SQL = "INSERT INTO tables (name, is_occupied, is_deleted) VALUES (?, FALSE, FALSE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) return false;

            stmt.setString(1, tableName.trim());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                LOGGER.info("Masa eklendi: " + tableName);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Masa eklenirken hata: " + tableName, e);
        }

        return false;
    }

    public boolean deleteTable(int tableId) {
        final String SQL = "UPDATE tables SET is_deleted = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) return false;

            stmt.setInt(1, tableId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Masa silinirken hata: ID=" + tableId, e);
        }

        return false;
    }

    public Table getTableById(int tableId) {
        final String SQL = "SELECT * FROM tables WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return null;
            }

            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Table(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getBoolean("is_occupied")
                    );
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Masa getirilirken hata: ID=" + tableId, e);
        }

        return null;
    }

    public int getTableCount() {
        final String SQL = "SELECT COUNT(*) as total FROM tables WHERE is_deleted = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (conn != null && rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Masa sayisi hesaplanirken hata", e);
        }

        return 0;
    }
}
