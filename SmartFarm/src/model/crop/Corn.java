package model.crop;

public class Corn extends Crop {
    public Corn() {
        super("玉米", 3, 6.0, true, false);
    }

    @Override
    public String getSuitableLandType() {
        return "沙地";
    }

    @Override
    public int getSellPrice() {
        return 25;
    }
}
