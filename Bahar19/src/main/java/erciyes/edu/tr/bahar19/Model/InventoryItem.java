package erciyes.edu.tr.bahar19.Model;

public class InventoryItem {
    private int id;
    private String name;        // Örn: Paket Çay, Küp Şeker, Kahve Çekirdeği
    private int currentStock;   // Mevcut stok miktarı (kg, adet, litre)
    private String unit;        // Örn: KG, Adet, Kutu
    private int criticalLevel;  // Kritik stok seviyesi

    // Constructor, Getter ve Setter'lar (Basitçe)

    public InventoryItem(int id, String name, int currentStock, String unit, int criticalLevel) {
        this.id = id;
        this.name = name;
        this.currentStock = currentStock;
        this.unit = unit;
        this.criticalLevel = criticalLevel;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCurrentStock() { return currentStock; }
    public String getUnit() { return unit; }
    public int getCriticalLevel() { return criticalLevel; }

    // Stok düşürme/artırma işlemleri için
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    // Diğer setter'lar...
}