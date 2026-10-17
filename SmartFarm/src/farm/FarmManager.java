package farm;

import exception.*;
import model.crop.*;
import model.enums.PartnerType;
import model.enums.Season;
import model.event.*;
import model.land.*;
import model.life.*;
import model.livestock.*;

import java.util.*;

public class FarmManager {
    private static FarmManager instance;

    private int gold;
    private int week;
    private Season currentSeason;
    private int weeksInSeason;

    private List<Land> lands;
    private List<Livestock> livestockList;
    private Map<String, Integer> warehouse;
    private int maxStorage;

    private House house;
    private Vehicle vehicle;
    private Partner partner;

    private int loanAmount;
    private int loanInterestRate;
    private int consecutiveDisasterWeeks;
    private GameEvent lastEvent;

    private List<String> achievements;
    private Set<String> unlockedAchievements;

    private List<String> messageLog;

    private int totalCropsHarvested;
    private int peakAsset;
    private int plantCount;
    private int breedCount;
    private int totalCured;
    private int totalFertilized;
    private List<Livestock> newborns;

    private FarmManager() {
        gold = 1000;
        week = 1;
        currentSeason = Season.SPRING;
        weeksInSeason = 1;
        consecutiveDisasterWeeks = 0;

        lands = new ArrayList<>();
        lands.add(new BlackSoilLand());

        house = House.getDefault();
        vehicle = Vehicle.getDefault();

        livestockList = new ArrayList<>();
        newborns = new ArrayList<>();
        warehouse = new HashMap<>();
        maxStorage = house.getStorageBonus();
        partner = null;

        loanAmount = 0;
        loanInterestRate = 3;

        achievements = new ArrayList<>();
        unlockedAchievements = new HashSet<>();
        messageLog = new ArrayList<>();
        totalCropsHarvested = 0;
        peakAsset = 0;
        plantCount = 0;
        breedCount = 0;
        totalCured = 0;
        totalFertilized = 0;
    }

