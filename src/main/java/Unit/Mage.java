package Unit;

import Action.*;
import Controller.Controller;

public class Mage extends Unit {
    public Mage(boolean friendly, Controller unitController) {
        super("Mage", 60, 2, 20, 4, 15, 5, friendly, unitController);
        this.addAction(new MagicAttack("Damage", "Fireball", 2, 5, 3, false));
    }
}