package Unit;

import Action.*;
import Controller.*;

public class Tank extends Unit {
    public Tank(boolean friendly, Controller unitController) {
        super("Tank", 120, 15, 0, 10, 5, 2, friendly, unitController);
        this.addAction(new PhysicalAttack("Damage", "Smite", 6, 5, 1, false));
        this.addAction(new DefAttack("Damage", "Shield Bash", 6, 5, 1, false));
    }
}