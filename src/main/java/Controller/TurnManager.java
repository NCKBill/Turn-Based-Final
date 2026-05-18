package Controller;

import Unit.Unit;

import java.util.*;

public class TurnManager {
    private final Queue<UnitWrap> movementQueue;
    private final Random dice;
    private Unit activeUnit;
    private List<Unit> allActiveUnits;
    public boolean endGame = false;
    public TurnManager() {
        this.movementQueue = new PriorityQueue<>((a, b) -> b.roll() - a.roll());
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
            Unit currentUnit = unitWrap.unit();
            int roll = unitWrap.roll();
            System.out.print(currentUnit.getName() + ": " + roll);
            if (!queue.isEmpty())
                System.out.print(" -> ");
        }

        System.out.println();
    }

    public Unit getNextUnit() {
        if (!movementQueue.isEmpty()) {
            return movementQueue.poll().unit();
        }
        return null;
    }

    public void startNextTurn() {
        while (activeUnit == null && !allActiveUnits.isEmpty() && !endGame) {
            activeUnit = getNextUnit();
            if (activeUnit == null) {
                calculateTurnOrder(this.allActiveUnits);
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
        for (UnitWrap uw : movementQueue) arr.offer(uw.unit());
        return arr;
    }

    public void removeUnit(Unit unit) {
        movementQueue.removeIf(unitWrap -> unitWrap.unit() == unit);
        allActiveUnits.remove(unit);
        System.out.println(unit.getName() + " removed.");
    }

    public List<Unit> getAllActiveUnits() {
        return allActiveUnits;
    }

    private record UnitWrap(Unit unit, int roll) {
    }
}