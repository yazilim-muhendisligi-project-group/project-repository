// erciyes.edu.tr.bahar19.Model.Order.java
package erciyes.edu.tr.bahar19.Model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int tableNumber;
    private double totalAmount; // <-- DEĞİŞİKLİK: int yerine double

    // YENİ ALAN: Sipariş kalemlerini tutacak liste
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    // --- YARDIMCI METOTLAR (Normalde Controller'da olur, View için şimdilik burada kalsın) ---
    public void addItem(OrderItem item) {
        this.items.add(item);
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        this.totalAmount = this.items.stream()
                .mapToDouble(OrderItem::getTotal)
                .sum();
    }
    // -------------------------

    // Getter ve Setter'lar
    public int getId() { return id; }
    public int getTableNumber() { return tableNumber; }

    public double getTotalAmount() { return totalAmount; } // double döndürür
    public List<OrderItem> getItems() { return items; }

    public void setId(int id) { this.id = id; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; } // double kabul eder
    public void setItems(List<OrderItem> items) { this.items = items; }
}