    public static FarmManager getInstance() {
        if (instance == null) {
            instance = new FarmManager();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = new FarmManager();
    }

    // --- Getters ---
    public int getGold() { return gold; }
    public int getWeek() { return week; }
    public Season getCurrentSeason() { return currentSeason; }
    public int getWeeksInSeason() { return weeksInSeason; }
    public List<Land> getLands() { return lands; }
    public List<Livestock> getLivestockList() { return livestockList; }
    public List<Livestock> getNewborns() { return newborns; }
    public Map<String, Integer> getWarehouse() { return warehouse; }
    public House getHouse() { return house; }
    public Vehicle getVehicle() { return vehicle; }
    public Partner getPartner() { return partner; }
    public int getLoanAmount() { return loanAmount; }
    public GameEvent getLastEvent() { return lastEvent; }
    public List<String> getAchievements() { return achievements; }
    public int getMaxStorage() { return maxStorage; }
    public int getLivestockCount() { return livestockList.size(); }

    public void addMessage(String msg) { messageLog.add(msg); }
    public List<String> getAndClearMessages() {
        List<String> copy = new ArrayList<>(messageLog);
        messageLog.clear();
        return copy;
    }

    public int getTotalAsset() {
        int total = gold;
        for (Land land : lands) {
            total += land.getPurchasePrice();
        }
        for (Map.Entry<String, Integer> entry : warehouse.entrySet()) {
            total += Market.getInstance().getCurrentPrice(entry.getKey()) * entry.getValue();
        }
        for (Livestock livestock : livestockList) {
            total += livestock.getPurchasePrice();
        }
        total += house.getPrice();
        total += vehicle.getPrice();
        return total;
    }

    public int getUsedStorage() {
        return warehouse.values().stream().mapToInt(Integer::intValue).sum();
    }

    // --- 种植 ---
    public void plant(int landIndex, String cropType) throws LandNotSuitableException, CropNotMatureException {
        if (landIndex < 0 || landIndex >= lands.size()) {
            throw new IndexOutOfBoundsException("无效的土地编号");
        }
        Land land = lands.get(landIndex);
        if (!land.isEmpty()) {
            throw new CropNotMatureException("该土地已有作物，请先收获");
        }
        Crop crop = createCrop(cropType);
        if (crop == null) {
            throw new IllegalArgumentException("未知作物类型: " + cropType);
        }
        if (!land.isSuitableFor(crop)) {
            throw new LandNotSuitableException(land.getTypeName() + "不适合种植" + crop.getName()
                    + "，" + crop.getName() + "适合种在" + crop.getSuitableLandType());
        }
        land.setPlantedCrop(crop);
        plantCount++;
    }

    private String getSuitableCrop(Land land) {
        if (land instanceof BlackSoilLand) return "小麦";
        if (land instanceof SandyLand) return "玉米";
        if (land instanceof RedSoilLand) return "西红柿";
        return null;
    }

    public int plantAll() {
        int count = 0;
        for (int i = 0; i < lands.size(); i++) {
            Land land = lands.get(i);
            if (!land.isEmpty()) continue;
            String cropType = getSuitableCrop(land);
            if (cropType == null) continue;
            try {
                plant(i, cropType);
                count++;
            } catch (Exception ignored) {}
        }
        if (count > 0) addMessage("✔ 一键种植完成，共种植" + count + "块土地");
        else addMessage("✘ 没有可种植的空地");
        return count;
    }

    public int harvestAll() {
        int total = 0;
        for (int i = 0; i < lands.size(); i++) {
            Land land = lands.get(i);
            if (land.isEmpty()) continue;
            try {
                total += harvest(i);
            } catch (Exception ignored) {}
        }
        if (total > 0) addMessage("✔ 一键收获完成，共收获" + total + "单位作物");
        else addMessage("✘ 没有可收获的作物");
        return total;
    }

    private Crop createCrop(String type) {
        switch (type) {
            case "小麦": return new Wheat();
            case "玉米": return new Corn();
            case "西红柿": return new Tomato();
            default: return null;
        }
    }

    // --- 收获 ---
    public int harvest(int landIndex) throws CropNotMatureException {
        if (landIndex < 0 || landIndex >= lands.size()) {
            throw new IndexOutOfBoundsException("无效的土地编号");
        }
        Land land = lands.get(landIndex);
        if (land.isEmpty()) {
            throw new CropNotMatureException("该土地没有作物");
        }
        Crop crop = land.getPlantedCrop();
        if (!crop.isMature()) {
            throw new CropNotMatureException(crop.getName() + "尚未成熟，还需" + (crop.getGrowthCycle() - crop.getGrownWeeks()) + "周");
        }

        double yield = crop.getYieldAmount();
        double modifier = land.getYieldModifier(crop);
        if (land.getFertilizerType() == 1) modifier += 0.2;
        else if (land.getFertilizerType() == 2) modifier += 0.1;
        if (land.hasPesticide()) modifier += 0.1;
        int amount = (int) (yield * modifier);

        int used = getUsedStorage();
        if (used + amount > maxStorage) {
            throw new CropNotMatureException("仓库已满！无法收获，请先出售或升级房屋（当前 " + used + "/" + maxStorage + "）");
        }

        String cropName = crop.getName();
        warehouse.put(cropName, warehouse.getOrDefault(cropName, 0) + amount);
        land.setPlantedCrop(null);
        land.consumeFertilizer();
        land.reduceFertility(5);

        totalCropsHarvested++;
        return amount;
    }

    // --- 购买土地 ---
    public void buyLand(String type) throws InsufficientFundsException {
        Land land;
        switch (type) {
            case "黑土地": land = new BlackSoilLand(); break;
            case "沙地": land = new SandyLand(); break;
            case "红壤": land = new RedSoilLand(); break;
            default: throw new IllegalArgumentException("未知土地类型: " + type);
        }
        int price = land.getPurchasePrice();
        if (gold < price) {
            throw new InsufficientFundsException("金币不足，购买" + type + "需要" + price + "金币");
        }
        gold -= price;
        lands.add(land);
    }

    // --- 牲畜 ---
    public void buyLivestock(String type) throws InsufficientFundsException, BarnFullException {
        if (livestockList.size() >= getMaxLivestockSlots()) {
            throw new BarnFullException("圈舍已满，最多" + getMaxLivestockSlots() + "头牲畜");
        }
        int price = Market.getInstance().getBuyPrice(type);
        if (price <= 0) price = getDefaultAnimalPrice(type);
        if (gold < price) {
            throw new InsufficientFundsException("金币不足，购买" + type + "需要" + price + "金币");
        }
        Livestock animal;
        switch (type) {
            case "鸡": animal = new Chicken(); break;
            case "猪": animal = new Pig(); break;
            case "牛": animal = new Cow(); break;
            default: throw new IllegalArgumentException("未知牲畜类型: " + type);
        }
        gold -= price;
        livestockList.add(animal);
        addMessage("✔ 花费" + price + "金币购买了" + type);
    }

    private int getDefaultAnimalPrice(String type) {
        switch (type) {
            case "鸡": return 150;
            case "猪": return 600;
            case "牛": return 1500;
            default: return 0;
        }
    }

    public int sellLivestock(int index) {
        if (index < 0 || index >= livestockList.size()) return 0;
        Livestock animal = livestockList.remove(index);
        int basePrice = Market.getInstance().getSellPrice(animal.getName());
        if (basePrice <= 0) basePrice = getDefaultAnimalPrice(animal.getName()) / 2;
        int price = Math.max(1, (int) (basePrice * animal.getAgeSellMultiplier()));
        gold += price;
        addMessage("✔ 出售" + animal.getName() + "(" + animal.getAgeStage() + ")，获得" + price + "金币(基准" + basePrice + ")");
        return price;
    }

    private int getMaxLivestockSlots() {
        return 5 + house.getLevel();
    }

    // --- 交易 ---
    public void sellProduct(String item, int amount) throws InsufficientFundsException {
        int available = warehouse.getOrDefault(item, 0);
        if (available < amount) {
            throw new InsufficientFundsException(item + "库存不足，当前仅有" + available);
        }
        Market market = Market.getInstance();
        int price = market.getSellPrice(item);
        double bonus = vehicle.getSellBonus();
        if (partner != null && partner.getType() == PartnerType.BUSINESS_ELITE) {
            bonus += 0.10;
        }
        int income = (int) (price * amount * bonus);

        int remaining = available - amount;
        if (remaining <= 0) {
            warehouse.remove(item);
        } else {
            warehouse.put(item, remaining);
        }
        gold += income;
    }

    public void buyItem(String item, int amount) throws InsufficientFundsException {
        int used = getUsedStorage();
        if (used + amount > maxStorage) {
            throw new InsufficientFundsException("仓库容量不足！当前 " + used + "/" + maxStorage + "，需要额外 " + (used + amount - maxStorage) + " 格空间");
        }
        Market market = Market.getInstance();
        int price = market.getBuyPrice(item);
        int cost = price * amount;
        double discount = (partner != null && partner.getType() == PartnerType.GENTLE_TEACHER) ? 0.9 : 1.0;
        cost = (int) (cost * discount);

        if (gold < cost) {
            throw new InsufficientFundsException("金币不足，需要" + cost + "金币");
        }
        gold -= cost;

        warehouse.put(item, warehouse.getOrDefault(item, 0) + amount);
    }

    // --- 贷款 ---
    public void takeLoan(int amount) {
        loanAmount += amount;
        gold += amount;
    }

    public void repayLoan(int amount) throws InsufficientFundsException {
        amount = Math.min(amount, loanAmount);
        if (gold < amount) {
            throw new InsufficientFundsException("金币不足，无法还款");
        }
        gold -= amount;
        loanAmount -= amount;
    }

    // --- 施肥/打药 ---
    public void applyFertilizer(int landIndex, int type) throws InsufficientFundsException {
        if (landIndex < 0 || landIndex >= lands.size()) {
            throw new IndexOutOfBoundsException("无效的土地编号");
        }
        Land land = lands.get(landIndex);
        if (land.hasFertilizer()) {
            throw new InsufficientFundsException("该土地已施过肥");
        }
        String item = type == 1 ? "化肥" : "猪粪";
        int count = warehouse.getOrDefault(item, 0);
        if (count <= 0) {
            throw new InsufficientFundsException(item + "库存不足");
        }
        land.applyFertilizer(type);
        warehouse.put(item, count - 1);
        totalFertilized++;
        addMessage("✔ 在土地" + (landIndex + 1) + "使用了" + item);
    }

    public void applyPesticide(int landIndex) throws InsufficientFundsException {
        if (landIndex < 0 || landIndex >= lands.size()) {
            throw new IndexOutOfBoundsException("无效的土地编号");
        }
        Land land = lands.get(landIndex);
        if (land.hasPesticide()) {
            throw new InsufficientFundsException("该土地已打过农药");
        }
        int count = warehouse.getOrDefault("农药", 0);
        if (count <= 0) {
            throw new InsufficientFundsException("农药库存不足");
        }
        land.applyPesticide();
        warehouse.put("农药", count - 1);
        addMessage("✔ 在土地" + (landIndex + 1) + "喷洒了农药");
    }

    // --- 治疗牲畜 ---
    public void cureLivestock(int index) throws InsufficientFundsException {
        if (index < 0 || index >= livestockList.size()) {
            throw new IndexOutOfBoundsException("无效的牲畜编号");
        }
        Livestock animal = livestockList.get(index);
        if (!animal.isSick()) {
            throw new InsufficientFundsException(animal.getName() + "没有生病");
        }
        int medicineCount = warehouse.getOrDefault("兽药", 0);
        if (medicineCount <= 0) {
            throw new InsufficientFundsException("兽药库存不足");
        }
        animal.setSick(false);
        animal.setTimesSick(0);
        warehouse.put("兽药", medicineCount - 1);
        totalCured++;
        addMessage("✔ 使用兽药完全治愈了" + animal.getName() + "（生病次数已清零）");
    }

    // --- 生活品质 ---
    public boolean upgradeHouse() throws InsufficientFundsException {
        House next = House.nextLevel(house);
        if (next == null) return false;
        if (gold < next.getPrice()) {
            throw new InsufficientFundsException("金币不足，升级到" + next.getName() + "需要" + next.getPrice() + "金币");
        }
        gold -= next.getPrice();
        house = next;
        maxStorage = house.getStorageBonus();
        return true;
    }

    public boolean upgradeVehicle() throws InsufficientFundsException {
        Vehicle next = Vehicle.nextLevel(vehicle);
        if (next == null) return false;
        if (gold < next.getPrice()) {
            throw new InsufficientFundsException("金币不足，购买" + next.getName() + "需要" + next.getPrice() + "金币");
        }
        gold -= next.getPrice();
        vehicle = next;
        return true;
    }

    public void seekPartner(int choice) throws InsufficientFundsException {
        if (partner != null) {
            throw new IllegalStateException("已有伴侣，不可再娶");
        }
        if (house.getLevel() < 4) {
            throw new IllegalStateException("需要豪华庄园才能求婚");
        }
        if (getTotalAsset() < 30000) {
            throw new InsufficientFundsException("总资产需达到30000才能求婚");
        }
        PartnerType[] types = PartnerType.values();
        if (choice < 0 || choice >= types.length) {
            throw new IllegalArgumentException("无效选择");
        }
        partner = new Partner(types[choice]);
    }

    /** 商业精英：生成下周价格预测 */
    private void generateMarketPrediction() {
        Market m = Market.getInstance();
        StringBuilder pred = new StringBuilder("  [伴侣] 商业精英下周市场预测：");
        String[] items = {"小麦", "玉米", "西红柿", "鸡蛋", "牛奶", "鸡", "猪", "牛"};
        int count = 0;
        for (String item : items) {
            double fluct = m.getFluctuation(item);
            double next = fluct * (0.88 + Math.random() * 0.24);
            next = Math.max(0.5, Math.min(1.5, next));
            String trend;
            if (next > fluct + 0.05) trend = "📈涨";
            else if (next < fluct - 0.05) trend = "📉跌";
            else trend = "→平";
            if (count > 0) pred.append("，");
            pred.append(item).append(trend);
            count++;
        }
        addMessage(pred.toString());
    }

    /** 厨师能手：每周将原料加工为高价值产品 */
    private void processByChef() {
        // 3小麦 → 1面包  (价值 120 vs 90)
        if (warehouse.getOrDefault("小麦", 0) >= 3) {
            warehouse.put("小麦", warehouse.get("小麦") - 3);
            warehouse.put("面包", warehouse.getOrDefault("面包", 0) + 1);
            addMessage("  [伴侣] 厨师用3份小麦烤出了面包");
        }
        // 3西红柿 → 1番茄酱 (价值 170 vs 135)
        if (warehouse.getOrDefault("西红柿", 0) >= 3) {
            warehouse.put("西红柿", warehouse.get("西红柿") - 3);
            warehouse.put("番茄酱", warehouse.getOrDefault("番茄酱", 0) + 1);
            addMessage("  [伴侣] 厨师用3份西红柿熬出了番茄酱");
        }
        // 2鸡蛋 → 1蛋糕   (价值 100 vs 28)
        if (warehouse.getOrDefault("鸡蛋", 0) >= 2) {
            warehouse.put("鸡蛋", warehouse.get("鸡蛋") - 2);
            warehouse.put("蛋糕", warehouse.getOrDefault("蛋糕", 0) + 1);
            addMessage("  [伴侣] 厨师用2枚鸡蛋烤出了蛋糕");
        }
    }

    /** 处理一个动物列表的每周逻辑：成长、消耗、产出 */
    private void processAnimalList(List<Livestock> animals, List<Livestock> toRemove, String prefix) {
        for (Livestock animal : animals) {
            if (animal.isDead()) {
                toRemove.add(animal);
                continue;
            }
            animal.passWeek();
            animal.setSick(false);

            if (animal.isDead()) {
                addMessage(prefix + " [死亡] " + animal.getName() + "寿命已尽，自然死亡");
                toRemove.add(animal);
                continue;
            }

            boolean hasFood = true;
            for (Map.Entry<String, Integer> feed : animal.getWeeklyConsumption().entrySet()) {
                int available = warehouse.getOrDefault(feed.getKey(), 0);
                if (available < feed.getValue()) { hasFood = false; break; }
            }

            if (!hasFood) {
                addMessage(prefix + " [提醒] " + animal.getName() + "饲料不足，本周无产出");
                continue;
            }

            for (Map.Entry<String, Integer> feed : animal.getWeeklyConsumption().entrySet()) {
                int remaining = warehouse.get(feed.getKey()) - feed.getValue();
                if (remaining <= 0) warehouse.remove(feed.getKey());
                else warehouse.put(feed.getKey(), remaining);
            }

            if (animal.canProduce() && !animal.isSick()) {
                String product = animal.getProductName();
                int baseAmount = animal.getProductAmount();
                double sickPenalty = 1.0 - animal.getTimesSick() * 0.25;
                double seasonFactor = currentSeason.getYieldRate();
                int amount = Math.max(1, (int) (baseAmount * sickPenalty * seasonFactor));
                String note = "";
                if (amount < baseAmount) note += " (缩水" + amount + "/" + baseAmount + ")";
                if (seasonFactor < 1) note += " [冬季减产]";
                else if (seasonFactor > 1) note += " [春季增产]";
                if (getUsedStorage() + amount > maxStorage) {
                    addMessage(prefix + " [提醒] 仓库已满，" + animal.getName() + "的产出(" + product + "x" + amount + ")" + note + "无处存放！");
                } else {
                    warehouse.put(product, warehouse.getOrDefault(product, 0) + amount);
                    addMessage(prefix + " [产出] " + animal.getName() + "产出了" + amount + "个" + product + note);
                }
            }
        }
    }

    // --- 度过一周 ---
    public boolean nextWeek() {
        week++;
        int goldStart = gold;

        // 季节推进
        weeksInSeason++;
        if (weeksInSeason > 4) {
            currentSeason = currentSeason.next();
            weeksInSeason = 1;
        }

        // 土地生长
        for (Land land : lands) {
            if (!land.isEmpty()) {
                land.onWeekPass(currentSeason);
                if (land.hasFertilizer()) {
                    int cost = 3;
                    if (partner != null && partner.getType() == PartnerType.AGRICULTURE_EXPERT) cost = 2;
                    land.reduceFertility(cost);
                    if (land.getFertility() <= 0) {
                        land.consumeFertilizer();
                    }
                }
            }
        }

        // 牲畜处理（包括主栏和新生栏）
        List<Livestock> toRemove = new ArrayList<>();
        processAnimalList(livestockList, toRemove, "");
        processAnimalList(newborns, toRemove, "  [新生]");
        livestockList.removeAll(toRemove);
        newborns.removeAll(toRemove);

        // 繁殖：同种成熟牲畜 ≥2 头时，每周 5% 概率产仔
        Map<String, List<Livestock>> breedGroups = new HashMap<>();
        for (Livestock a : livestockList) {
            if (a.isMature() && !a.isDead()) {
                breedGroups.computeIfAbsent(a.getName(), k -> new ArrayList<>()).add(a);
            }
        }
        for (Map.Entry<String, List<Livestock>> e : breedGroups.entrySet()) {
            if (e.getValue().size() >= 2 && Math.random() < 0.05) {
                Livestock baby;
                switch (e.getKey()) {
                    case "鸡": baby = new Chicken(); break;
                    case "猪": baby = new Pig(); break;
                    case "牛": baby = new Cow(); break;
                    default: continue;
                }
                newborns.add(baby);
                breedCount++;
                addMessage("  [繁殖] " + e.getKey() + "生下了幼崽！");
            }
        }

        // 随机事件
        triggerRandomEvent();

        // 市场价格波动
        Market.getInstance().updatePrices(currentSeason);

        // 贷款利息
        if (loanAmount > 0) {
            int interest = loanAmount * loanInterestRate / 100;
            gold -= interest;
            addMessage("  [利息] 本周需还利息" + interest + "金币");
        }

        // 伴侣家庭开销
        if (partner != null) {
            gold -= 100;
            addMessage("  [家庭] 每周家庭开销100金币");

            // 伴侣技能触发
            switch (partner.getType()) {
                case GENTLE_TEACHER:
                    if (Math.random() < 0.4) {
                        int saved = 30 + (int)(Math.random() * 70);
                        gold += saved;
                        addMessage("  [伴侣] 温柔教师精打细算，本周省下了" + saved + "金币");
                    }
                    break;
                case AGRICULTURE_EXPERT:
                    if (Math.random() < 0.4) {
                        addMessage("  [伴侣] 农业专家帮你照料田地，土地肥力恢复");
                        for (Land land : lands) {
                            land.reduceFertility(-5);
                        }
                    }
                    break;
                case BUSINESS_ELITE:
                    generateMarketPrediction();
                    break;
                case CHEF:
                    processByChef();
                    break;
            }
        }

        // 检查破产
        if (checkBankruptcy()) {
            return true;
        }

        // 每周财务小结
        int netGold = gold - goldStart;
        if (netGold >= 0) {
            addMessage("  [结算] 本周净收入 +" + netGold + "金币 (余额: " + gold + ")");
        } else {
            addMessage("  [结算] 本周净支出 " + netGold + "金币 (余额: " + gold + ")");
        }

        // 检查成就
        checkAchievements();

        // 更新峰值资产
        peakAsset = Math.max(peakAsset, getTotalAsset());
        return false;
    }

    private void triggerRandomEvent() {
        if (Math.random() < 0.35) {
            GameEvent[] events;
            if (consecutiveDisasterWeeks >= 2) {
                events = new GameEvent[]{new SunshineEvent()};
                consecutiveDisasterWeeks = 0;
            } else {
                events = new GameEvent[]{
                        new StormEvent(),
                        new HailEvent(),
                        new SunshineEvent(),
                        new DiseaseEvent()
                };
            }

            GameEvent event = events[new Random().nextInt(events.length)];
            lastEvent = event;
            addMessage("  [事件] " + event.getDescription());
            event.apply(this);

            if (event instanceof StormEvent || event instanceof HailEvent || event instanceof DiseaseEvent) {
                consecutiveDisasterWeeks++;
            } else {
                consecutiveDisasterWeeks = 0;
            }
        } else {
            lastEvent = null;
            consecutiveDisasterWeeks = 0;
        }
    }

    private boolean checkBankruptcy() {
        if (loanAmount <= 0) return false;

        // 条件1: 资不抵债 — 总资产 < 总负债
        int totalAsset = getTotalAsset();
        int totalDebt = loanAmount + (int)(loanAmount * loanInterestRate / 100.0);
        if (totalAsset < totalDebt) {
            addMessage("  [破产] 资不抵债！总资产(" + totalAsset + ") < 总负债(" + totalDebt + ")");
            return true;
        }

        // 条件2: 流动性枯竭 — 金币 < 0 且无法偿还本周利息
        if (gold < 0) {
            int interest = loanAmount * loanInterestRate / 100;
            if (gold + interest < 0) {
                addMessage("  [破产] 流动性枯竭！手头金币(" + gold + ")无法偿还利息(" + interest + ")");
                return true;
            }
        }
        return false;
    }

    // --- 成就系统 ---
    private void checkAchievements() {
        // 🌾 种植相关
        if (totalCropsHarvested >= 1 && !unlockedAchievements.contains("🌾 首次丰收"))
            addAchievement("🌾 首次丰收", "成功收获第一批作物");
        if (totalCropsHarvested >= 50 && !unlockedAchievements.contains("🌽 丰收大师"))
            addAchievement("🌽 丰收大师", "累计收获50次作物");
        if (totalCropsHarvested >= 200 && !unlockedAchievements.contains("🌽 丰收之神"))
            addAchievement("🌽 丰收之神", "累计收获200次作物");
        if (plantCount >= 10 && !unlockedAchievements.contains("🌱 种植达人"))
            addAchievement("🌱 种植达人", "累计种植10次作物");
        if (plantCount >= 50 && !unlockedAchievements.contains("🌱 种植狂人"))
            addAchievement("🌱 种植狂人", "累计种植50次作物");

        // 💰 资产相关
        if (getTotalAsset() >= 10000 && !unlockedAchievements.contains("💰 资产破万"))
            addAchievement("💰 资产破万", "农场总资产突破10000");
        if (getTotalAsset() >= 50000 && !unlockedAchievements.contains("🏠 中产阶级"))
            addAchievement("🏠 中产阶级", "农场总资产突破50000");
        if (getTotalAsset() >= 200000 && !unlockedAchievements.contains("💎 百万富翁"))
            addAchievement("💎 百万富翁", "农场总资产突破200000");
        if (getTotalAsset() >= 500000 && !unlockedAchievements.contains("👑 农场大亨"))
            addAchievement("👑 农场大亨", "农场总资产突破500000");

        // 🏞 土地相关
        if (lands.size() >= 5 && !unlockedAchievements.contains("🏞 小地主"))
            addAchievement("🏞 小地主", "拥有5块土地");
        if (lands.size() >= 10 && !unlockedAchievements.contains("🗺 大地主"))
            addAchievement("🗺 大地主", "拥有10块土地");

        // 🐓 牲畜相关
        if (breedCount >= 1 && !unlockedAchievements.contains("🐣 新生命"))
            addAchievement("🐣 新生命", "成功繁殖第一次");
        if (breedCount >= 10 && !unlockedAchievements.contains("🐣 繁殖大师"))
            addAchievement("🐣 繁殖大师", "成功繁殖10次牲畜");
        if (totalCured >= 1 && !unlockedAchievements.contains("🏥 初次治疗"))
            addAchievement("🏥 初次治疗", "成功治疗一头生病的牲畜");
        if (totalCured >= 10 && !unlockedAchievements.contains("🚑 妙手回春"))
            addAchievement("🚑 妙手回春", "成功治疗10头生病的牲畜");
        int livestockCount = livestockList.size();
        if (livestockCount >= 10 && !unlockedAchievements.contains("🐄 牲畜成群"))
            addAchievement("🐄 牲畜成群", "同时拥有10头牲畜");

        // 🧪 施肥相关
        if (totalFertilized >= 1 && !unlockedAchievements.contains("🧪 初次施肥"))
            addAchievement("🧪 初次施肥", "第一次施肥");
        if (totalFertilized >= 20 && !unlockedAchievements.contains("🧪 农艺大师"))
            addAchievement("🧪 农艺大师", "累计施肥20次");

        // 🏡 生活品质
        if (house.getLevel() >= 4 && !unlockedAchievements.contains("🏰 庄园主"))
            addAchievement("🏰 庄园主", "拥有豪华庄园");
        if (vehicle.getLevel() >= 4 && !unlockedAchievements.contains("🚗 成功人士"))
            addAchievement("🚗 成功人士", "拥有进口豪车");
        if (partner != null && !unlockedAchievements.contains("💑 成家立业"))
            addAchievement("💑 成家立业", "找到人生伴侣");

        // ⏰ 经营时长
        if (week >= 52 && !unlockedAchievements.contains("⏰ 经营满年"))
            addAchievement("⏰ 经营满年", "成功经营农场一整年");
        if (week >= 104 && !unlockedAchievements.contains("📅 两年之期"))
            addAchievement("📅 两年之期", "坚持经营农场两年");

        // 💸 特殊成就
        if (loanAmount >= 50000 && !unlockedAchievements.contains("💸 负债累累"))
            addAchievement("💸 负债累累", "贷款超过50000金币");
        if (gold >= 100000 && !unlockedAchievements.contains("💰 日进斗金"))
            addAchievement("💰 日进斗金", "手头现金超过100000金币");
    }

    private void addAchievement(String title, String desc) {
        achievements.add(title + " — " + desc);
        unlockedAchievements.add(title);
        addMessage("  [成就解锁] " + title + "！" + desc);
    }

    // --- 存档 ---
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(gold).append(",").append(week).append(",")
                .append(currentSeason.name()).append(",").append(weeksInSeason).append("\n");
        sb.append(house.getLevel()).append(",").append(vehicle.getLevel()).append("\n");
        if (partner != null) {
            sb.append(partner.getType().name()).append("\n");
        } else {
            sb.append("null\n");
        }
        sb.append(loanAmount).append(",").append(loanInterestRate).append("\n");
        sb.append(maxStorage).append(",").append(consecutiveDisasterWeeks).append(",").append(totalCropsHarvested).append(",").append(peakAsset)
                .append(",").append(plantCount).append(",").append(breedCount).append(",").append(totalCured).append(",").append(totalFertilized).append("\n");

        for (Land land : lands) {
            sb.append("LAND,").append(land.getClass().getSimpleName()).append(",")
                    .append(land.getFertility()).append(",")
                    .append(land.getFertilizerType()).append(",")
                    .append(land.getPesticideTurns()).append(",");
            if (!land.isEmpty()) {
                Crop c = land.getPlantedCrop();
                sb.append(c.getClass().getSimpleName()).append(",").append(c.getGrownWeeks());
            }
            sb.append("\n");
        }

        for (Livestock animal : livestockList) {
            sb.append("LIVESTOCK,").append(animal.getClass().getSimpleName()).append(",")
                    .append(animal.getOwnedWeeks()).append(",")
                    .append(animal.isSick()).append(",")
                    .append(animal.getTimesSick()).append("\n");
        }

        for (Livestock animal : newborns) {
            sb.append("NEWBORN,").append(animal.getClass().getSimpleName()).append(",")
                    .append(animal.getOwnedWeeks()).append(",")
                    .append(animal.isSick()).append(",")
                    .append(animal.getTimesSick()).append("\n");
        }

        for (Map.Entry<String, Integer> entry : warehouse.entrySet()) {
            sb.append("ITEM,").append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    public void deserialize(String data) {
        String[] lines = data.split("\n");
        int idx = 0;

        String[] parts = lines[idx++].split(",");
        gold = Integer.parseInt(parts[0]);
        week = Integer.parseInt(parts[1]);
        currentSeason = Season.valueOf(parts[2]);
        weeksInSeason = Integer.parseInt(parts[3]);

        parts = lines[idx++].split(",");
        int houseLevel = Integer.parseInt(parts[0]);
        int vehicleLevel = Integer.parseInt(parts[1]);

        String partnerStr = lines[idx++].trim();
        partner = partnerStr.equals("null") ? null : new Partner(PartnerType.valueOf(partnerStr));

        parts = lines[idx++].split(",");
        loanAmount = Integer.parseInt(parts[0]);
        // 利率是游戏平衡参数，始终使用当前默认值，不依赖存档
        loanInterestRate = 3;

        parts = lines[idx++].split(",");
        maxStorage = Integer.parseInt(parts[0]);
        consecutiveDisasterWeeks = Integer.parseInt(parts[1]);
        totalCropsHarvested = Integer.parseInt(parts[2]);
        peakAsset = Integer.parseInt(parts[3]);
        // 兼容旧存档：新字段可能不存在
        plantCount = parts.length > 4 ? Integer.parseInt(parts[4]) : 0;
        breedCount = parts.length > 5 ? Integer.parseInt(parts[5]) : 0;
        totalCured = parts.length > 6 ? Integer.parseInt(parts[6]) : 0;
        totalFertilized = parts.length > 7 ? Integer.parseInt(parts[7]) : 0;

        lands.clear();
        livestockList.clear();
        newborns.clear();
        warehouse.clear();

        // Rebuild House/Vehicle based on level
        house = House.getDefault();
        vehicle = Vehicle.getDefault();
        for (int i = 1; i < houseLevel; i++) {
            House next = House.nextLevel(house);
            if (next != null) house = next;
        }
        // 同步 maxStorage 与房屋等级，避免读档后容量与房屋不匹配
        maxStorage = house.getStorageBonus();
        for (int i = 1; i < vehicleLevel; i++) {
            Vehicle next = Vehicle.nextLevel(vehicle);
            if (next != null) vehicle = next;
        }

        while (idx < lines.length) {
            String line = lines[idx++].trim();
            if (line.isEmpty()) continue;
            String[] tokens = line.split(",");
            switch (tokens[0]) {
                case "LAND": {
                    Land land = createLand(tokens[1]);
                    if (land != null) {
                        // 兼容新旧存档格式
                        boolean newFormat = tokens[3].matches("\\d+");
                        if (newFormat) {
                            int ft = Integer.parseInt(tokens[3]);
                            if (ft > 0) land.applyFertilizer(ft);
                            // tokens[4] 可能是旧版 boolean 或新版 int(pesticideTurns)
                            if (tokens[4].matches("\\d+"))
                                for (int i = 0; i < Integer.parseInt(tokens[4]); i++) land.applyPesticide();
                            else if (Boolean.parseBoolean(tokens[4]))
                                land.applyPesticide();
                        } else {
                            if (Boolean.parseBoolean(tokens[3])) land.applyFertilizer(1);
                        }
                        int cropTokenIdx = newFormat ? 5 : 4;
                        if (tokens.length > cropTokenIdx && !tokens[cropTokenIdx].isEmpty()) {
                            Crop crop = createCropFromClassName(tokens[cropTokenIdx]);
                            if (crop != null) {
                                int grown = Integer.parseInt(tokens[cropTokenIdx + 1]);
                                for (int i = 0; i < grown; i++) {
                                    crop.grow(currentSeason, land);
                                }
                                land.setPlantedCrop(crop);
                            }
                        }
                        lands.add(land);
                    }
                    break;
                }
                case "LIVESTOCK": {
                    Livestock animal = createLivestock(tokens[1]);
                    if (animal != null) {
                        int owned = Integer.parseInt(tokens[2]);
                        for (int i = 0; i < owned; i++) animal.passWeek();
                        animal.setSick(Boolean.parseBoolean(tokens[3]));
                        if (tokens.length > 4 && tokens[4].matches("\\d+"))
                            animal.setTimesSick(Integer.parseInt(tokens[4]));
                        else if (tokens.length > 4)
                            animal.setDead(Boolean.parseBoolean(tokens[4]));
                        livestockList.add(animal);
                    }
                    break;
                }
                case "NEWBORN": {
                    Livestock animal = createLivestock(tokens[1]);
                    if (animal != null) {
                        int owned = Integer.parseInt(tokens[2]);
                        for (int i = 0; i < owned; i++) animal.passWeek();
                        animal.setSick(Boolean.parseBoolean(tokens[3]));
                        if (tokens.length > 4 && tokens[4].matches("\\d+"))
                            animal.setTimesSick(Integer.parseInt(tokens[4]));
                        else if (tokens.length > 4)
                            animal.setDead(Boolean.parseBoolean(tokens[4]));
                        newborns.add(animal);
                    }
                    break;
                }
                case "ITEM": {
                    warehouse.put(tokens[1], Integer.parseInt(tokens[2]));
                    break;
                }
            }
        }
    }

    private Land createLand(String className) {
        switch (className) {
            case "BlackSoilLand": return new BlackSoilLand();
            case "SandyLand": return new SandyLand();
            case "RedSoilLand": return new RedSoilLand();
            default: return null;
        }
    }

    private Crop createCropFromClassName(String className) {
        switch (className) {
            case "Wheat": return new Wheat();
            case "Corn": return new Corn();
            case "Tomato": return new Tomato();
            default: return null;
        }
    }

    private Livestock createLivestock(String className) {
        switch (className) {
            case "Chicken": return new Chicken();
            case "Pig": return new Pig();
            case "Cow": return new Cow();
            default: return null;
        }
    }

    // --- 市场报告 ---
    public String getMarketReport() {
        Market market = Market.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("📊 本周市场价格\n");
        sb.append(String.format("%-10s %-8s %-8s\n", "商品", "价格", "趋势"));
        for (int i = 0; i < 30; i++) sb.append('-');
        String[] items = {"小麦", "玉米", "西红柿", "鸡蛋", "猪粪", "牛奶"};
        for (String item : items) {
            if (market.getBasePrice(item) > 0) {
                sb.append(String.format("%-10s %-8d %-8s\n",
                        item, market.getCurrentPrice(item), market.getPriceTrend(item)));
            }
        }
        return sb.toString();
    }
}
