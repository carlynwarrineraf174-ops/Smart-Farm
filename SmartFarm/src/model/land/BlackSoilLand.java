package model.land;

import model.crop.Crop;
import model.crop.Wheat;

public class BlackSoilLand extends Land {
    public BlackSoilLand() {
        super("黑土地", 80);
    }

    @Override
    public boolean isSuitableFor(Crop crop) {
        return crop instanceof Wheat;
    }

    @Override
    public double getYieldModifier(Crop crop) {
        return isSuitableFor(crop) ? 1.3 : 0.8;
    }

    @Override
    public int getPurchasePrice() { return 1000; }

    @Override
    public String getSuitableCropNames() { return "适合: 小麦"; }
}
