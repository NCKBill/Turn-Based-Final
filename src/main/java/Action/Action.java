package Action;

import Board.Cell;
import Unit.Unit;

/**
 * Base class for all character skills/actions.
 */

public abstract class Action {
    private final String type; // Buff | Damage
    private final String name;
    private final int apCost; // apCost out of 6
    private final int value; // default damage/heal before modifier
    private final int range;
    private final boolean targetFriendly; // can target friendly or not

    public Action(String type, String name, int apCost, int value, int range, boolean targetFriendly) {
        this.type = type;
        this.name = name;
        this.apCost = apCost;
        this.value = value;
        this.range = range;
        this.targetFriendly = targetFriendly;
    }

    public abstract int execute(Unit unit, Cell target);

    /**
     *
     * @param unit: current unit
     * @param start: cell of the current unit
     * @param target: cell of the targeted unit
     * @return whether the action casting from start can be executed to target
     */
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

    // Calculate hypotenuse for range
    // Get the rounded down value
    // Return whether the action can reach target
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

    public int getApCost() {
        return apCost;
    }

    public int getValue() {
        return value;
    }


    public String getType() {
        return type;
    }

    public boolean isTargetFriendly() {
        return targetFriendly;
    }
}