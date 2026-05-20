package Unit;

import Action.MagicAttack;
import Controller.Controller;

public class Mage extends Unit {
    public Mage(boolean friendly, Controller unitController) {
        super("Mage", 60, 2, 10, 5, 5, 5, friendly, unitController);
        this.addAction(new MagicAttack("Damage", "Fireball", 3, 10, 3, false));
    }
}