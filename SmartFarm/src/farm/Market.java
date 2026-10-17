package farm;

import model.enums.Season;

import java.util.*;

public class Market {
    private static Market instance;
    private Map<String, Integer> basePrices;
    private Map<String, Double> priceFluctuations;
    private Map<String, List<Integer>> priceHistory;
    private Map<String, Double> sellMargins;
    private Random random;

    private Market() {
        random = new Random();
        basePrices = new HashMap<>();
        priceFluctuations = new HashMap<>();
        priceHistory = new HashMap<>();
        sellMargins = new HashMap<>();
        initPrices();
        initMargins();
        // 初始生成随机波动
        for (String key : basePrices.keySet()) {
            priceFluctuations.put(key, 0.8 + random.nextDouble() * 0.4);
        }
    }

    public static Market getInstance() {
        if (instance == null) {
            instance = new Market();
        }
        return instance;
    }

    private void initPrices() {
        basePrices.put("小麦", 30);
        basePrices.put("玉米", 25);
        basePrices.put("西红柿", 45);
        basePrices.put("鸡蛋", 14);
        basePrices.put("猪粪", 25);
        basePrices.put("牛奶", 55);
        basePrices.put("化肥", 50);
        basePrices.put("农药", 80);
        basePrices.put("兽药", 100);
        basePrices.put("鸡", 150);
        basePrices.put("猪", 600);
        basePrices.put("牛", 1500);
        // 加工品（厨师伴侣专属）
        basePrices.put("面包", 120);
        basePrices.put("番茄酱", 170);
        basePrices.put("蛋糕", 100);
    }

    /** 各物品卖出折价率（买入=100%，卖出价=时价×折价率） */
    private void initMargins() {
        // 作物 — 大宗商品，薄利多销
        for (String s : new String[]{"小麦", "玉米", "西红柿"}) sellMargins.put(s, 0.80);
        // 畜产品
        sellMargins.put("鸡蛋", 0.75);
        sellMargins.put("牛奶", 0.70);  // 易腐品折价高
        sellMargins.put("猪粪", 0.85);  // 小众但稳定
        // 消耗品 — 几乎不能回卖
        for (String s : new String[]{"化肥", "农药", "兽药"}) sellMargins.put(s, 0.10);
        // 牲畜 — 活体交易折价
        sellMargins.put("鸡", 0.60);
        sellMargins.put("猪", 0.60);
        sellMargins.put("牛", 0.60);
        // 加工品 — 附加值高
        sellMargins.put("面包", 0.90);
        sellMargins.put("番茄酱", 0.90);
        sellMargins.put("蛋糕", 0.90);
    }

    /** 季节对各类商品价格的影响 */
    private double getSeasonFactor(String item, Season season) {
        switch (item) {
            // 作物：丰收季节便宜（夏秋），淡季贵（冬）
            case "小麦":
                switch (season) {
                    case SPRING: return 1.0;
                    case SUMMER: return 0.90;
                    case AUTUMN: return 0.70;  // 秋收，价格最低
                    case WINTER: return 1.30;  // 冬季稀缺
                }
            case "玉米":
                switch (season) {
                    case SPRING: return 1.0;
                    case SUMMER: return 0.85;
                    case AUTUMN: return 0.65;
                    case WINTER: return 1.40;
                }
            case "西红柿":
                switch (season) {
                    case SPRING: return 1.0;
                    case SUMMER: return 0.75;  // 夏季高产
                    case AUTUMN: return 0.85;
                    case WINTER: return 1.30;
                }
            // 畜产品：冬季略低
            case "鸡蛋":
                return season == Season.WINTER ? 0.90 : 1.0;
            case "牛奶":
                return season == Season.WINTER ? 0.90 : (season == Season.SUMMER ? 0.95 : 1.0);
            case "猪粪":
                return 1.0;  // 全年稳定
            // 消耗品：淡旺季波动
            case "化肥":
                switch (season) {
                    case SPRING: return 1.05;  // 春耕需求大
                    case SUMMER: return 0.90;
                    case AUTUMN: return 1.10;  // 秋肥
                    case WINTER: return 0.85;
                }
            case "农药":
                switch (season) {
                    case SPRING: return 1.0;
                    case SUMMER: return 1.15;  // 虫害高发
                    case AUTUMN: return 0.90;
                    case WINTER: return 0.85;
                }
            case "兽药":
                return 1.0;
            // 牲畜价格稳定
            default: return 1.0;
        }
    }

