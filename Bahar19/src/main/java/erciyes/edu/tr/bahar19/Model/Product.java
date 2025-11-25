package erciyes.edu.tr.bahar19.Model;

public class Product {
    private int id;
    private String name;
    private int stockQuantity;
    private int price;

    public int getId(){
        return id;
    }

    public String getname(){
        return name;
    }

    public int getstockQuantity(){
        return stockQuantity;
    }

    public int getPrice() {
        return price;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setname(String name) {
        this.name = name;
    }

    public void setstockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
