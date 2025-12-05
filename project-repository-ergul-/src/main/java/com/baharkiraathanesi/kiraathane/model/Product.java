package com.baharkiraathanesi.kiraathane.model;

// Veritabanındaki 'products' tablosunun Java karşılığı
public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private int stockQty;           // Toplam porsiyon sayısı
    private String unit;            // Porsiyon, Kg, Adet, bardak, fincan
    private int criticalLevel;
    private int stockPackage;       // Paket sayısı
    private int portionsPerPackage; // Paket başına porsiyon sayısı
    private String stockDisplay;    // Gösterim: "5 paket (1000 bardak)"

    // Yapıcı Metot (Constructor) - Eski sistem için (geriye dönük uyumluluk)
    public Product(int id, String name, String category, double price, int stockQty, String unit, int criticalLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQty = stockQty;
        this.unit = unit;
        this.criticalLevel = criticalLevel;
        this.stockPackage = 0;
        this.portionsPerPackage = 1;
        this.stockDisplay = stockQty + " " + unit;
    }

    // Yeni Yapıcı Metot - Paket/Porsiyon sistemi ile
    public Product(int id, String name, String category, double price, int stockQty, String unit,
                   int criticalLevel, int stockPackage, int portionsPerPackage, String stockDisplay) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQty = stockQty;
        this.unit = unit;
        this.criticalLevel = criticalLevel;
        this.stockPackage = stockPackage;
        this.portionsPerPackage = portionsPerPackage;
        this.stockDisplay = stockDisplay != null ? stockDisplay : stockPackage + " paket (" + stockQty + " " + unit + ")";
    }

    // Getter Metotları (Verileri okumak için)
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStockQty() { return stockQty; }
    public String getUnit() { return unit; }
    public int getCriticalLevel() { return criticalLevel; }
    public int getStockPackage() { return stockPackage; }
    public int getPortionsPerPackage() { return portionsPerPackage; }
    public String getStockDisplay() { return stockDisplay; }

    // Setter Metotları (Stok güncellemesi için)
    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
        updateStockDisplay();
    }

    public void setStockPackage(int stockPackage) {
        this.stockPackage = stockPackage;
        this.stockQty = stockPackage * portionsPerPackage;
        updateStockDisplay();
    }

    // Stok gösterim metnini güncelle
    private void updateStockDisplay() {
        this.stockDisplay = stockPackage + " paket (" + stockQty + " " + unit + ")";
    }

    @Override
    public String toString() {
        return name + " - " + stockDisplay + " - " + price + " TL";
    }
}