package model.enums;

public enum Season {
    SPRING("春季", 1.2, 1.1),
    SUMMER("夏季", 1.3, 1.0),
    AUTUMN("秋季", 1.0, 1.0),
    WINTER("冬季", 0.3, 0.5);

    private final String displayName;
    private final double growthRate;
    private final double yieldRate;

    Season(String displayName, double growthRate, double yieldRate) {
        this.displayName = displayName;
        this.growthRate = growthRate;
        this.yieldRate = yieldRate;
    }

    public String getDisplayName() { return displayName; }
    public double getGrowthRate() { return growthRate; }
    public double getYieldRate() { return yieldRate; }

    public Season next() {
        Season[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
