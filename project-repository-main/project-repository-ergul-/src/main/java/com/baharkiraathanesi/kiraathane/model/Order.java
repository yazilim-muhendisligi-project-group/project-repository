package com.baharkiraathanesi.kiraathane.model;

import java.sql.Timestamp;

public class Order {
    private int id;
    private int tableId;
    private String tableName;
    private double totalAmount;
    private boolean isPaid;
    private Timestamp createdAt;
    private String orderTime;
    private String paymentType;

    // Boş constructor
    public Order() {
    }

    public Order(int id, int tableId, double totalAmount, boolean isPaid, Timestamp createdAt) {
        this.id = id;
        this.tableId = tableId;
        this.totalAmount = totalAmount;
        this.isPaid = isPaid;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getTotal() {
        return totalAmount;
    }

    public void setTotal(double total) {
        this.totalAmount = total;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}