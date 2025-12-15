package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDAOTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockStatement;
    @Mock private ResultSet mockResultSet;

    @Test
    void testGetAllProducts() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, true, false);


            when(mockResultSet.getInt("id")).thenReturn(1, 2);
            when(mockResultSet.getString("name")).thenReturn("Çay", "Kahve");
            when(mockResultSet.getString("category")).thenReturn("İçecek", "İçecek"); // Eksikti
            when(mockResultSet.getDouble("price")).thenReturn(15.0, 35.0);
            when(mockResultSet.getInt("stock_qty")).thenReturn(100, 50); // Eksikti
            when(mockResultSet.getString("unit")).thenReturn("bardak", "fincan"); // Eksikti
            when(mockResultSet.getInt("critical_level")).thenReturn(10, 5); // Eksikti
            when(mockResultSet.getInt("stock_package")).thenReturn(5, 2); // Eksikti
            when(mockResultSet.getInt("portions_per_package")).thenReturn(20, 25); // Eksikti
            when(mockResultSet.getString("stock_display")).thenReturn("5 paket", "2 paket");
            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.getAllProducts();

            assertNotNull(products);
            assertEquals(2, products.size(), "Listede 2 ürün olmalı");
            assertEquals("Çay", products.get(0).getName());
            assertEquals("Kahve", products.get(1).getName());
            assertEquals("İçecek", products.get(0).getCategory()); // Kategori kontrolü de ekledik
        }
    }

    @Test
    void testAddProductSuccess() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            when(mockStatement.executeUpdate()).thenReturn(1);

            ProductDAO productDAO = new ProductDAO();
            boolean result = productDAO.addProduct("Oralet", "İçecek", 20.0, 5, "bardak", 50);

            assertTrue(result, "Ürün başarıyla eklenmeli");
            verify(mockStatement).setString(1, "Oralet"); // İlk parametre "Oralet" mi diye kontrol et
        }
    }

    @Test
    void testDeleteProduct() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1); // Silme başarılı

            ProductDAO productDAO = new ProductDAO();
            boolean result = productDAO.deleteProduct(10); // ID 10'u sil

            assertTrue(result);
            verify(mockStatement).setInt(1, 10); // ID parametresi 10 mu ayarlanmış?
        }
    }
}
