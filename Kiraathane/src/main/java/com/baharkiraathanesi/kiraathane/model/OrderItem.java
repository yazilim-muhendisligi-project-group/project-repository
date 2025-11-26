package com.baharkiraathanesi.kiraathane.model;

public class OrderItem {
    private int id;
    private String productName; // Tabloda ID var ama ekranda isim göstereceğiz
    private int quantity;
    private double price;

    public OrderItem(int id, String productName, int quantity, double price) {
        this.id = id;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public String toString() {
        return productName + " x" + quantity + " (" + (price * quantity) + " TL)";
    }
}