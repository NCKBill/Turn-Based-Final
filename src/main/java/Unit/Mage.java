package Unit;

import Action.MagicAttack;
import Controller.Controller;

public class Mage extends Unit {
    public Mage(boolean friendly, Controller unitController) {
        super("Mage", 60, 2, 10, 5, 5, 5, friendly, unitController);
        this.setMaxMP(3);
        this.setMP(3);
        this.addAction(new MagicAttack("Damage", "Fireball", 3, 15, 1, false));
        this.addAction(new MagicAttack("Damage", "Snowball", 3, 10, 3, false));
    }
}