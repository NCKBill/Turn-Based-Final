package Action;

import Board.Cell;
import Unit.Unit;

public class DefAttack extends Action{
    public DefAttack(String type, String name, int apCost, int value, int range, boolean friendly) {
        super(type, name, apCost, value, range, friendly);
    }

    @Override
    public int execute(Unit currentUnit, Cell targetCell) {
        Unit target = targetCell.getUnit();
        int damage = 0;
        if (target != null) {
            if (!currentUnit.isTargetFriendly(target)) {

                // Damage = Default value + Attacker Defense - Target Defense
                damage = this.getValue() + currentUnit.getDefense() - target.getDefense();

                if (damage < 0) damage = 0;
                this.setValueOnTarget(damage);
            }
            // Deduct AP cost
            currentUnit.setAP(currentUnit.getAP() - this.getApCost());
        }
        return damage;
    }
}