package model.life;

import model.enums.PartnerType;

public class Partner {
    private PartnerType type;
    private String name;
    private String skillName;
    private String skillDescription;

    public Partner(PartnerType type) {
        this.type = type;
        this.name = type.getName();
        this.skillName = type.getSkill();
        this.skillDescription = type.getDescription();
    }

    public PartnerType getType() { return type; }
    public String getName() { return name; }
    public String getSkillName() { return skillName; }
    public String getSkillDescription() { return skillDescription; }

    @Override
    public String toString() {
        return name + " (" + skillName + ": " + skillDescription + ")";
    }
}
