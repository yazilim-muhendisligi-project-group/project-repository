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

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                totalRevenue = rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalRevenue;
    }

    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT DATE(created_at) AS report_date, SUM(total_amount) AS total_revenue, COUNT(*) AS total_orders " +
                     "FROM orders WHERE is_paid = TRUE GROUP BY DATE(created_at) ORDER BY report_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LocalDate date = rs.getDate("report_date").toLocalDate();
                double totalRevenue = rs.getDouble("total_revenue");
                int totalOrders = rs.getInt("total_orders");

                reports.add(new Report(date, totalRevenue, totalOrders));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }
}