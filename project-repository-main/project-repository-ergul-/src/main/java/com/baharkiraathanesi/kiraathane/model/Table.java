package com.baharkiraathanesi.kiraathane.model;

import java.sql.Timestamp;

public class Table {
    private int id;
    private String name;
    private boolean isOccupied;

    // YENİ EKLENENLER
    private Timestamp openTime;      // Masa ne zaman açıldı?
    private Timestamp lastActionTime;// En son ne zaman sipariş girildi?

    public Table(int id, String name, boolean isOccupied) {
        this.id = id;
        this.name = name;
        this.isOccupied = isOccupied;
    }

    // Getter ve Setter'lar
    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isOccupied() { return isOccupied; }

    public Timestamp getOpenTime() { return openTime; }
    public void setOpenTime(Timestamp openTime) { this.openTime = openTime; }

    public Timestamp getLastActionTime() { return lastActionTime; }
    public void setLastActionTime(Timestamp lastActionTime) { this.lastActionTime = lastActionTime; }

    @Override
    public String toString() {
        return name + (isOccupied ? " (DOLU)" : " (BOŞ)");
    }
}