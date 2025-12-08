package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Report;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportDAO.class.getName());

    public double getDailyRevenue() {
        double totalRevenue = 0.0;
        final String SQL = "SELECT SUM(total_amount) FROM orders WHERE is_paid = TRUE AND DATE(created_at) = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (conn == null) {
                LOGGER.warning("Veritabani baglantisi kurulamadi");
                return totalRevenue;
            }

            if (rs.next()) {
                totalRevenue = rs.getDouble(1);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gunluk ciro alinirken hata", e);
        }
        return totalRevenue;
    }

    public List<Report> getWeeklyDailyReports() {
        List<Report> reports = new ArrayList<>();
        final String SQL = "SELECT DATE(created_at) AS report_date, SUM(total_amount) AS total_revenue, COUNT(*) AS total_orders " +
                "FROM orders WHERE is_paid = TRUE AND created_at >= CURDATE() - INTERVAL 7 DAY " +
                "GROUP BY DATE(created_at) ORDER BY report_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("report_date").toLocalDate();
                double totalRevenue = rs.getDouble("total_revenue");
                int totalOrders = rs.getInt("total_orders");
                reports.add(new Report(date, totalRevenue, totalOrders));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Haftalik raporlar alinirken hata", e);
        }
        return reports;
    }

    public List<Report> getMonthlyReports() {
        List<Report> reports = new ArrayList<>();
        final String SQL = "SELECT DATE_FORMAT(created_at, '%Y-%m-01') AS report_date, SUM(total_amount) AS total_revenue, COUNT(*) AS total_orders " +
                "FROM orders WHERE is_paid = TRUE " +
                "GROUP BY DATE_FORMAT(created_at, '%Y-%m-01') " +
                "ORDER BY report_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("report_date").toLocalDate();
                double totalRevenue = rs.getDouble("total_revenue");
                int totalOrders = rs.getInt("total_orders");
                reports.add(new Report(date, totalRevenue, totalOrders));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Aylik raporlar alinirken hata", e);
        }
        return reports;
    }

    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        final String SQL = "SELECT DATE(created_at) AS report_date, SUM(total_amount) AS total_revenue, COUNT(*) AS total_orders " +
                     "FROM orders WHERE is_paid = TRUE GROUP BY DATE(created_at) ORDER BY report_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (conn == null) {
                LOGGER.warning("Veritabani baglantisi kurulamadi");
                return reports;
            }

            while (rs.next()) {
                LocalDate date = rs.getDate("report_date").toLocalDate();
                double totalRevenue = rs.getDouble("total_revenue");
                int totalOrders = rs.getInt("total_orders");
                reports.add(new Report(date, totalRevenue, totalOrders));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Tum raporlar alinirken hata", e);
        }

        return reports;
    }

    public boolean resetDailyData() {
        final String DELETE_ITEMS_SQL = "DELETE oi FROM order_items oi " +
                                       "JOIN orders o ON oi.order_id = o.id " +
                                       "WHERE o.is_paid = TRUE AND DATE(o.created_at) = CURDATE()";
        final String DELETE_ORDERS_SQL = "DELETE FROM orders " +
                                        "WHERE is_paid = TRUE AND DATE(created_at) = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                LOGGER.warning("Veritabani baglantisi kurulamadi");
                return false;
            }

            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(DELETE_ITEMS_SQL);
                 PreparedStatement stmt2 = conn.prepareStatement(DELETE_ORDERS_SQL)) {

                stmt1.executeUpdate();
                stmt2.executeUpdate();

                conn.commit();
                LOGGER.info("Bugunku veriler basariyla sifirlandi");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Veri sifirlama hatasi, rollback yapildi", e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabani baglanti hatasi", e);
            return false;
        }
    }
}

