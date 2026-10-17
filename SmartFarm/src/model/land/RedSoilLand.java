package model.land;

import model.crop.Crop;
import model.crop.Tomato;

public class RedSoilLand extends Land {
    public RedSoilLand() {
        super("红壤", 70);
    }

    @Override
    public boolean isSuitableFor(Crop crop) {
        return crop instanceof Tomato;
    }

    @Override
    public double getYieldModifier(Crop crop) {
        return isSuitableFor(crop) ? 1.3 : 0.8;
    }

    @Override
    public int getPurchasePrice() { return 1200; }

    @Override
    public String getSuitableCropNames() { return "适合: 西红柿"; }
}
