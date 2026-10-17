package model.enums;

public enum PartnerType {
    GENTLE_TEACHER("温柔教师", "精打细算", "集市买入价9折，每周随机省钱"),
    AGRICULTURE_EXPERT("农业专家", "科学种植", "土地肥耗-20%，减免灾害损失，随机恢复肥力"),
    BUSINESS_ELITE("商业精英", "风险投资", "卖出价+10%，每周获取下周价格预测"),
    CHEF("厨师能手", "农产品深加工", "每周把小麦/西红柿/蛋加工成高价值产品");

    private final String name;
    private final String skill;
    private final String description;

    PartnerType(String name, String skill, String description) {
        this.name = name;
        this.skill = skill;
        this.description = description;
    }

    public String getName() { return name; }
    public String getSkill() { return skill; }
    public String getDescription() { return description; }
}
