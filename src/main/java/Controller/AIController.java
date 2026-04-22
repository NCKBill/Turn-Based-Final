package Controller;

import Board.*;
import Unit.*;
import Action.*;
import java.util.*;

/**
 * For controlling computer controlled units
 * Scan for reachable target
 * Calculate path to take
 */
public class AIController implements Controller{
    private Grid backendGrid;
    private TurnManager turnManager;
    private GameManager gameManager;

    public AIController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.backendGrid = gameManager.getBackendGrid();
        this.turnManager = gameManager.getTurnManager();
    }

    public void takeTurn(Unit aiUnit) {
        System.out.println("AI Controller taking turn for: " + aiUnit.getName());

        // 1. Calculate Path
        List<Cell> aiPath = getAIMovePath(aiUnit);

        // 2. Define what the AI should do AFTER moving (Attack and End Turn)
        Runnable postMovementAction = () -> {
            Cell currentCell = backendGrid.getCell(aiUnit);
            if (currentCell != null) {
                int[][] direction = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] dir : direction) {
                    int r = currentCell.getRow() + dir[0];
                    int c = currentCell.getCol() + dir[1];

                    if (backendGrid.isValidCoordinate(r, c)) {
                        Cell neighbor = backendGrid.getCell(r, c);
                        if (neighbor != null && neighbor.getUnit() != null) {
                            Unit target = neighbor.getUnit();
                            if (!aiUnit.isTargetFriendly(target) && !aiUnit.isExhausted()) {
                                for (Action action : aiUnit.getAvailableActions()) {
                                    if (action.getType().equals("Damage")) {
                                        System.out.println("Enemy attacks!");
                                        int dmg = action.execute(aiUnit, neighbor);
                                        gameManager.handleDamage(target, dmg);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // End turn after attacking
            turnManager.endCurrentTurn();
            gameManager.processNextTurn();
        };

        // 3. Execute movement
        if (aiPath != null && !aiPath.isEmpty()) {
            // Start animation and pass the attack logic as a callback
            gameManager.executeMovement(aiUnit, aiPath, postMovementAction);
        } else {
            // If the AI doesn't need to move, execute the attack logic immediately
            postMovementAction.run();
        }
    }

    @Override
    public void handleInput(Cell clickedCell, Unit activeUnit) {
        // Do nothing
    }

    /**
     * Path for unit according to its maximum MP
     * @param AIUnit Current AI unit
     * @return List of Cells representing path
     */
    public List<Cell> getAIMovePath(Unit AIUnit) {
        List<Cell> bestPath = findBestPathToTarget(AIUnit);

        if (bestPath.isEmpty()) return new ArrayList<>();

        // AI step onto the Cell adjacent to the target
        if (bestPath.getLast().getUnit() != null) {
            bestPath.removeLast();
        }

        int moveLimit = Math.min(bestPath.size(), AIUnit.getMaxMP());
        return bestPath.subList(0, moveLimit);
    }

    /**
     * Calculated path to nearest reachable target
     * @param AIUnit current unit
     * @return List of Cells representing path to nearest reachable target
     */
    private List<Cell> findBestPathToTarget(Unit AIUnit) {
        Cell startCell = backendGrid.getCell(AIUnit);
        List<Cell> shortestPath = new ArrayList<>();
        int minPathLength = Integer.MAX_VALUE;

        for (Unit targetUnit : turnManager.getAllActiveUnits()) {
            if (!AIUnit.isTargetFriendly(targetUnit)) {
                Cell targetCell = backendGrid.getCell(targetUnit);
                if (targetCell != null) {
                    // Calculate path
                    // If path is valid and shorter than previous best, update
                    List<Cell> currentPath = backendGrid.calculatePathDijkstra(startCell, targetCell);

                    if (!currentPath.isEmpty() && currentPath.size() < minPathLength) {
                        minPathLength = currentPath.size();
                        shortestPath = currentPath;
                    }
                }
            }
        }
        return shortestPath;
    }
}