package Unit;

import Action.Action;
import Controller.Controller;

import java.util.ArrayList;
import java.util.List;

public abstract class Unit {
    private String name;
    private int HP;
    private int AP; // "energy" per turn
    private int MP; // cells the unit can move a turn
    private int maxHP; // for resetting turn
    private int maxMP; // for resetting turn
    private int maxAP; // for resetting turn
    private int strength; // Physical Damage = Base dmg + strength
    private int power; // Magic Damage = Spell dmg + power
    private int defense; // HP = HP - (Physical Damage - defense > 0)
    private int defenseMagic; // HP = HP - (Magic Damage - defenseMagic > 0)
    private int initiation; // increase chance to go first
    private boolean friendly; // true for friendly, false for enemy
    private Controller unitController;
    private List<Action> availableActions; // List to hold skills/actions

    public String getImagePath() {
        String friendly = isFriendly() ? "ally" : "enemy";
        String unitName = this.getClass().getSimpleName().toLowerCase();
        return "/assets/units/" + friendly + "-" + unitName + ".png";
    }

    public Unit(String name, int HP, int strength, int power, int defense, int defenseMagic, int initiation, boolean friendly, Controller unitController) {
        String temp = " Ally";
        if (!friendly)
            temp = " Enemy";
        this.name = name + temp;
        this.HP = HP;
        this.AP = 6;
        this.MP = 2;
        this.strength = strength;
        this.power = power;
        this.defense = defense;
        this.defenseMagic = defenseMagic;
        this.initiation = initiation;
        this.friendly = friendly;
        this.unitController = unitController;
        this.availableActions = new ArrayList<>();
        this.maxHP = HP;
        this.maxAP = AP;
        this.maxMP = MP;
    }

    // Triggers the turn logic via the assigned Controller
    public void performAction() {
        if (unitController != null) {
            unitController.takeTurn(this);
        }
    }

    public void addAction(Action action) {
        this.availableActions.add(action);
    }
    public void setAvailableActions(List<Action> availableActions) {
        this.availableActions = availableActions;
    }
    public List<Action> getAvailableActions() { return availableActions; }

    public int getHP() {
        return HP;
    }

    public void setHP(int HP) {
        this.HP = HP;
    }

    public int getAP() {
        return AP;
    }

    public void setAP(int AP) {
        this.AP = AP;
    }
    public boolean isExhausted () {
        if (this.getAP() <= 0) {
            System.out.println("Unit is exhausted.");
            return true;
        }

        for (Action action: availableActions) {
            if (action.getApCost() <= this.getAP())
                return false;
        }
        System.out.println("Unit is exhausted.");
        return true;
    }

    public int getMP() {
        return MP;
    }

    public void setMP(int MP) {
        this.MP = MP;
    }

    public int getInitiation() { return initiation; }
    public void setInitiation(int initiation) { this.initiation = initiation; }

    public int getStrength() {
        return strength;
    }
    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getPower() {
        return power;
    }
    public void setPower(int power) {
        this.power = power;
    }

    public int getDefense() {
        return defense;
    }
    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getDefenseMagic() {
        return defenseMagic;
    }
    public void setDefenseMagic(int defenseMagic) {
        this.defenseMagic = defenseMagic;
    }

    public boolean isFriendly() {
        return friendly;
    }
    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setUnitController(Controller unitController) {
        this.unitController = unitController;
    }
    public Controller getUnitController() { return unitController; }

    /**
     * Check if the target is on the same team as the current unit
     * @param target target to check
     * @return boolean value if target is on the same team
     */
    public boolean isTargetFriendly(Unit target) {
        if (target != null) {
            return this.isFriendly() == target.isFriendly();
        }
        return false;
    }

    public int getMaxMP() {
        return maxMP;
    }
    public void setMaxMP(int maxMP) {
        this.maxMP = maxMP;
    }

    public int getMaxAP() {
        return maxAP;
    }
    public void setMaxAP(int maxAP) {
        this.maxAP = maxAP;
    }

    public int getMaxHP() {
        return maxHP;
    }
    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    @Override
    public String toString() {
        return
            "Name: " + name +
                    "\nHP: " + HP + " / " + maxHP +
                    "\nAP: " + AP + " / " + maxAP +
                    "\nMP: " + MP + " / " + maxMP +
            "\nSTR: " + strength +
            "\nPOW: " + power +
            "\nDEF: " + defense +
            "\nMAG DEF: " + defenseMagic +
            "\nINIT: " + initiation;
    }
}