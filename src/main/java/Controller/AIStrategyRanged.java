package Controller;

import Action.Action;
import Board.Cell;
import Board.Grid;
import Unit.Unit;

import java.util.List;

public class AIStrategyRanged implements AIStrategy {
    private static final int SAFE_DISTANCE = 3; // minimum range for skills

    @Override
    public void executeTurn(Unit currentUnit, List<Unit> allUnits, GameManager gm) {
        // Base Case: Out of AP and MP
        if (currentUnit.getAP() <= 0 && currentUnit.getMP() <= 0) {
            endTurn(gm);
            return;
        }

        // 1. Support Phase
        Action supportAction = findSupportAction(currentUnit);
        if (supportAction != null && currentUnit.getAP() >= supportAction.getApCost()) {
            Unit bestFriendlyTarget = findBestFriendlyTarget(currentUnit, supportAction, gm);

            if (bestFriendlyTarget != null) {
                Cell targetCell = gm.getBackendGrid().getCell(bestFriendlyTarget);

                gm.handleAction(currentUnit, targetCell.getUnit(), supportAction);
                gm.getGUI().logMessage(supportAction.setLogAction(currentUnit, targetCell.getUnit()));

                // Delay to let animation play
                gm.getGUI().delayExecution(1 / nckbill.turnbasedfinal.GameGUI.getGameSpeed(), () -> executeTurn(currentUnit, allUnits, gm));
                return;
            }
        }

        // 2. Identify Target
        Unit target = findClosestEnemy(currentUnit, allUnits, gm);
        if (target == null) {
            endTurn(gm);
            return;
        }

        Cell selfCell = gm.getBackendGrid().getCell(currentUnit);
        Cell targetCell = gm.getBackendGrid().getCell(target);
        double dist = getDistance(selfCell, targetCell);

        // 3. Attack Phase
        boolean attackedThisCycle = false;
        for (Action action : currentUnit.getAvailableActions()) {
            if (action.getType().equals("Damage")) {
                if (action.canExecute(selfCell, targetCell)) {
                    gm.handleAction(currentUnit, target, action);
                    gm.getGUI().logMessage(action.setLogAction(currentUnit, target));
                    attackedThisCycle = true;
                    break;
                }
            }
        }

        if (attackedThisCycle) {
            // Delay to let attack animation play
            gm.getGUI().delayExecution(1 / nckbill.turnbasedfinal.GameGUI.getGameSpeed(), () -> executeTurn(currentUnit, allUnits, gm));
            return;
        }

        // 4. Kite phase (Retreat if enemy is too close)
        if (dist < SAFE_DISTANCE && currentUnit.getMP() > 0) {
            Cell retreatCell = calculateRetreatCell(selfCell, targetCell, gm);
            if (retreatCell != null) {
                List<Cell> retreatPath = gm.getBackendGrid().calculatePathDijkstra(selfCell, retreatCell);
                retreatPath = limitPathByMP(retreatPath, currentUnit.getMP());
                // executeMovement triggers the callback when the walk animation finishes
                gm.getGUI().executeMovement(currentUnit, retreatPath, () -> {
                    executeTurn(currentUnit, allUnits, gm);
                });
                return;
            }
        }

        // 5. Movement (Chase if out of range)
        if (!hasActionInRange(currentUnit, targetCell, gm)) {
            if (currentUnit.getMP() > 0) {

                List<Cell> reachable = gm.getBackendGrid().getReachableCells(selfCell, currentUnit.getMP());
                Cell bestChaseCell = null;
                double minTargetDist = Double.MAX_VALUE;

                for (Cell candidate : reachable) {
                    double distToTarget = getDistance(candidate, targetCell);

                    if (distToTarget >= SAFE_DISTANCE && distToTarget < minTargetDist) {
                        minTargetDist = distToTarget;
                        bestChaseCell = candidate;
                    }
                }

                if (bestChaseCell != null && bestChaseCell != selfCell) {
                    List<Cell> pathToTake = gm.getBackendGrid().calculatePathDijkstra(selfCell, bestChaseCell);
                    pathToTake = limitPathByMP(pathToTake, currentUnit.getMP());
                    if (pathToTake.size() > 1) {
                        gm.getGUI().executeMovement(currentUnit, pathToTake, () -> {
                            executeTurn(currentUnit, allUnits, gm);
                        });
                        return;
                    }
                }
            }
        }

        // 6. End turn if no action/move is possible
        endTurn(gm);
    }

    // Iterate through the action list
    // check if unit has action within range
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

    // Find the best cell away from the enemy within MP limit
    private Cell calculateRetreatCell(Cell currentCell, Cell target, GameManager gm) {
        Grid grid = gm.getBackendGrid();
        Unit unit = currentCell.getUnit();

        if (unit == null || unit.getMP() <= 0) return null;

        // Use Dijkstra-based getReachableCells to find all valid moves within MP limit
        List<Cell> reachableCells = grid.getReachableCells(currentCell, unit.getMP());

        Cell bestRetreat = null;
        double maxDist = getDistance(currentCell, target);

        for (Cell candidate : reachableCells) {
            double dist = getDistance(candidate, target);

            // Pick the valid cell that maximizes the distance from the target
            if (dist > maxDist) {
                maxDist = dist;
                bestRetreat = candidate;
            }
        }

        return bestRetreat;
    }

    // Find closest reachable enemy target
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

    // Find closest reachable friendly target
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

    // Restrict the Dijkstra path within the remaining MP
    private List<Cell> limitPathByMP(List<Cell> path, int mpLimit) {
        if (path == null || path.isEmpty()) return path;

        int accumulatedCost = 0;
        int validSteps = 1;

        for (int i = 1; i < path.size(); i++) {
            accumulatedCost += path.get(i).getTerrainCost();

            if (accumulatedCost <= mpLimit) {
                validSteps++;
            } else {
                break;
            }
        }
        return path.subList(0, validSteps);
    }
}