    /** 每周更新价格：随机游走 + 季节因子 */
    public void updatePrices(Season season) {
        // 存档本周价格
        for (String key : basePrices.keySet()) {
            priceHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(getCurrentPrice(key));
        }
        // 随机游走：在前一周基础±12%内变动
        for (String key : basePrices.keySet()) {
            double prev = priceFluctuations.getOrDefault(key, 1.0);
            double step = 0.88 + random.nextDouble() * 0.24;  // 0.88~1.12
            double newFluct = prev * step;
            newFluct = Math.max(0.5, Math.min(1.5, newFluct));
            priceFluctuations.put(key, newFluct);
        }
    }

    // ──────── 价格查询 ────────

    /** 市场时价（含随机波动 + 季节因子） */
    public int getCurrentPrice(String item) {
        int base = basePrices.getOrDefault(item, 0);
        double fluct = priceFluctuations.getOrDefault(item, 1.0);
        return (int) (base * fluct);
    }

    /** 买入价 = 时价 × 季节因子 */
    public int getBuyPrice(String item) {
        return getCurrentPrice(item);
    }

    /** 卖出价 = 时价 × 物品折价率（需传入季节以显示准确季节性） */
    public int getSellPrice(String item) {
        int price = getCurrentPrice(item);
        double margin = sellMargins.getOrDefault(item, 0.5);
        return Math.max(1, (int) (price * margin));
    }

    /** 获取指定季节下的预估卖出价（用于市场报告中的季节提示） */
    public int getSeasonalSellPrice(String item, Season season) {
        int base = basePrices.getOrDefault(item, 0);
        double fluct = priceFluctuations.getOrDefault(item, 1.0);
        double seasonFactor = getSeasonFactor(item, season);
        double margin = sellMargins.getOrDefault(item, 0.5);
        return Math.max(1, (int) (base * fluct * seasonFactor * margin));
    }

    /** 获取季节因子（给UI显示用） */
    public double getItemSeasonFactor(String item, Season season) {
        return getSeasonFactor(item, season);
    }

    public double getMargin(String item) {
        return sellMargins.getOrDefault(item, 0.5);
    }

    // ──────── 历史记录 ────────

    public List<Integer> getPriceHistory(String item, int weeks) {
        List<Integer> history = priceHistory.getOrDefault(item, new ArrayList<>());
        int current = getCurrentPrice(item);
        List<Integer> result = new ArrayList<>(history);
        result.add(current);
        int size = result.size();
        return result.subList(Math.max(0, size - weeks), size);
    }

    public String getPriceSparkline(String item) {
        List<Integer> prices = getPriceHistory(item, 5);
        if (prices.size() < 2) return "—";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prices.size() - 1; i++) {
            int diff = prices.get(i + 1) - prices.get(i);
            if (diff > 5) sb.append("⬆");
            else if (diff > 0) sb.append("↗");
            else if (diff < -5) sb.append("⬇");
            else if (diff < 0) sb.append("↘");
            else sb.append("→");
        }
        return sb.toString();
    }

    public boolean hasPriceHistory(String item) {
        return priceHistory.containsKey(item) && priceHistory.get(item).size() >= 2;
    }

    // ──────── 波动/趋势 ────────

    public double getFluctuation(String item) {
        return priceFluctuations.getOrDefault(item, 1.0);
    }

    public int getBasePrice(String item) {
        return basePrices.getOrDefault(item, 0);
    }

    public Map<String, Double> getPriceFluctuations() {
        return priceFluctuations;
    }

    public String getPriceTrend(String item) {
        double fluct = getFluctuation(item);
        if (fluct >= 1.25) return "📈 大涨";
        if (fluct >= 1.10) return "↑ 上涨";
        if (fluct <= 0.70) return "📉 暴跌";
        if (fluct <= 0.85) return "↓ 下跌";
        return "→ 平稳";
    }

    public String getFluctuationPercent(String item) {
        double fluct = getFluctuation(item);
        if (fluct >= 1.0) return "+" + (int)((fluct - 1.0) * 100) + "%";
        else return "-" + (int)((1.0 - fluct) * 100) + "%";
    }

    public String getSeasonHint(String item, Season season) {
        double factor = getSeasonFactor(item, season);
        if (factor > 1.1) return "🔥旺季涨价";
        if (factor < 0.9) return "❄️淡季跌价";
        return "→价格平稳";
    }
}
