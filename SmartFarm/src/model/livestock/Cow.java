package model.livestock;

import java.util.HashMap;
import java.util.Map;

public class Cow extends Livestock {
    public Cow() {
        super("牛", 1500, 3, 36,
              new HashMap<String, Integer>() {{ put("小麦", 1); put("玉米", 1); }},
              "牛奶", 2);
    }

    @Override
    public int getProductSellPrice() {
        return 25;
    }
}
