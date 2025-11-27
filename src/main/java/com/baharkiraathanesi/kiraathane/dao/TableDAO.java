package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Table;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Table Data Access Object
 * Masa işlemleri için veritabanı erişim katmanı
 */
public class TableDAO {

    private static final Logger LOGGER = Logger.getLogger(TableDAO.class.getName());

    /**
     * Tüm masaları getirir
     *
     * @return Masa listesi, hata durumunda boş liste
     */
    public List<Table> getAllTables() {
        List<Table> tableList = new ArrayList<>();
        final String SQL = "SELECT * FROM tables ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            if (conn == null) {
                LOGGER.info("⚠️ TableDAO: Veritabanı bağlantısı kurulamadı!");
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

            LOGGER.info("✅ " + tableList.size() + " masa getirildi");

        } catch (SQLException e) {
            LOGGER.info("⚠️ Masalar getirilirken hata oluştu: " + e.getMessage());
        }

        return tableList;
    }

    /**
     * Masa durumunu günceller (dolu/boş)
     *
     * @param tableId Masa ID
     * @param isOccupied true=dolu, false=boş
     * @return Başarılıysa true
     */
    public boolean updateTableStatus(int tableId, boolean isOccupied) {
        final String SQL = "UPDATE tables SET is_occupied = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return false;
            }

            stmt.setBoolean(1, isOccupied);
            stmt.setInt(2, tableId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                String status = isOccupied ? "DOLU" : "BOŞ";
                LOGGER.info("✅ Masa durumu güncellendi: ID=" + tableId + " -> " + status);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.info("⚠️ Masa durumu güncellenirken hata: ID=" + tableId + " - " + e.getMessage());
        }

        return false;
    }

    /**
     * Yeni masa ekler
     *
     * @param tableName Masa adı
     * @return Başarılıysa true
     */
    public boolean addTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            LOGGER.info("⚠️ Masa adı boş olamaz!");
            return false;
        }

        final String SQL = "INSERT INTO tables (name, is_occupied) VALUES (?, FALSE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return false;
            }

            stmt.setString(1, tableName.trim());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                LOGGER.info("✅ Masa eklendi: " + tableName);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.info("⚠️ Masa eklenirken hata: " + tableName + " - " + e.getMessage());
        }

        return false;
    }

    /**
     * Masa siler
     *
     * @param tableId Silinecek masa ID
     * @return Başarılıysa true
     */
    public boolean deleteTable(int tableId) {
        final String SQL = "DELETE FROM tables WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return false;
            }

            stmt.setInt(1, tableId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                LOGGER.info("✅ Masa silindi: ID=" + tableId);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.info("⚠️ Masa silinirken hata: ID=" + tableId + " - " + e.getMessage());
        }

        return false;
    }

    /**
     * ID'ye göre masa getirir
     *
     * @param tableId Masa ID
     * @return Masa nesnesi, bulunamazsa null
     */
    public Table getTableById(int tableId) {
        final String SQL = "SELECT * FROM tables WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            if (conn == null) {
                return null;
            }

            stmt.setInt(1, tableId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Table(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getBoolean("is_occupied")
                );
            }

        } catch (SQLException e) {
            LOGGER.info("⚠️ Masa getirilirken hata: ID=" + tableId + " - " + e.getMessage());
        }

        return null;
    }

    /**
     * Toplam masa sayısını getirir
     *
     * @return Masa sayısı
     */
    public int getTableCount() {
        final String SQL = "SELECT COUNT(*) as total FROM tables";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            if (conn != null && rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            LOGGER.info("⚠️ Masa sayısı hesaplanırken hata: " + e.getMessage());
        }

        return 0;
    }
}

