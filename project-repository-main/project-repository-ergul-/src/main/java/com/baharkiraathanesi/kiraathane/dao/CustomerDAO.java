package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerDAO {
    private static final Logger LOGGER = Logger.getLogger(CustomerDAO.class.getName());

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getDouble("balance")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Musteriler getirilemedi", e);
        }
        return list;
    }

    public boolean addCustomer(String fullName, String phone) {
        String sql = "INSERT INTO customers (full_name, phone, balance) VALUES (?, ?, 0.0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Musteri eklenemedi", e);
            return false;
        }
    }

    // Borç Tahsil Etme (Bakiye Düşme)
    public boolean makePayment(int customerId, double amount) {
        String sql = "UPDATE customers SET balance = balance - ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Odeme alinamadi", e);
            return false;
        }
    }

    // Siparişi Veresiye Yazma (Bakiyeyi Artırma)
    public boolean addDebtToCustomer(int customerId, double amount) {
        String sql = "UPDATE customers SET balance = balance + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Borc eklenemedi", e);
            return false;
        }
    }

    // Müşteri Bilgilerini Güncelle
    public boolean updateCustomer(int id, String fullName, String phone) {
        final String SQL = "UPDATE customers SET full_name = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            stmt.setInt(3, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Musteri guncellenemedi", e);
            return false;
        }
    }

    // Müşteriyi Sil
    public boolean deleteCustomer(int id) {
        final String SQL = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Musteri silinemedi", e);
            return false;
        }
    }
}