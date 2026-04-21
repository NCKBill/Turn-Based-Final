package Unit;

import Action.Heal;
import Action.MagicAttack;
import Controller.Controller;

public class Healer extends Unit {
    public Healer(boolean friendly, Controller unitController) {
        super("Healer", 60, 2, 20, 4, 15, 5, friendly, unitController);
        this.addAction(new MagicAttack("Damage", "Holy Nova", 2, 2, 3, false));
        this.addAction(new Heal("Buff", "Divine Heal", 2, 5, 3, true));
    }
}
