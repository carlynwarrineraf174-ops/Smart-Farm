package model.livestock;

import java.util.Map;

public abstract class Livestock {
    protected String name;
    protected int purchasePrice;
    protected int maturityWeeks;
    protected int ownedWeeks;
    protected int lifespan;
    protected boolean dead;
    protected Map<String, Integer> weeklyConsumption;
    protected String productName;
    protected int productAmount;
    protected boolean sick;
    protected int timesSick;

    public Livestock(String name, int purchasePrice, int maturityWeeks, int lifespan,
                     Map<String, Integer> weeklyConsumption,
                     String productName, int productAmount) {
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.maturityWeeks = maturityWeeks;
        this.lifespan = lifespan;
        this.ownedWeeks = 0;
        this.dead = false;
        this.weeklyConsumption = weeklyConsumption;
        this.productName = productName;
        this.productAmount = productAmount;
        this.sick = false;
        this.timesSick = 0;
    }

    public String getName() { return name; }
    public int getPurchasePrice() { return purchasePrice; }
    public int getMaturityWeeks() { return maturityWeeks; }
    public int getOwnedWeeks() { return ownedWeeks; }
    public int getLifespan() { return lifespan; }
    public boolean isDead() { return dead; }
    public void setDead(boolean dead) { this.dead = dead; }
    public Map<String, Integer> getWeeklyConsumption() { return weeklyConsumption; }
    public String getProductName() { return productName; }
    public int getProductAmount() { return productAmount; }
    public boolean isMature() { return ownedWeeks >= maturityWeeks; }
    public boolean isSick() { return sick; }
    public void setSick(boolean sick) { this.sick = sick; }
    public int getTimesSick() { return timesSick; }
    public void setTimesSick(int timesSick) { this.timesSick = timesSick; }
    public void incTimesSick() { this.timesSick++; }

    public void passWeek() {
        ownedWeeks++;
        if (ownedWeeks >= lifespan) {
            dead = true;
        }
    }

    public boolean canProduce() {
        return isMature() && !sick && !dead;
    }

    public abstract int getProductSellPrice();

    /** 出售价格随年龄正态分布：幼年30%→巅峰100%→老年20% */
    public double getAgeSellMultiplier() {
        if (dead) return 0.2;
        double ratio = (double) ownedWeeks / lifespan;
        if (ratio <= 0.5) {
            return 0.3 + 0.7 * (ratio / 0.5);
        } else {
            return 1.0 - 0.8 * ((ratio - 0.5) / 0.5);
        }
    }

    public String getAgeStage() {
        if (dead) return "已死亡";
        double ratio = (double) ownedWeeks / lifespan;
        if (ratio < 0.2) return "幼年";
        if (ratio < 0.4) return "成长期";
        if (ratio < 0.6) return "巅峰期";
        if (ratio < 0.8) return "成熟期";
        return "老年";
    }

    @Override
    public String toString() {
        if (dead) return name + " [已死亡]";
        int remaining = lifespan - ownedWeeks;
        String status = sick ? " [生病]" : (isMature() ? " [已成熟]" : " [成长期]");
        String sickInfo = timesSick > 0 ? " 生病" + timesSick + "/3" : "";
        return name + status + sickInfo + " (已养" + ownedWeeks + "周, 剩余" + remaining + "周)";
    }
}
