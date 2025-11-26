// erciyes.edu.tr.bahar19.Model.Product.java
package erciyes.edu.tr.bahar19.Model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int id;
    private String name;
    // stockQuantity kaldırıldı, Recipe listesi eklendi:
    private double price;

    private List<Recipe> recipes; // Bu ürünü yapmak için gereken malzemeler

    public Product() {
        this.recipes = new ArrayList<>();
    }

    // YENİ METOT: Tarife malzeme ekleme
    public void addRecipe(Recipe recipe) {
        this.recipes.add(recipe);
    }

    // YENİ METOT
    public List<Recipe> getRecipes() {
        return recipes;
    }

    // Orijinal metot adları korundu
    public int getId(){ return id; }
    public String getname(){ return name; }
    // getstockQuantity() metodu artık kullanılmamalıdır.
    public double getPrice() { return price; }

    public void setId(int id) { this.id = id; }
    public void setname(String name) { this.name = name; }
    // setstockQuantity() metodu kaldırıldı.
    public void setPrice(double price) { this.price = price; }

    // UYUM İÇİN: TableView'lar Product objesinden stok çekmeye çalıştığında hata vermemek için ekliyoruz
    public int getstockQuantity() { return 0; }
    public void setstockQuantity(int stockQuantity) { /* Boş Bırakıldı */ }
}