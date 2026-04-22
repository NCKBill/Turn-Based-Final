package Action;

import Board.Cell;
import Unit.Unit;

public class MagicAttack extends Action {

    public MagicAttack(String type, String name, int apCost, int value, int range, boolean friendly) {
        super(type, name, apCost, value, range, friendly);
    }

    @Override
    public int execute(Unit attacker, Cell targetCell) {
        Unit target = targetCell.getUnit();
        int damage = 0;
        if (target != null) {
            if (!attacker.isTargetFriendly(target)) {
                System.out.println(attacker.getName() + " casts " + this.getName() + " on " + target.getName());

                // Damage = Default value + Power - Magic Defense
                damage = this.getValue() + attacker.getPower() - target.getDefenseMagic();
                if (damage < 0) damage = 0;

                System.out.println(target.getName() + " was attacked for " + damage + " HP.");
            } else {
                // prevent player from accidentally attacking a friendly unit
                System.out.println("Invalid target! " + this.getName() + " can only be cast on enemy units.");
            }
            // deduct AP cost
            attacker.setActionPoint(attacker.getActionPoint() - this.getApCost());
        }
        return damage;
    }
}