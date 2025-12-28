package com.baharkiraathanesi.kiraathane.model;

public class Customer {
    private int id;
    private String fullName;
    private String phone;
    private double balance;

    public Customer(int id, String fullName, String phone, double balance) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.balance = balance;
    }

    // Getter ve Setterlar
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public double getBalance() { return balance; }

    // toString metodunu ComboBox'ta isim gözüksün diye ekliyoruz
    @Override
    public String toString() {
        return fullName + " (" + String.format("%.2f", balance) + " TL)";
    }
}