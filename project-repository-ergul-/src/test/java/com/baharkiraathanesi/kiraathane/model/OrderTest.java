package com.baharkiraathanesi.kiraathane.model;

import org.junit.jupiter.api.Test;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void testOrderCreation() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Order order = new Order(100, 5, 250.0, false, now);

        assertEquals(100, order.getId());
        assertEquals(5, order.getTableId());
        assertEquals(250.0, order.getTotalAmount());
        assertFalse(order.isPaid());
        assertEquals(now, order.getCreatedAt());
    }

    @Test
    void testSetters() {
        Order order = new Order();
        order.setTableName("Masa 5");
        order.setOrderTime("14:30");
        order.setPaid(true);

        assertEquals("Masa 5", order.getTableName());
        assertEquals("14:30", order.getOrderTime());
        assertTrue(order.isPaid());
    }
}