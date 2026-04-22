package Action;

import Board.Cell;
import Unit.Unit;

public class Heal extends Action {

    public Heal(String type, String name, int apCost, int value, int range, boolean targetsFriendly) {
        super(type, name, apCost, value, range, targetsFriendly);
    }

    @Override
    public int execute(Unit healer, Cell targetCell) {
        Unit target = targetCell.getUnit();
        int heal = 0;
        if (target != null) {
            // check if target is an ally
            if (healer.isTargetFriendly(target)) {
                System.out.println(healer.getName() + " casts " + this.getName() + " on " + target.getName());

                // heal target
                // deduct ap cost
                heal = this.getValue() + healer.getPower();
                healer.setActionPoint(healer.getActionPoint() - this.getApCost());

                System.out.println(target.getName() + " was healed for " + this.getValue() + " HP.");
            } else {
                // prevent player from accidentally healing an enemy unit
                System.out.println("Invalid target! " + this.getName() + " can only be cast on friendly units.");
            }
        } else {
            System.out.println("No unit in the target cell!");
        }
        return heal;
    }
}