package erciyes.edu.tr.bahar19.Model;

public class Recipe {
    private InventoryItem ingredient; // Hangi hammaddeden (Stoktan)
    private double consumptionQuantity; // Ne kadar tüketilecek (Örn: 0.005 kg çay)

    public Recipe(InventoryItem ingredient, double consumptionQuantity) {
        this.ingredient = ingredient;
        this.consumptionQuantity = consumptionQuantity;
    }

    public InventoryItem getIngredient() { return ingredient; }
    public double getConsumptionQuantity() { return consumptionQuantity; }
}