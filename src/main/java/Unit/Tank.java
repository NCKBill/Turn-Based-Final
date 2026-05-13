package Unit;

import Action.*;
import Controller.*;

public class Tank extends Unit {
    public Tank(boolean friendly, Controller unitController) {
        super("Tank", 120, 5, 0, 20, 10, 2, friendly, unitController);
        this.setMaxMP(3);
        this.setMovementPoint(3);
        this.addAction(new PhysicalAttack("Damage", "Smite", 6, 5, 1, false));
        this.addAction(new DefAttack("Damage", "Shield Bash", 6, 5, 1, false));
    }
}