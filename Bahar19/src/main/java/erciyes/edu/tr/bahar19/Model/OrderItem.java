// erciyes.edu.tr.bahar19.Model.OrderItem.java
package erciyes.edu.tr.bahar19.Model;

public class OrderItem {
    private Product product;
    private int quantity;
    private double unitPrice; // Sipariş anındaki fiyatı (double)

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    // Getter'lar
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }

    public double getTotal() {
        return quantity * unitPrice;
    }

    // Setterlar (gerekirse)
    public void setQuantity(int quantity) { this.quantity = quantity; }
}