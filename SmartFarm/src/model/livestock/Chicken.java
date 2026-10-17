package model.livestock;

import java.util.HashMap;
import java.util.Map;

public class Chicken extends Livestock {
    public Chicken() {
        super("鸡", 150, 1, 24,
              new HashMap<String, Integer>() {{ put("小麦", 1); }},
              "鸡蛋", 3);
    }

    @Override
    public int getProductSellPrice() {
        return 8;
    }
}
