package model.product;

public class Product implements ISellable {
    private String name;
    private String category;
    private int basePrice;

    public Product(String name, String category, int basePrice) {
        this.name = name;
        this.category = category;
        this.basePrice = basePrice;
    }

    @Override
    public String getName() { return name; }

    @Override
    public int getBasePrice() { return basePrice; }

    @Override
    public String getCategory() { return category; }

    public void setBasePrice(int basePrice) { this.basePrice = basePrice; }

    @Override
    public String toString() {
        return name + "(" + basePrice + "金)";
    }
}
