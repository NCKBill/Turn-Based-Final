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

    public boolean canExecute(Unit unit, Cell start, Cell target) {
        boolean canExecute = true;
        if (unit.getActionPoint() < this.getApCost()) {
            System.out.println("Not enough AP");
            canExecute = false;
        }

        if (!isInRange(start, target)) {
            System.out.println("Not in range.");
            canExecute = false;
        }

        if (!this.targetFriendly && unit.isTargetFriendly(target.getUnit())) {
            System.out.println("Cannot damage allies.");
            canExecute = false;
        }

        if (this.targetFriendly && !unit.isTargetFriendly(target.getUnit())) {
            System.out.println("Cannot buff enemies.");
            canExecute = false;
        }
        return canExecute;
    }
    public boolean isInRange(Cell start, Cell target) {
        int x = target.getCol() - start.getCol();
        int y = target.getRow() - start.getRow();
        double delta = Math.pow(x, 2) + Math.pow(y, 2);
        int range = (int) Math.sqrt(delta);
        return this.range >= range;
    }

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