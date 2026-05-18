package Unit;

import Action.DefAttack;
import Action.PhysicalAttack;
import Controller.Controller;

public class Tank extends Unit {
    public Tank(boolean friendly, Controller unitController) {
        super("Tank", 100, 5, 0, 10, 10, 2, friendly, unitController);
        this.setMaxMP(3);
        this.setMovementPoint(3);
        this.addAction(new PhysicalAttack("Damage", "Smite", 3, 15, 1, false));
        this.addAction(new DefAttack("Damage", "Shield Bash", 3, 5, 1, false));
    }
}