package model.event;

import farm.FarmManager;
import model.enums.PartnerType;
import model.livestock.Livestock;

import java.util.ArrayList;
import java.util.List;

public class DiseaseEvent extends GameEvent {
    private static final int SICK_LIMIT = 3;

    public DiseaseEvent() {
        super("牲畜疫病", "疫病传播！牲畜容易反复生病，生病" + SICK_LIMIT + "次会死亡！");
    }

    @Override
    public void apply(FarmManager farm) {
        double sickChance = 0.5;
        if (farm.getPartner() != null && farm.getPartner().getType() == PartnerType.AGRICULTURE_EXPERT) {
            sickChance = 0.35;
        }
        List<Livestock> allAnimals = new ArrayList<>();
        allAnimals.addAll(farm.getLivestockList());
        allAnimals.addAll(farm.getNewborns());

        int count = 0;
        for (Livestock animal : allAnimals) {
            if (Math.random() < sickChance) {
                animal.setSick(true);
                animal.incTimesSick();
                count++;
                if (animal.getTimesSick() >= SICK_LIMIT) {
                    animal.setDead(true);
                    farm.addMessage("  [死亡] " + animal.getName() + "因反复生病" + SICK_LIMIT + "次而死亡！");
                }
            }
        }
        if (count > 0) {
            farm.addMessage("  [疫病] " + count + "头牲畜生病了（已生病次数累计，满" + SICK_LIMIT + "次死亡），请注意治疗。");
        }
    }
}
