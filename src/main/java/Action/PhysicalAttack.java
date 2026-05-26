package Action;

import Board.Cell;
import Unit.Unit;

public class PhysicalAttack extends Action {

    public PhysicalAttack(String type, String name, int apCost, int value, int range, boolean friendly) {
        super(type, name, apCost, value, range, friendly);
    }

    @Override
    public int execute(Unit attacker, Cell targetCell) {
        Unit target = targetCell.getUnit();
        int damage = 0;
        if (target != null) {
            if (!attacker.isTargetFriendly(target)) {
                // Damage = Strength - Defense
                damage = this.getValue() + attacker.getStrength() - target.getDefense();
                if (damage < 0) damage = 0;
                this.setValueOnTarget(damage);
            }
            // Deduct AP cost
            attacker.setAP(attacker.getAP() - this.getApCost());
        }
        return damage;
    }
}