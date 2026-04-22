package Controller;

import Unit.*;

import java.util.*;

public class TurnManager {
    private Queue<UnitWrap> movementQueue;
    private Random dice;
    private Unit activeUnit;
    private List<Unit> allActiveUnits;

    public TurnManager() {
        this.movementQueue = new PriorityQueue<>((a, b) -> b.getRoll() - a.getRoll());
        this.dice = new Random();
        this.allActiveUnits = new ArrayList<>();
    }

    public void calculateTurnOrder(List<Unit> allActiveUnits) {
        this.allActiveUnits = allActiveUnits;
        movementQueue.clear();
        for (Unit unit : allActiveUnits) {
            unit.setMovementPoint(unit.getMaxMP()); // Restores movement points
            unit.setActionPoint(unit.getMaxAP());   // Restores action points

            int initRoll = unit.getInitiation() > 0 ? dice.nextInt(unit.getInitiation()) : 0;
            int roll = (dice.nextInt(10) + 1) + initRoll;
            movementQueue.add(new UnitWrap(unit, roll));
        }

        // Display turn order into console
        Queue<UnitWrap> queue = new PriorityQueue<>(movementQueue);
        System.out.println("Initiation roll: ");
        while (!queue.isEmpty()) {
            UnitWrap unitWrap = queue.poll();
            Unit currentUnit = unitWrap.getUnit();
            int roll = unitWrap.getRoll();
            System.out.print(currentUnit.getName() + ": " + roll);
            if (!queue.isEmpty())
                System.out.print(" -> ");
        }

        System.out.println();
    }

    public Unit getNextUnit() {
        if (!movementQueue.isEmpty()) {
            return movementQueue.poll().getUnit();
        }
        return null;
    }

    public void startNextTurn() {
        activeUnit = getNextUnit();
        if (activeUnit == null) {
            if (!allActiveUnits.isEmpty()) {
                calculateTurnOrder(this.allActiveUnits);
                startNextTurn();
            }
        }
    }

    public void endCurrentTurn() {
        if (activeUnit != null) {
            activeUnit.setActionPoint(0);
            activeUnit = null;
        }
        startNextTurn();
    }

    public Unit getActiveUnit() {
        return activeUnit;
    }

    public Queue<Unit> getTurnQueue() {
        Queue<Unit> arr = new LinkedList<>();
        for (UnitWrap uw : movementQueue) arr.offer(uw.getUnit());
        return arr;
    }

    public void removeUnit(Unit unit) {
        movementQueue.removeIf(unitWrap -> unitWrap.getUnit() == unit);
        allActiveUnits.remove(unit);
        System.out.println(unit.getName() + " removed.");
    }


    public List<Unit> getAllActiveUnits() {
        return allActiveUnits;
    }

    public void setAllActiveUnits(List<Unit> allActiveUnits) {
        this.allActiveUnits = allActiveUnits;
    }

    private class UnitWrap {
        private Unit unit;
        private int roll;

        UnitWrap(Unit unit, int roll) {
            this.unit = unit;
            this.roll = roll;
        }

        public Unit getUnit() {
            return unit;
        }

        public int getRoll() {
            return roll;
        }
    }
}