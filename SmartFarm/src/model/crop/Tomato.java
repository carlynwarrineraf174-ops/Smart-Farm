package model.crop;

public class Tomato extends Crop {
    public Tomato() {
        super("西红柿", 4, 8.0, false, true);
    }

    @Override
    public String getSuitableLandType() {
        return "红壤";
    }

    @Override
    public int getSellPrice() {
        return 45;
    }
}
