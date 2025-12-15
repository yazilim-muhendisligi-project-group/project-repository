package com.baharkiraathanesi.kiraathane.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testConstructorAndGetters() {
        Product product = new Product(1, "Çay", "İçecek", 15.0, 100, "bardak", 10, 5, 20, null);

        assertEquals("Çay", product.getName());
        assertEquals(15.0, product.getPrice());
        assertEquals(5, product.getStockPackage(), "Paket sayısı 5 olmalı");
        assertEquals(100, product.getStockQty(), "Toplam stok 100 olmalı (5 paket * 20)");

        assertTrue(product.getStockDisplay().contains("5 paket"));
        assertTrue(product.getStockDisplay().contains("100 bardak"));
    }

    @Test
    void testStockPackageUpdate() {
        Product product = new Product(1, "Kahve", "İçecek", 50.0, 0, "fincan", 5, 0, 10, null);

        product.setStockPackage(3);

        assertEquals(30, product.getStockQty(), "Paket güncellenince toplam stok (qty) otomatik hesaplanmalı");

        assertEquals("3 paket (30 fincan)", product.getStockDisplay());
    }

    @Test
    void testToString() {
        Product product = new Product(1, "Su", "İçecek", 5.0, 10, "adet", 2, 1, 10, "1 paket (10 adet)");
        String result = product.toString();

        assertTrue(result.contains("Su"));
        assertTrue(result.contains("5.0 TL"));
    }
}