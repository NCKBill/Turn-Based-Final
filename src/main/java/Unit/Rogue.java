package Unit;

import Action.DefAttack;
import Action.PhysicalAttack;
import Controller.Controller;

public class Rogue extends Unit{
    public Rogue(boolean friendly, Controller unitController) {
        // super(name, HP, STR, POW, DEF, MAG DEF, INIT, friendly, controller)
        super("Rogue", 70, 30, 0, 5, 5, 5, friendly, unitController);
        this.setMP(5);
        this.setMaxMP(5);
        this.addAction(new PhysicalAttack("Damage", "Assassinate", 6, 5, 1, false));
        this.addAction(new DefAttack("Damage", "Dagger Spray", 6, 3, 3, false));
    }
}
