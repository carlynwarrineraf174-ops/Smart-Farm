package model.life;

public class House {
    private int level;
    private String name;
    private int price;
    private int storageBonus;
    private String specialEffect;

    public House(int level, String name, int price, int storageBonus, String specialEffect) {
        this.level = level;
        this.name = name;
        this.price = price;
        this.storageBonus = storageBonus;
        this.specialEffect = specialEffect;
    }

    public int getLevel() { return level; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStorageBonus() { return storageBonus; }
    public String getSpecialEffect() { return specialEffect; }

    public static House getDefault() {
        return new House(1, "破旧木屋", 0, 100, "基础仓库容量100");
    }

    public static House nextLevel(House current) {
        switch (current.level) {
            case 1: return new House(2, "红砖瓦房", 2000, 200, "仓库容量+100");
            case 2: return new House(3, "郊区小楼", 10000, 400, "仓库容量+200");
            case 3: return new House(4, "豪华庄园", 50000, 800, "仓库容量+400，求婚前提");
            default: return null;
        }
    }
}
