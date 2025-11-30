package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Report;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // Bugünün Toplam Cirosunu Getir (Z Raporu)
    public double getDailyRevenue() {
        double totalRevenue = 0.0;
        // Sadece BUGÜNÜN ve ÖDENMİŞ siparişlerinin toplamını al
        String sql = "SELECT SUM(total_amount) FROM orders WHERE is_paid = TRUE AND DATE(created_at) = CURDATE()";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.out.println("ReportDAO: Veritabanı bağlantısı kurulamadı!");
                return totalRevenue;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                totalRevenue = rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return totalRevenue;
    }

    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT DATE(created_at) AS report_date, SUM(total_amount) AS total_revenue, COUNT(*) AS total_orders " +
                     "FROM orders WHERE is_paid = TRUE GROUP BY DATE(created_at) ORDER BY report_date DESC";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.out.println("ReportDAO: Veritabanı bağlantısı kurulamadı!");
                return reports;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                LocalDate date = rs.getDate("report_date").toLocalDate();
                double totalRevenue = rs.getDouble("total_revenue");
                int totalOrders = rs.getInt("total_orders");

                reports.add(new Report(date, totalRevenue, totalOrders));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return reports;
    }

    // Z Raporu Sonrası Günlük Verileri Sıfırla
    public boolean resetDailyData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Transaction başlat
            conn.setAutoCommit(false);

            try {
                // 1. Bugünün order_items kayıtlarını sil
                String deleteItemsSql = "DELETE oi FROM order_items oi " +
                                       "JOIN orders o ON oi.order_id = o.id " +
                                       "WHERE o.is_paid = TRUE AND DATE(o.created_at) = CURDATE()";
                Statement stmt1 = conn.createStatement();
                stmt1.executeUpdate(deleteItemsSql);

                // 2. Bugünün orders kayıtlarını sil
                String deleteOrdersSql = "DELETE FROM orders " +
                                        "WHERE is_paid = TRUE AND DATE(created_at) = CURDATE()";
                Statement stmt2 = conn.createStatement();
                stmt2.executeUpdate(deleteOrdersSql);

                // Transaction'ı onayla
                conn.commit();
                System.out.println("Bugünkü veriler başarıyla sıfırlandı!");
                return true;

            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                System.out.println("Veri sıfırlama hatası: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database bağlantı hatası: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}