package Action;

import Board.Cell;
import Unit.Unit;

/**
 * Base class for all character skills/actions.
 */

public abstract class Action {
    // Encapsulation of action properties
    private String type;
    private String name;
    private int apCost;
    private int value;
    private int range;
    private boolean targetFriendly;

    // Constructor to initialize the action's properties
    public Action(String type, String name, int apCost, int value, int range, boolean targetFriendly) {
        this.type = type;
        this.name = name;
        this.apCost = apCost;
        this.value = value;
        this.range = range;
        this.targetFriendly = targetFriendly;
    }

    public abstract int execute(Unit unit, Cell target);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getApCost() {
        return apCost;
    }

    public void setApCost(int apCost) {
        this.apCost = apCost;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setTargetFriendly(boolean targetFriendly) {
        this.targetFriendly = targetFriendly;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTargetFriendly() {
        return targetFriendly;
    }
}