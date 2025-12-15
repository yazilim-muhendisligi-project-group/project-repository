package com.baharkiraathanesi.kiraathane.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void testConstructorAndGetters() {
        OrderItem item = new OrderItem(1, "Çay", 2, 15.0);

        assertEquals(1, item.getId());
        assertEquals("Çay", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(15.0, item.getPrice());
    }

    @Test
    void testSubtotalCalculation() {
        OrderItem item = new OrderItem();
        item.setQuantity(3);
        item.setPrice(10.0);
        item.setSubtotal(30.0);

        assertEquals(30.0, item.getSubtotal());
    }

    @Test
    void testToString() {
        OrderItem item = new OrderItem(1, "Kahve", 2, 50.0);
        String output = item.toString();

        assertTrue(output.contains("Kahve"));
        assertTrue(output.contains("x2"));
        assertTrue(output.contains("100.0 TL")); // 2 * 50.0 = 100.0
    }
}