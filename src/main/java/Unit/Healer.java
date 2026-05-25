package Unit;

import Action.Heal;
import Action.MagicAttack;
import Controller.Controller;

public class Healer extends Unit {
    public Healer(boolean friendly, Controller unitController) {
        super("Healer", 60, 2, 15, 4, 15, 5, friendly, unitController);
        this.setMaxMP(3);
        this.setMP(3);
        this.addAction(new Heal("Buff", "Divine Heal", 3, 5, 3, true));
        this.addAction(new MagicAttack("Damage", "Holy Nova", 2, 5, 3, false));
    }
}
