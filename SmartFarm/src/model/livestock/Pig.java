package model.livestock;

import java.util.HashMap;
import java.util.Map;

public class Pig extends Livestock {
    public Pig() {
        super("猪", 600, 5, 20,
              new HashMap<String, Integer>() {{ put("玉米", 2); }},
              "猪粪", 3);
    }

    @Override
    public int getProductSellPrice() {
        return 12;
    }
}
