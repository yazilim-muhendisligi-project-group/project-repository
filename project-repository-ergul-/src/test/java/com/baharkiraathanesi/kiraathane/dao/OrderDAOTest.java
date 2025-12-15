package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDAOTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockStatement;
    @Mock private ResultSet mockResultSet;

    @Test
    void testAddProductToOrder_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockStatement.executeUpdate()).thenReturn(1);

            when(mockResultSet.next()).thenReturn(true, true, true, false);

            when(mockResultSet.getInt("id")).thenReturn(1); // Order ID
            when(mockResultSet.getInt("stock_qty")).thenReturn(100); // Stok Adedi
            when(mockResultSet.getDouble("price")).thenReturn(15.0); // Fiyat

            OrderDAO orderDAO = new OrderDAO();
            boolean result = orderDAO.addProductToOrder(1, 5, 2);

            assertTrue(result, "Ürün başarıyla siparişe eklenmeli");

            verify(mockStatement, atLeastOnce()).executeUpdate();
        }
    }

    @Test
    void testCloseOrder() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            when(mockStatement.executeUpdate()).thenReturn(1);

            OrderDAO orderDAO = new OrderDAO();
            orderDAO.closeOrder(1); // Masa 1'in hesabını kapat

            verify(mockStatement, atLeastOnce()).executeUpdate();
        }
    }
}