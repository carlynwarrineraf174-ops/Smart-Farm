package model.life;

public class Vehicle {
    private int level;
    private String name;
    private int price;
    private double sellBonus;
    private String specialEffect;

    public Vehicle(int level, String name, int price, double sellBonus, String specialEffect) {
        this.level = level;
        this.name = name;
        this.price = price;
        this.sellBonus = sellBonus;
        this.specialEffect = specialEffect;
    }

    public int getLevel() { return level; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public double getSellBonus() { return sellBonus; }
    public String getSpecialEffect() { return specialEffect; }

    public static Vehicle getDefault() {
        return new Vehicle(1, "破旧自行车", 0, 1.0, "正常售价");
    }

    public static Vehicle nextLevel(Vehicle current) {
        switch (current.level) {
            case 1: return new Vehicle(2, "二手皮卡", 5000, 1.10, "售价+10%");
            case 2: return new Vehicle(3, "高端越野", 20000, 1.20, "售价+20%");
            case 3: return new Vehicle(4, "进口豪车", 60000, 1.35, "售价+35%");
            default: return null;
        }
    }
}
