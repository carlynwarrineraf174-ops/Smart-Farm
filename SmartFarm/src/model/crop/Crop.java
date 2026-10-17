package model.crop;

import model.enums.Season;
import model.land.Land;

public abstract class Crop {
    protected String name;
    protected int growthCycle;
    protected int grownWeeks;
    protected boolean mature;
    protected double baseYield;
    protected boolean droughtTolerant;
    protected boolean floodTolerant;

    public Crop(String name, int growthCycle, double baseYield, boolean droughtTolerant, boolean floodTolerant) {
        this.name = name;
        this.growthCycle = growthCycle;
        this.grownWeeks = 0;
        this.mature = false;
        this.baseYield = baseYield;
        this.droughtTolerant = droughtTolerant;
        this.floodTolerant = floodTolerant;
    }

    public String getName() { return name; }
    public int getGrowthCycle() { return growthCycle; }
    public int getGrownWeeks() { return grownWeeks; }
    public boolean isMature() { return mature; }
    public double getBaseYield() { return baseYield; }
    public boolean isDroughtTolerant() { return droughtTolerant; }
    public boolean isFloodTolerant() { return floodTolerant; }

    public abstract String getSuitableLandType();
    public abstract int getSellPrice();

    public void grow(Season season, Land land) {
        if (mature) return;
        double rate = season.getGrowthRate();
        if (land != null && land.isSuitableFor(this)) {
            rate *= 1.3;
        }
        if (land != null && land.hasFertilizer()) {
            rate *= 1.5;
        }
        grownWeeks += rate;
        if (grownWeeks >= growthCycle) {
            mature = true;
            grownWeeks = growthCycle;
        }
    }

    public double getYieldAmount() {
        return mature ? baseYield : 0;
    }

    @Override
    public String toString() {
        return name + " (" + grownWeeks + "/" + growthCycle + "周)" + (mature ? " [已成熟]" : "");
    }
}
