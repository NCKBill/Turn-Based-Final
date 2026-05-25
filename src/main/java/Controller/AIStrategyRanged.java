package Controller;

import Action.Action;
import Board.Cell;
import Unit.Unit;

import java.util.List;

public class AIStrategyRanged implements AIStrategy {

    @Override
    public void executeTurn(Unit currentUnit, List<Unit> allUnits, GameManager gm) {
        Action supportAction = findSupportAction(currentUnit);
        Unit enemyTarget = findClosestEnemy(currentUnit, allUnits, gm);
        // If current unit has support action then find support target -> else null
        Unit supportTarget = supportAction != null ? findBestFriendlyTarget(currentUnit, supportAction, gm) : null;

        // Support if current unit has support target
        if (supportAction != null && supportTarget != null) {
            executeSupportTurn(currentUnit, supportTarget, supportAction, allUnits, gm);
            return;
        }

        // otherwise use damage skill
        if (enemyTarget != null) {
            executeAttackTurn(currentUnit, enemyTarget, gm);
            return;
        }
        endTurn(gm);
    }

    private void executeAttackTurn(Unit unit, Unit target, GameManager gm) {
        Cell selfCell = gm.getBackendGrid().getCell(unit);
        Cell targetCell = gm.getBackendGrid().getCell(target);
        Action attackAction = getBestAttackAction(unit);

        // No AP or no valid action — move toward target if possible, then end
        if (attackAction == null) {
            if (unit.getMP() > 0) {
                moveIntoRange(unit, targetCell, 1, gm, () -> endTurn(gm));
            } else {
                endTurn(gm);
            }
            return;
        }

        if (attackAction.canExecute(selfCell, targetCell)) {
            // Already in range — attack now, then loop back to spend remaining AP
            tryExecutingAttack(unit, target, attackAction, gm);
        } else if (unit.getMP() > 0) {
            // Out of range — move in, then attack with whatever AP remains
            moveIntoRange(unit, targetCell, attackAction.getRange(), gm,
                    () -> tryExecutingAttack(unit, target, attackAction, gm));
        } else {
            endTurn(gm);
        }
    }

    // Attack the target if still in range and AP allows; reposition when AP is spent
    private void tryExecutingAttack(Unit unit, Unit target, Action attackAction, GameManager gm) {
        Cell currentCell = gm.getBackendGrid().getCell(unit);
        Cell targetCell = gm.getBackendGrid().getCell(target);

        // Target may have died
        if (target == null || target.getHP() <= 0) {
            repositionAfterAction(unit, currentCell, gm);
            return;
        }

        // Re-fetch best action in case AP changed between recursive calls
        Action nextAction = getBestAttackAction(unit);
        if (nextAction == null) {
            // Out of AP: reposition and end turn
            repositionAfterAction(unit, targetCell, gm);
            return;
        }

        if (nextAction.canExecute(currentCell, targetCell)) {
            gm.getGUI().logMessage(nextAction.setLogAction(unit, target));

            gm.handleAction(unit, target, nextAction, () -> {
                // Try to spend remaining AP on the same target
                tryExecutingAttack(unit, target, nextAction, gm);
            });
        } else {
            // Out of range after recheck: reposition and end
            repositionAfterAction(unit, targetCell, gm);
        }
    }

    private void performAction(Unit unit, Unit target, Action action, List<Unit> allUnits, GameManager gm) {
        gm.getGUI().logMessage(action.setLogAction(unit, target));

        gm.handleAction(unit, target, action, () -> {
            executeTurn(unit, allUnits, gm);
        });
    }


    private void executeSupportTurn(Unit unit, Unit ally, Action supportAction, List<Unit> allUnits, GameManager gm) {
        Cell selfCell = gm.getBackendGrid().getCell(unit);
        Cell allyCell = gm.getBackendGrid().getCell(ally);

        // If out of range, move first
        if (!supportAction.canExecute(selfCell, allyCell) && unit.getMP() > 0) {
            // re-evaluate after movement
            moveIntoRange(unit, allyCell, supportAction.getRange(), gm, () -> {
                Cell newSelf = gm.getBackendGrid().getCell(unit);
                if (supportAction.canExecute(newSelf, allyCell)) {
                    performAction(unit, ally, supportAction, allUnits, gm);
                } else {
                    endTurn(gm);
                }
            });
            return;
        }

        // If already in range, perform the action immediately
        if (supportAction.canExecute(selfCell, allyCell)) {
            performAction(unit, ally, supportAction, allUnits, gm);
        } else {
            endTurn(gm);
        }
    }

