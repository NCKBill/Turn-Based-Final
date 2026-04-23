package Controller;

import Board.Cell;
import Board.Grid;
import Unit.Unit;
import Action.*;
import nckbill.turnbasedfinal.*;

import java.util.List;

public class GameManager {
    private Grid backendGrid;
    private TurnManager turnManager;
    private GameGUI gui;

    private Action selectedAction;
    private Unit selectedViewUnit;

    public GameManager(GameGUI gui, int rows, int cols) {
        this.gui = gui;
        this.backendGrid = new Grid(rows, cols);
        this.turnManager = new TurnManager();
    }

    public void startGame(List<Unit> allActiveUnits) {
        turnManager.calculateTurnOrder(allActiveUnits);
        turnManager.startNextTurn();
        processNextTurn();
    }

    /**
     * Take turn for AI units and Player units
     */
    public void processNextTurn() {
        Unit activeUnit = turnManager.getActiveUnit();
        if (activeUnit == null) {
            turnManager.startNextTurn();
            activeUnit = turnManager.getActiveUnit();
        }

        // Tell GUI to update the turn display
        gui.updateTurnDisplay(activeUnit);

        if (activeUnit != null) {
            // If AI, calculates paths and attacks
            // If Player, yields to the JavaFX GUI
            activeUnit.performAction();
        }
    }

    public void executeMovement(Unit movingUnit, List<Cell> path, Runnable onComplete) {
        gui.executeMovement(movingUnit, path, onComplete);
    }

    /**
     * GUI passes coordinates
     * Method decides what to do
     */
    public void handleCellClick(int row, int col) {
        Unit activeUnit = turnManager.getActiveUnit();
        Cell clickedCell = backendGrid.getCell(row, col);

        // Allow players to view stats by clicking a unit
        if (clickedCell != null && clickedCell.getUnit() != null && selectedAction == null) {
            selectedViewUnit = clickedCell.getUnit();
            gui.updateSidebarStats(selectedViewUnit);
        }

        if (activeUnit != null && activeUnit.getUnitController() != null) {
            activeUnit.getUnitController().handleInput(clickedCell, activeUnit);
        }
    }

    public Action getSelectedAction() {
        return selectedAction;
    }

    public void updateTurnDisplay(Unit unit) {
        gui.updateTurnDisplay(unit);
    }

    // Call this whenever damage is applied
    public void handleDamage(Unit target, int damage) {
        target.setHealthPoint(target.getHealthPoint() - damage);

        // Check for Death
        if (target.getHealthPoint() <= 0) {
            backendGrid.getCell(target).setUnit(null); // remove from grid
            turnManager.removeUnit(target); // remove from queue
            gui.refreshVisualGrid(); // remove from gui grid
            gui.updateTurnDisplay(turnManager.getActiveUnit()); // remove from top bar
        }
    }

    public void handleHeal(Unit target, int heal) {
        int potentialHP = target.getHealthPoint() + heal;
        target.setHealthPoint(Math.min(potentialHP, target.getMaxHP()));
    }

    public void endPlayerTurn() {
        turnManager.endCurrentTurn();
        processNextTurn();
    }

    public Grid getBackendGrid() {
        return backendGrid;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public void setSelectedAction(Action action) {
        this.selectedAction = action;
    }

    public Unit getSelectedViewUnit() {
        return selectedViewUnit;
    }

    public void setSelectedViewUnit(Unit unit) {
        this.selectedViewUnit = unit;
    }
}