package Controller;

import Board.Cell;
import Unit.Unit;
import Action.Action;
import java.util.List;

public class PlayerController implements Controller {
    private GameManager gameManager;

    public PlayerController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void takeTurn(Unit unit) {
        System.out.println("Player's turn for unit: " + unit.getName());
        System.out.println("Waiting for GUI input...");
    }

    @Override
    public void handleInput(Cell clickedCell, Unit activeUnit) {
        Action selectedAction = gameManager.getSelectedAction();
        Unit selectedViewUnit = gameManager.getSelectedViewUnit();

        // Player clicked an empty cell (Handling Movement)
        if (clickedCell != null && clickedCell.getUnit() == null) {
            if (selectedViewUnit != null && selectedViewUnit == activeUnit) {
                Cell startCell = gameManager.getBackendGrid().getCell(selectedViewUnit);
                List<Cell> reachable = gameManager.getBackendGrid().getReachableCells(startCell, selectedViewUnit.getMovementPoint());

                if (reachable.contains(clickedCell)) {
                    List<Cell> fullPath = gameManager.getBackendGrid().calculatePathDijkstra(startCell, clickedCell);

                    // UPDATED: Using animated movement instead of instant teleportation.
                    // Passing 'null' because the player manually chooses their post-movement actions.
                    gameManager.executeMovement(selectedViewUnit, fullPath, null);
                } else {
                    System.out.println("Invalid movement: Target out of range.");
                }
            }
        }
        // Player clicked an occupied cell with a skill queued up (Handling Attacks/Heals)
        else if (selectedAction != null) {
            if (!activeUnit.isExhausted()) {
                selectedAction.execute(activeUnit, clickedCell);
                gameManager.setSelectedAction(null);       // Deselect skill after using it
                gameManager.updateTurnDisplay(activeUnit); // Refresh UI to show damage
            }
        }
    }
}