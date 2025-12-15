package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import com.baharkiraathanesi.kiraathane.model.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableDAOTest {

    @Mock private Connection mockConnection;
    @Mock private Statement mockStatement; // Düz Statement
    @Mock private PreparedStatement mockPreparedStatement; // Parametreli sorgular için
    @Mock private ResultSet mockResultSet;

    @Test
    void testGetAllTables() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, false); // 1 masa var
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("name")).thenReturn("Masa 1");
            when(mockResultSet.getBoolean("is_occupied")).thenReturn(false);

            TableDAO tableDAO = new TableDAO();
            List<Table> tables = tableDAO.getAllTables();

            assertEquals(1, tables.size());
            assertEquals("Masa 1", tables.get(0).getName());
            assertFalse(tables.get(0).isOccupied());
        }
    }

    @Test
    void testAddTable() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            TableDAO tableDAO = new TableDAO();
            boolean result = tableDAO.addTable("Bahçe Masa 1");

            assertTrue(result);
            verify(mockPreparedStatement).setString(1, "Bahçe Masa 1");
        }
    }
}