    // Move to safe area
    private void repositionAfterAction(Unit unit, Cell targetCell, GameManager gm) {
        if (unit.getMP() <= 0) {
            endTurn(gm);
            return;
        }

        Cell selfCell = gm.getBackendGrid().getCell(unit);
        List<Cell> reachable = gm.getBackendGrid().getReachableCells(selfCell, unit.getMP());

        Cell bestCell = selfCell;
        double maxDistance = getDistance(selfCell, targetCell);

        for (Cell candidate : reachable) {
            double distance = getDistance(candidate, targetCell);
            if (distance > maxDistance) {
                maxDistance = distance;
                bestCell = candidate;
            }
        }

        if (!bestCell.equals(selfCell)) {
            List<Cell> path = gm.getBackendGrid().calculatePathDijkstra(selfCell, bestCell);
            path = limitPathByMP(path, unit.getMP());
            gm.executeMovement(unit, path, () -> endTurn(gm));
            return;
        }

        endTurn(gm);
    }

    // Move toward target
    private void moveIntoRange(Unit unit, Cell targetCell, double range, GameManager gm, Runnable callback) {
        Cell currentCell = gm.getBackendGrid().getCell(unit);
        List<Cell> reachable = gm.getBackendGrid().getReachableCells(currentCell, unit.getMP());
        Cell bestMove = null;
        double bestDistance = -1;
        double minDistanceToTarget = getDistance(currentCell, targetCell);
        Cell bestFallbackMove = currentCell;

        for (Cell candidate : reachable) {
            // Skip occupied cells
            if (candidate.getUnit() != null && !candidate.equals(currentCell)) {
                continue;
            }

            double dist = getDistance(candidate, targetCell);

            // Unit stay at furthest possible position
            // while maintaining support/attack range
            if (dist <= range && dist > bestDistance) {
                bestDistance = dist;
                bestMove = candidate;
            }

            // If unit can't move into range
            // move toward target
            if (dist < minDistanceToTarget) {
                minDistanceToTarget = dist;
                bestFallbackMove = candidate;
            }
        }

        // If no valid in-range tile exists
        // move toward target
        if (bestMove == null && !bestFallbackMove.equals(currentCell)) {
            bestMove = bestFallbackMove;
        }

        if (bestMove != null && !bestMove.equals(currentCell)) {
            List<Cell> path = gm.getBackendGrid().calculatePathDijkstra(currentCell, bestMove);
            path = limitPathByMP(path, unit.getMP());

            if (path != null && path.size() > 1) {
                gm.executeMovement(unit, path, callback);
                return;
            }
        }

        callback.run();
    }

    private Action getBestAttackAction(Unit unit) {
        Action bestAction = null;
        int maxRange = -1;
        for (Action action : unit.getAvailableActions()) {
            if (action.isTargetFriendly()) {
                continue;
            }
            if (unit.getAP() < action.getApCost()) {
                continue;
            }
            if (action.getRange() > maxRange) {
                maxRange = action.getRange();
                bestAction = action;
            }
        }
        return bestAction;
    }

    private void endTurn(GameManager gm) {
        gm.getTurnManager().endCurrentTurn();
        gm.processNextTurn();
    }

    private Action findSupportAction(Unit unit) {
        for (Action action : unit.getAvailableActions()) {
            if (action.isTargetFriendly()) {
                return action;
            }
        }
        return null;
    }

    private Unit findClosestEnemy(Unit currentUnit, List<Unit> allUnits, GameManager gm) {
        Unit closest = null;
        double min = Double.MAX_VALUE;
        Cell currentCell = gm.getBackendGrid().getCell(currentUnit);

        for (Unit unit : allUnits) {
            if (!currentUnit.isTargetFriendly(unit)) {
                double distance = getDistance(currentCell, gm.getBackendGrid().getCell(unit));

                if (distance < min) {
                    min = distance;
                    closest = unit;
                }
            }
        }

        return closest;
    }

    private Unit findBestFriendlyTarget(Unit currentUnit, Action action, GameManager gm) {
        Unit bestTarget = null;
        double lowestHealthRatio = Double.MAX_VALUE;
        Cell currentCell = gm.getBackendGrid().getCell(currentUnit);

        for (Unit unit : gm.getTurnManager().getAllActiveUnits()) {
            if (unit == currentUnit) {
                continue;
            }

            if (unit.isFriendly() != currentUnit.isFriendly()) {
                continue;
            }

            Cell targetCell = gm.getBackendGrid().getCell(unit);
            if (!action.canExecute(currentCell, targetCell)) {
                continue;
            }

            double healthRatio = (double) unit.getHP() / unit.getMaxHP();
            if (healthRatio < 0.9 && healthRatio < lowestHealthRatio) {
                lowestHealthRatio = healthRatio;
                bestTarget = unit;
            }
        }

        return bestTarget;
    }

    private double getDistance(Cell a, Cell b) {
        return Math.hypot(a.getCol() - b.getCol(), a.getRow() - b.getRow());
    }

    private List<Cell> limitPathByMP(List<Cell> path, int mpLimit) {
        if (path == null || path.isEmpty()) return path;
        int cost = 0;
        int valid = 1;
        for (int i = 1; i < path.size(); i++) {
            cost += path.get(i).getTerrainCost();
            if (cost <= mpLimit) {
                valid++;
            } else {
                break;
            }
        }

        return path.subList(0, valid);
    }
}