package Controller;

import Board.Cell;
import Unit.Unit;
import Action.Action;
import java.util.List;
import java.util.ArrayList;

public class AIStrategyTank implements AIStrategy {
    @Override
    public void executeTurn(Unit self, List<Unit> allUnits, GameManager gm) {
        // Find the best reachable target
        List<Cell> pathToTarget = findBestPathToTarget(self, allUnits, gm);

        Runnable postMovementAction = () -> {
            attackAdjacentEnemies(self, gm);
            gm.getTurnManager().endCurrentTurn();
            gm.processNextTurn();
        };

        // Move or execute immediate attack if already in range
        if (!pathToTarget.isEmpty()) {
            gm.executeMovement(self, pathToTarget, postMovementAction);
        } else {
            postMovementAction.run();
        }
    }

    private List<Cell> findBestPathToTarget(Unit self, List<Unit> allUnits, GameManager gm) {
        Cell startCell = gm.getBackendGrid().getCell(self);
        List<Cell> bestPath = new ArrayList<>();
        int minPathLength = Integer.MAX_VALUE;

        for (Unit target : allUnits) {
            if (self.isTargetFriendly(target)) continue;

            Cell targetCell = gm.getBackendGrid().getCell(target);
            if (targetCell == null) continue;

            List<Cell> currentPath = gm.getBackendGrid().calculatePathDijkstra(startCell, targetCell);

            // If path valid and shorter, keep it [cite: 56]
            if (!currentPath.isEmpty() && currentPath.size() < minPathLength) {
                minPathLength = currentPath.size();
                // Remove the target cell so we stop adjacent to them
                if (currentPath.get(currentPath.size() - 1).getUnit() != null) {
                    currentPath.remove(currentPath.size() - 1);
                }
                bestPath = currentPath;
            }
        }
        // Limit path to movement points
        int moveLimit = Math.min(bestPath.size(), self.getMovementPoint());
        return bestPath.subList(0, moveLimit);
    }

    private void attackAdjacentEnemies(Unit self, GameManager gm) {
        Cell currentCell = gm.getBackendGrid().getCell(self);
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            Cell neighbor = gm.getBackendGrid().getCell(currentCell.getRow() + dir[0], currentCell.getCol() + dir[1]);
            if (neighbor != null && neighbor.getUnit() != null && !self.isTargetFriendly(neighbor.getUnit())) {
                for (Action action : self.getAvailableActions()) {
                    if (action.getType().equals("Damage") && action.canExecute(self, currentCell, neighbor)) {
                        int damage = action.execute(self, neighbor);
                        gm.handleDamage(neighbor.getUnit(), damage);
                        return;
                    }
                }
            }
        }
    }
}