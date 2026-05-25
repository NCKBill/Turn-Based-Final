package Unit;

import Action.DefAttack;
import Action.PhysicalAttack;
import Controller.Controller;

public class Tank extends Unit {
    public Tank(boolean friendly, Controller unitController) {
        super("Tank", 120, 5, 0, 10, 10, 2, friendly, unitController);
        this.addAction(new PhysicalAttack("Damage", "Smite", 2, 15, 1, false));
        this.addAction(new DefAttack("Damage", "Shield Bash", 2, 5, 1, false));
    }
}