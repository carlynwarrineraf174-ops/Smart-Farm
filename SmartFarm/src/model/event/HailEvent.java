package model.event;

import farm.FarmManager;
import model.crop.Crop;
import model.enums.PartnerType;
import model.land.Land;

public class HailEvent extends GameEvent {
    public HailEvent() {
        super("冰雹", "冰雹来袭！部分作物可能被摧毁！");
    }

    @Override
    public void apply(FarmManager farm) {
        double destroyChance = 0.4;
        if (farm.getPartner() != null && farm.getPartner().getType() == PartnerType.AGRICULTURE_EXPERT) {
            destroyChance = 0.25;
        }
        for (Land land : farm.getLands()) {
            if (!land.isEmpty()) {
                Crop crop = land.getPlantedCrop();
                if (Math.random() < destroyChance) {
                    land.setPlantedCrop(null);
                    farm.addMessage("  [损失] " + land.getTypeName() + "上的" + crop.getName() + "被冰雹摧毁了！");
                }
            }
        }
    }
}
