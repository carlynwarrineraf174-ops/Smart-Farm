package model.event;

import farm.FarmManager;

public abstract class GameEvent {
    protected String name;
    protected String description;

    public GameEvent(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public abstract void apply(FarmManager farm);
}
