package com.baharkiraathanesi.kiraathane.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    void testTableStatus() {
        Table table = new Table(1, "Masa 1", false);

        assertEquals("Masa 1", table.getName());
        assertFalse(table.isOccupied(), "Masa başlangıçta boş olmalı");

        table.setOccupied(true);
        assertTrue(table.isOccupied(), "Durum güncellendiğinde dolu olmalı");
    }

    @Test
    void testToString() {
        Table emptyTable = new Table(2, "Masa 2", false);
        Table occupiedTable = new Table(3, "Masa 3", true);

        assertTrue(emptyTable.toString().contains("BOŞ"));
        assertTrue(occupiedTable.toString().contains("DOLU"));
    }
}