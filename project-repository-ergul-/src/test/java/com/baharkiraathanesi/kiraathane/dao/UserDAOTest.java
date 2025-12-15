package com.baharkiraathanesi.kiraathane.dao;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    @Test
    void testAuthenticateSuccess() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {

            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            when(mockStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);

            when(mockResultSet.getString("role")).thenReturn("admin");

            UserDAO userDAO = new UserDAO();
            boolean result = userDAO.authenticate("admin", "1234");

            assertTrue(result, "Kullanıcı veritabanında varsa sonuç true dönmeli");

            verify(mockConnection).prepareStatement(anyString());
        }
    }

    @Test
    void testAuthenticateFailure() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedDB = Mockito.mockStatic(DatabaseConnection.class)) {

            mockedDB.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(false);

            UserDAO userDAO = new UserDAO();
            boolean result = userDAO.authenticate("kotu_niyetli", "yanlis_sifre");

            assertFalse(result, "Kullanıcı yoksa sonuç false dönmeli");
        }
    }

    @Test
    void testAuthenticateEmptyInput() {

        UserDAO userDAO = new UserDAO();
        assertFalse(userDAO.authenticate("", "1234"), "Boş kullanıcı adı kabul edilmemeli");
        assertFalse(userDAO.authenticate(null, "1234"), "Null kullanıcı adı kabul edilmemeli");
    }
}