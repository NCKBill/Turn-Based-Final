package Controller;

import Action.Action;
import Board.Cell;
import Board.Grid;
import Unit.Unit;

import java.util.Arrays;
import java.util.List;

public class AIStrategyRanged implements AIStrategy {
    private static final int SAFE_DISTANCE = 3; // minimum range for skills

    @Override
    public void executeTurn(Unit currentUnit, List<Unit> allUnits, GameManager gm) {

        // Use a loop to process sequential instant actions without stacking frames
        while (true) {
            // Base Case
            if (currentUnit.getAP() <= 0 && currentUnit.getMP() <= 0) {
                endTurn(gm);
                return;
            }

            // Check for support action
            Action supportAction = findSupportAction(currentUnit);
            if (supportAction != null && currentUnit.getAP() >= supportAction.getApCost()) {
                Unit bestFriendlyTarget = findBestFriendlyTarget(currentUnit, supportAction, gm);

                if (bestFriendlyTarget != null) {
                    Cell targetCell = gm.getBackendGrid().getCell(bestFriendlyTarget);
                    supportAction.execute(currentUnit, targetCell);
                    gm.getGUI().logMessage(supportAction.setLogAction(currentUnit, targetCell.getUnit()));
                    continue;
                }
            }

            // Identify Target
            Unit target = findClosestEnemy(currentUnit, allUnits, gm);
            if (target == null) {
                endTurn(gm);
                return;
            }

            Cell selfCell = gm.getBackendGrid().getCell(currentUnit);
            Cell targetCell = gm.getBackendGrid().getCell(target);
            double dist = getDistance(selfCell, targetCell);

            // Kite Phase
            if (dist < SAFE_DISTANCE && currentUnit.getMP() > 0) {
                Cell retreatCell = calculateRetreatCell(selfCell, targetCell, gm);
                if (retreatCell != null) {
                    gm.executeMovement(currentUnit, Arrays.asList(selfCell, retreatCell), () -> {
                        executeTurn(currentUnit, allUnits, gm);
                    });
                    return;
                }
            }

            // Attack Phase
            boolean attackedThisCycle = false;
            for (Action action : currentUnit.getAvailableActions()) {
                if (action.getType().equals("Damage")) {
                    if (action.canExecute(selfCell, targetCell)) {
                        performAttack(currentUnit, target, gm);
                        attackedThisCycle = true;
                        break;
                    }
                }
            }

            if (attackedThisCycle) {
                continue;
            }

            // Movement Phase
            if (!hasActionInRange(currentUnit, targetCell, gm)) {
                if (currentUnit.getMP() > 0) {
                    List<Cell> path = gm.getBackendGrid().calculatePathDijkstra(selfCell, targetCell);
                    if (path.size() > 1) {
                        int maxSafeSize = Math.max(1, path.size() - SAFE_DISTANCE + 1);
                        int moveLimit = Math.min(maxSafeSize, currentUnit.getMP() + 1);

                        if (moveLimit > 1) {
                            List<Cell> pathToTake = path.subList(0, moveLimit);
                            gm.executeMovement(currentUnit, pathToTake, () -> {
                                executeTurn(currentUnit, allUnits, gm);
                            });
                            return;
                        }
                    }
                }
            }

            // Phase 6: End loop if no actions or moves are viable
            endTurn(gm);
            return;
        }
    }

    private void performAttack(Unit currentUnit, Unit target, GameManager gm) {
        for (Action action : currentUnit.getAvailableActions()) {
            if (action.getType().equals("Damage")) {
                gm.handleAction(currentUnit, target, action);

                gm.getGUI().logMessage(action.setLogAction(currentUnit, target));
                break;
            }
        }
    }

//    private void logFailedAction(GameManager gm, Action action) {
//        gm.getGUI().logMessage(action.getLogMessage());
//    }

    private boolean hasActionInRange(Unit currentUnit, Cell targetCell, GameManager gm) {
        List<Action> availableActions = currentUnit.getAvailableActions();
        Cell currentCell = gm.getBackendGrid().getCell(currentUnit);

        for (Action availableAction : availableActions) {
            if (availableAction.isInRange(currentCell, targetCell)) {
                return true;
            }
        }
        return false;
    }

    private void endTurn(GameManager gm) {
        gm.getTurnManager().endCurrentTurn();
        gm.processNextTurn();
    }

    private Action findSupportAction(Unit unit) {
        return unit.getAvailableActions().stream()
                .filter(Action::isTargetFriendly)
                .findFirst()
                .orElse(null);
    }

    private Cell calculateRetreatCell(Cell currentUnit, Cell target, GameManager gm) {
        Grid grid = gm.getBackendGrid();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        Cell bestRetreat = null;
        double maxDist = getDistance(currentUnit, target);

        for (int[] dir : directions) {
            int nextR = currentUnit.getRow() + dir[0];
            int nextC = currentUnit.getCol() + dir[1];
            Cell candidate = grid.getCell(nextR, nextC);

            if (candidate != null && !candidate.isOccupied()) {
                double dist = getDistance(candidate, target);

                if (dist > maxDist) {
                    maxDist = dist;
                    bestRetreat = candidate;
                }
            }
        }
        return bestRetreat;
    }

    private Unit findClosestEnemy(Unit currentUnit, List<Unit> allUnits, GameManager gm) {
        Unit closest = null;
        double min = Double.MAX_VALUE;
        Cell selfCell = gm.getBackendGrid().getCell(currentUnit);

        for (Unit u : allUnits) {
            if (!currentUnit.isTargetFriendly(u)) {
                double d = getDistance(selfCell, gm.getBackendGrid().getCell(u));
                if (d < min) {
                    min = d;
                    closest = u;
                }
            }
        }
        return closest;
    }

    private Unit findBestFriendlyTarget(Unit currentUnit, Action action, GameManager gm) {
        List<Unit> allies = gm.getTurnManager().getAllActiveUnits().stream()
                .filter(u -> u.isFriendly() == currentUnit.isFriendly()) // only friendly units
                .filter(u -> u != currentUnit) // that are not the current one casting the buff
                .toList();

        Unit bestTarget = null;
        double lowestHealthRatio = 1.0;

        for (Unit ally : allies) {
            if (action.isInRange(gm.getBackendGrid().getCell(currentUnit), gm.getBackendGrid().getCell(ally))) {
                double healthRatio = (double) ally.getHP() / ally.getMaxHP();
                if (healthRatio < lowestHealthRatio) {
                    lowestHealthRatio = healthRatio;
                    bestTarget = ally;
                }
            }
        }
        return (lowestHealthRatio < 0.9 || !action.getType().equalsIgnoreCase("Heal")) ? bestTarget : null;
    }

    private double getDistance(Cell a, Cell b) {
        return Math.hypot(a.getCol() - b.getCol(), a.getRow() - b.getRow());
    }
}