package model.land;

import model.crop.Corn;
import model.crop.Crop;

public class SandyLand extends Land {
    public SandyLand() {
        super("沙地", 60);
    }

    @Override
    public boolean isSuitableFor(Crop crop) {
        return crop instanceof Corn;
    }

    @Override
    public double getYieldModifier(Crop crop) {
        return isSuitableFor(crop) ? 1.3 : 0.8;
    }

    @Override
    public int getPurchasePrice() { return 900; }

    @Override
    public String getSuitableCropNames() { return "适合: 玉米"; }
}
