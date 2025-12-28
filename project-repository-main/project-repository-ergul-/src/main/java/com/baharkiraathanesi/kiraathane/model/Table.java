package com.baharkiraathanesi.kiraathane.model;

public class Table {
    private int id;
    private String name;      // Örn: "Masa 1"
    private boolean isOccupied; // true: Dolu, false: Boş

    public Table(int id, String name, boolean isOccupied) {
        this.id = id;
        this.name = name;
        this.isOccupied = isOccupied;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isOccupied() { return isOccupied; }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    @Override
    public String toString() {
        return name + " [" + (isOccupied ? "DOLU" : "BOŞ") + "]";
    }
}