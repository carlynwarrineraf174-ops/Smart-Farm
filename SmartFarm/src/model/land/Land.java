package model.land;

import model.crop.Crop;
import model.enums.Season;

public abstract class Land {
    protected String typeName;
    protected int fertility;
    protected Crop plantedCrop;
    protected int fertilizerType; // 0=无, 1=化肥, 2=猪粪
    protected int pesticideTurns; // 农药剩余加速次数, 0=无

    public Land(String typeName, int fertility) {
        this.typeName = typeName;
        this.fertility = fertility;
        this.plantedCrop = null;
        this.fertilizerType = 0;
        this.pesticideTurns = 0;
    }

    public String getTypeName() { return typeName; }
    public int getFertility() { return fertility; }
    public Crop getPlantedCrop() { return plantedCrop; }
    public boolean hasFertilizer() { return fertilizerType > 0; }
    public int getFertilizerType() { return fertilizerType; }
    public boolean hasPesticide() { return pesticideTurns > 0; }
    public int getPesticideTurns() { return pesticideTurns; }
    public boolean isEmpty() { return plantedCrop == null; }

    public void setPlantedCrop(Crop crop) { this.plantedCrop = crop; }

    /** 施肥：type=1 化肥(+0.2), type=2 猪粪(+0.1) */
    public void applyFertilizer(int type) {
        this.fertilizerType = type;
        this.fertility += type == 1 ? 20 : 10;
    }

    /** 喷洒农药：有效3周，每周作物额外生长一次 */
    public void applyPesticide() {
        this.pesticideTurns = 3;
    }

    public void consumeFertilizer() {
        this.fertilizerType = 0;
    }

    public void consumePesticide() {
        this.pesticideTurns = 0;
    }

    public void reduceFertility(int amount) {
        this.fertility = Math.max(0, this.fertility - amount);
    }

    public abstract boolean isSuitableFor(Crop crop);
    public abstract double getYieldModifier(Crop crop);
    public abstract int getPurchasePrice();
    public abstract String getSuitableCropNames();

    /** 每周推进：正常生长一次；农药生效时额外生长一次，农药持续3周不受收获影响 */
    public void onWeekPass(Season season) {
        if (plantedCrop != null) {
            plantedCrop.grow(season, this);
            if (pesticideTurns > 0 && !plantedCrop.isMature()) {
                plantedCrop.grow(season, this);
            }
        }
        if (pesticideTurns > 0) {
            pesticideTurns--;
        }
    }

    @Override
    public String toString() {
        String info = typeName + " [" + getSuitableCropNames() + "]";
        if (plantedCrop == null) {
            return info + " - 空" + fertilizerSuffix();
        }
        return info + " - " + plantedCrop.getName()
                + " (" + plantedCrop.getGrownWeeks() + "/" + plantedCrop.getGrowthCycle() + "周)"
                + fertilizerSuffix();
    }

    private String fertilizerSuffix() {
        String s = "";
        if (fertilizerType == 1) s += " (有化肥)";
        else if (fertilizerType == 2) s += " (有猪粪)";
        if (pesticideTurns > 0) s += " [已打药×" + pesticideTurns + "]";
        return s;
    }
}
