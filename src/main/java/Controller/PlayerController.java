package Controller;

import Action.Action;
import Board.Cell;
import Unit.Unit;

import java.util.List;

public class PlayerController implements Controller {
    private GameManager gameManager; // Reference to the central game manager that coordinates all game logic

    // Constructor: injects the GameManager so this controller can interact with game state
    public PlayerController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // Called at the start of a player's turn — currently just signals the GUI to wait for input
    @Override
    public void takeTurn(Unit unit) {
        System.out.println("Player's turn for unit: " + unit.getName());
        System.out.println("Waiting for GUI input...");
    }

    // Main input handler — called by the GUI whenever the player clicks a cell on the board
    @Override
    public void handleInput(Cell clickedCell, Unit activeUnit) {
        // Retrieve current game state: any skill the player has selected, and the unit currently highlighted in the view
        Action selectedAction = gameManager.getSelectedAction();
        Unit selectedViewUnit = gameManager.getSelectedViewUnit();

        // ── BRANCH 1: Player clicked an EMPTY cell → treat it as a movement intent ──
        if (clickedCell != null && !clickedCell.isOccupied()) {
            gameManager.setSelectedAction(null); // Clicking empty space cancels any queued skill selection

            // Only proceed with movement if the highlighted unit is the one currently taking its turn
            if (selectedViewUnit != null && selectedViewUnit == activeUnit) {
                // Find which cell the active unit is currently standing on
                Cell startCell = gameManager.getBackendGrid().getCell(selectedViewUnit);

                // Compute all cells the unit can reach given its remaining movement points (MP)
                List<Cell> reachable = gameManager.getBackendGrid().getReachableCells(startCell, selectedViewUnit.getMP());

                if (reachable.contains(clickedCell)) {
                    // Target is within range — calculate the optimal path using Dijkstra's algorithm
                    List<Cell> fullPath = gameManager.getBackendGrid().calculatePathDijkstra(startCell, clickedCell);

                    gameManager.getGUI().clearPathHighlight();                        // Remove any movement preview highlight from the board
                    gameManager.executeMovement(selectedViewUnit, fullPath, () -> {
                    });    // Move the unit along the computed path
                } else {
                    System.out.println("Invalid movement: Target out of range.");
                }
            }
        }

        // ── BRANCH 2: Player has a skill queued AND clicked a cell → treat it as an action (attack/heal) ──
        else if (selectedAction != null) {
            if (clickedCell != null) {

                // Check if the selected action is legally executable from the active unit's cell to the target cell
                // (validates range, line of sight, or any other action-specific rules)
                if (selectedAction.canExecute(gameManager.getBackendGrid().getCell(activeUnit), clickedCell)) {
                    // Valid target — execute the action (e.g. deal damage, apply heal) and log what happened
                    gameManager.handleAction(activeUnit, clickedCell.getUnit(), selectedAction, () -> {
                    });
                } else {
                    // Invalid target — show the action's built-in error/reason message in the log
                    gameManager.getGUI().logMessage(selectedAction.getLogMessage());
                }

                // After any action attempt (success or fail), deselect the skill and refresh the turn UI
                gameManager.setSelectedAction(null);
                gameManager.updateTurnDisplay(activeUnit); // Refresh AP/MP bars, highlights, etc. for the active unit
            }
        }
    }
}