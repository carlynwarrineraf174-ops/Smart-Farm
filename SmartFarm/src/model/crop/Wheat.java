package model.crop;

public class Wheat extends Crop {
    public Wheat() {
        super("小麦", 2, 4.0, true, false);
    }

    @Override
    public String getSuitableLandType() {
        return "黑土地";
    }

    @Override
    public int getSellPrice() {
        return 30;
    }
}
