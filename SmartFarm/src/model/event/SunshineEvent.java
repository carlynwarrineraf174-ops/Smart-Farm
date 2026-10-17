package model.event;

import farm.FarmManager;
import model.land.Land;

public class SunshineEvent extends GameEvent {
    public SunshineEvent() {
        super("阳光普照", "阳光普照！所有作物生长速度大幅提升！");
    }

    @Override
    public void apply(FarmManager farm) {
        for (Land land : farm.getLands()) {
            if (!land.isEmpty()) {
                land.getPlantedCrop().grow(farm.getCurrentSeason(), land);
                farm.addMessage("  [丰收] " + land.getTypeName() + "的作物额外成长了一周！");
            }
        }
    }
}
