package Controller;

import Action.Action;
import Board.Cell;
import Board.Grid;
import Unit.Unit;
import nckbill.turnbasedfinal.GameGUI;
import nckbill.turnbasedfinal.UI.CellUI;

import java.util.List;

/**
 * This class handle:
 * Starting & ending game
 * Processing and ending turns
 * Handling player's click on cells
 * Handle damage & death
 */
public class GameManager {
    private Grid backendGrid;
    private TurnManager turnManager;
    private final GameGUI gui;

    private Action selectedAction;
    private Unit selectedViewUnit;
    private int allyCountCurrent = 0;
    private int enemiesCountCurrent = 0;

    public GameManager(GameGUI gui, int rows, int cols) {
        this.gui = gui;
        this.backendGrid = new Grid(rows, cols);
        this.turnManager = new TurnManager(this);
    }

    public void startGame(List<Unit> allActiveUnits) {
        allyCountCurrent = 0;
        enemiesCountCurrent = 0;
        gui.logMessage("Game Start.");
        for (Unit allActiveUnit : allActiveUnits) {
            if (allActiveUnit.isFriendly())
                allyCountCurrent++;
            else
                enemiesCountCurrent++;
        }
        turnManager.calculateTurnOrder(allActiveUnits);
        turnManager.startNextTurn();
        gui.refreshVisualGrid();
        processNextTurn();
    }

    /**
     * Take turn for AI units and Player units
     */
    public void processNextTurn() {
        if (turnManager.endGame) {
            return;
        }
        Unit activeUnit = turnManager.getActiveUnit();

        if (activeUnit == null) {
            turnManager.startNextTurn();
            activeUnit = turnManager.getActiveUnit();
        }

        if (activeUnit == null) return;


        final Unit currentUnit = activeUnit;

        boolean isPlayer = activeUnit.getUnitController().getClass().getSimpleName().equals("PlayerController");

        javafx.application.Platform.runLater(() -> {
            gui.updateTurnDisplay(currentUnit);
            if (!isPlayer)
                gui.logMessage(currentUnit.getName() + "'s Turn.");
            else {
                for (int i = 0; i < 5; i++) {
                    gui.logMessage("IT IS YOUR TURN! TAKE IT RIGHT NOW!!!!!!");
                }
            }
        });

        gui.delayExecution(1 / GameGUI.getGameSpeed(), () -> {
            currentUnit.performAction();
        });
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

        // Ignore clicks during AI turn
        if (activeUnit != null && !activeUnit.isFriendly()) {
            return;
        }

        Cell clickedCell = backendGrid.getCell(row, col);

        if (clickedCell != null && clickedCell.getUnit() != null && selectedAction == null) {
            selectedViewUnit = clickedCell.getUnit();
            gui.updateSidebarUnitStats(selectedViewUnit);
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

    public void handleAction(Unit currentUnit, Unit target, Action action, Runnable onComplete) {
        if (currentUnit == null || currentUnit.getHP() <= 0) return;
        if (target == null || target.getHP() <= 0) return;

        Cell attackerCell = getBackendGrid().getCell(currentUnit);
        Cell targetCell = getBackendGrid().getCell(target);

        CellUI attackerCellUI = null;
        CellUI targetCellUI = null;

        if (attackerCell != null && targetCell != null) {
            attackerCellUI = gui.getGrid()[attackerCell.getRow()][attackerCell.getCol()];
            targetCellUI = gui.getGrid()[targetCell.getRow()][targetCell.getCol()];
        }

        // Apply Logic — execute first so valueOnTarget is set before logging
        int damage = action.execute(currentUnit, targetCell);

        gui.logMessage(action.setLogAction(currentUnit, target));

        if (action.getType().equals("Damage"))
            handleDamage(target, damage);
        else
            handleHeal(target, action.getValue() + currentUnit.getPower());

        // Pass the callback to the GUI
        if (attackerCellUI != null && targetCellUI != null) {
            gui.executeUnitAnimation(attackerCellUI, targetCellUI, onComplete);
        } else if (onComplete != null) {
            onComplete.run(); // Run instantly if there's no UI
        }
    }

    // Call when damage is applied
    public void handleDamage(Unit target, int damage) {
        target.setHP(target.getHP() - damage);
        // Whenever a target takes damage or is healed
        Cell targetCell = getBackendGrid().getCell(target);
        CellUI targetCellUI = gui.getGrid()[targetCell.getRow()][targetCell.getCol()];

        if (targetCellUI != null && targetCellUI.getUnitUI() != null) {
            targetCellUI.getUnitUI().updateHP();
        }
        // Check for Death
        if (target.getHP() <= 0) {
            if (target.isFriendly())
                allyCountCurrent--;
            else
                enemiesCountCurrent--;

            gui.logMessage(target.getName() + " was murdered.");

            backendGrid.getCell(target).setUnit(null); // remove from grid
            turnManager.removeUnit(target); // remove from queue

            gui.refreshVisualGrid(); // remove from gui grid
            gui.updateTurnDisplay(turnManager.getActiveUnit()); // remove from top bar

            if (enemiesCountCurrent <= 0)
                handleEnd("VICTORY!");
            else if (allyCountCurrent <= 0) {
                handleEnd("DEFEAT!");
            }
        }
    }

    public void handleHeal(Unit target, int heal) {
        int potentialHP = target.getHP() + heal;
        target.setHP(Math.min(potentialHP, target.getMaxHP()));

        Cell targetCell = getBackendGrid().getCell(target);
        CellUI targetCellUI = gui.getGrid()[targetCell.getRow()][targetCell.getCol()];

        if (targetCellUI != null && targetCellUI.getUnitUI() != null) {
            targetCellUI.getUnitUI().updateHP();
        }
    }

    public boolean isMatchOver() {
        return turnManager.endGame;
    }

    private void handleEnd(String message) {
        System.out.println(message);
        gui.showGameOver(message);
        turnManager.endGame = true;
    }

    public void endPlayerTurn() {
        turnManager.endCurrentTurn();
        processNextTurn();
    }

    public void resetGame() {
        backendGrid = new Grid(backendGrid.getRows(), backendGrid.getColumns());
        gui.initializeGrid();
        turnManager = new TurnManager(this);

        this.allyCountCurrent = 0;
        this.enemiesCountCurrent = 0;
        this.selectedAction = null;
        this.selectedViewUnit = null;

        gui.refreshVisualGrid();
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

    public GameGUI getGUI() {
        return gui;
    }
}