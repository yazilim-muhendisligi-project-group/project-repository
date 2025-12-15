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
class ReportDAOTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockStatement;
    @Mock private ResultSet mockResultSet;

    @Test
    void testGetDailyRevenue() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getDouble(1)).thenReturn(500.0);

            ReportDAO reportDAO = new ReportDAO();
            double revenue = reportDAO.getDailyRevenue();

            assertEquals(500.0, revenue);
        }
    }

    @Test
    void testResetDailyData_Transaction() throws SQLException {

        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1);

            ReportDAO reportDAO = new ReportDAO();
            boolean result = reportDAO.resetDailyData();

            assertTrue(result, "Sıfırlama işlemi başarılı olmalı");

            verify(mockConnection).setAutoCommit(false);

            verify(mockConnection).commit();

            verify(mockConnection).setAutoCommit(true);
        }
    }
}