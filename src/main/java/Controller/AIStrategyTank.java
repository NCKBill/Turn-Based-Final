package Controller;

import Action.Action;
import Board.Cell;
import Unit.Unit;
import nckbill.turnbasedfinal.GameGUI;

import java.util.ArrayList;
import java.util.List;

public class AIStrategyTank implements AIStrategy {
    @Override
    public void executeTurn(Unit self, List<Unit> allUnits, GameManager gm) {
        List<Cell> pathToTarget = findBestPathToTarget(self, allUnits, gm);

        Runnable postMovementAction = () -> {
            // Check if already attack
            boolean attacked = attackAdjacentEnemies(self, gm);

            if (attacked) {
                // Delay before ending turn
                gm.getGUI().delayExecution(1 / GameGUI.gameSpeed, () -> {
                    gm.getTurnManager().endCurrentTurn();
                    gm.processNextTurn();
                });
            } else {
                // If no attack, end turn
                gm.getTurnManager().endCurrentTurn();
                gm.processNextTurn();
            }
        };

        if (!pathToTarget.isEmpty()) {
            gm.executeMovement(self, pathToTarget, postMovementAction);
        } else {
            postMovementAction.run();
        }
    }

    private boolean attackAdjacentEnemies(Unit currentUnit, GameManager gm) {
        Cell currentCell = gm.getBackendGrid().getCell(currentUnit);
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            Cell neighbor = gm.getBackendGrid().getCell(currentCell.getRow() + dir[0], currentCell.getCol() + dir[1]);
            if (neighbor != null && neighbor.getUnit() != null && !currentUnit.isTargetFriendly(neighbor.getUnit())) {
                for (Action action : currentUnit.getAvailableActions()) {
                    if (action.getType().equals("Damage"))
                        if (action.canExecute(currentCell, neighbor)) {
                            gm.handleAction(currentUnit, neighbor.getUnit(), action);
                            gm.getGUI().logMessage(action.setLogAction(currentUnit, neighbor.getUnit()));
                            return true;
                        }
                }
            }
        }
        return false;
    }

    private List<Cell> findBestPathToTarget(Unit self, List<Unit> allUnits, GameManager gm) {
        Cell startCell = gm.getBackendGrid().getCell(self);
        List<Cell> bestPath = new ArrayList<>();
        int minPathLength = Integer.MAX_VALUE;

        // Find the shortest path to the closest enemy
        for (Unit target : allUnits) {
            if (self.isTargetFriendly(target)) continue;

            Cell targetCell = gm.getBackendGrid().getCell(target);
            if (targetCell == null) continue;

            List<Cell> currentPath = gm.getBackendGrid().calculatePathDijkstra(startCell, targetCell);

            // Keep the shortest valid path
            if (!currentPath.isEmpty() && currentPath.size() < minPathLength) {
                minPathLength = currentPath.size();

                // Move adjacent to enemy unit instead of directly on top of them
                if (currentPath.getLast().getUnit() != null) {
                    currentPath.removeLast();
                }
                bestPath = currentPath;
            }
        }

        // Limit path based on MP cost of terrain
        if (bestPath.isEmpty()) {
            return bestPath;
        }

        int accumulatedCost = 0;
        int validSteps = 1;

        for (int i = 1; i < bestPath.size(); i++) {
            accumulatedCost += bestPath.get(i).getTerrainCost();

            if (accumulatedCost <= self.getMP()) {
                validSteps++;
            } else {
                break;
            }
        }

        return bestPath.subList(0, validSteps);
    }
}