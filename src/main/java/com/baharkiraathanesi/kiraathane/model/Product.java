package com.baharkiraathanesi.kiraathane.model;

// Veritabanındaki 'products' tablosunun Java karşılığı
public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private int stockQty;
    private String unit; // Porsiyon, Kg, Adet
    private int criticalLevel;

    // Yapıcı Metot (Constructor) - Nesne oluştururken kullanılır
    public Product(int id, String name, String category, double price, int stockQty, String unit, int criticalLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQty = stockQty;
        this.unit = unit;
        this.criticalLevel = criticalLevel;
    }

    // Getter Metotları (Verileri okumak için)
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStockQty() { return stockQty; }
    public String getUnit() { return unit; }
    public int getCriticalLevel() { return criticalLevel; }

    // Test ederken ürün adını yazdırmak için
    @Override
    public String toString() {
        return name + " (" + stockQty + " " + unit + ") - " + price + " TL";
    }
}