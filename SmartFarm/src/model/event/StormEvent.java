package model.event;

import farm.FarmManager;
import model.enums.PartnerType;
import model.land.Land;

public class StormEvent extends GameEvent {
    public StormEvent() {
        super("暴风雨", "暴风雨来袭！所有作物减产20%！");
    }

    @Override
    public void apply(FarmManager farm) {
        int loss = 5;
        if (farm.getPartner() != null && farm.getPartner().getType() == PartnerType.AGRICULTURE_EXPERT) {
            loss = 3;
        }
        for (Land land : farm.getLands()) {
            if (!land.isEmpty()) {
                land.reduceFertility(loss);
            }
        }
    }
}
