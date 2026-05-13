package Controller;

import Board.Cell;
import Unit.Unit;
import Action.Action;
import java.util.List;

public class AIStrategyRanged implements AIStrategy {
    private static final int SAFE_DISTANCE = 3; // minimum range for skills

    @Override
    public void executeTurn(Unit self, List<Unit> allUnits, GameManager gm) {
        Unit target = findClosestEnemy(self, allUnits, gm);
        if (target == null) {
            gm.getTurnManager().endCurrentTurn();
            gm.processNextTurn();
            return;
        }

        Cell selfCell = gm.getBackendGrid().getCell(self);
        Cell targetCell = gm.getBackendGrid().getCell(target);
        double dist = getDistance(selfCell, targetCell);

        // Kite logic
        // Retreat if too close
        if (dist < SAFE_DISTANCE) {
            Cell retreatCell = calculateRetreatCell(selfCell, targetCell, gm);
            if (retreatCell != null) {
                gm.executeMovement(self, List.of(retreatCell), () -> {
                    gm.getTurnManager().endCurrentTurn();
                    gm.processNextTurn();
                });
                return;
            }
        }

        // Check if able to attack
        for (Action action : self.getAvailableActions()) {
            if (action.canExecute(self, selfCell, targetCell)) {
                performAttack(self, target, gm);
                return;
            }
        }

        // if unit can't attack, move closer
        List<Cell> path = gm.getBackendGrid().calculatePathDijkstra(selfCell, targetCell);

        // Move towards target
        if (path.size() > 1) {
            int moveLimit = Math.min(path.size() - 1, self.getMovementPoint());
            List<Cell> pathToTake = path.subList(0, moveLimit);

            gm.executeMovement(self, pathToTake, () -> {
                gm.getTurnManager().endCurrentTurn();
                gm.processNextTurn();
            });
        } else {
            // Cannot move, end turn
            gm.getTurnManager().endCurrentTurn();
            gm.processNextTurn();
        }
    }

    private void performAttack(Unit self, Unit target, GameManager gm) {
        Cell selfCell = gm.getBackendGrid().getCell(self);
        Cell targetCell = gm.getBackendGrid().getCell(target);

        for (Action action : self.getAvailableActions()) {
            if (action.getType().equals("Damage") && action.canExecute(self, selfCell, targetCell)) {
                int damage = action.execute(self, targetCell);
                gm.handleDamage(target, damage);
                break;
            }
        }
        gm.getTurnManager().endCurrentTurn();
        gm.processNextTurn();
    }

    private Cell calculateRetreatCell(Cell self, Cell target, GameManager gm) {
        // Move away from target
        int dr = self.getRow() - target.getRow();
        int dc = self.getCol() - target.getCol();
        int nextR = self.getRow() + Integer.signum(dr);
        int nextC = self.getCol() + Integer.signum(dc);

        Cell candidate = gm.getBackendGrid().getCell(nextR, nextC);
        return (candidate != null && !candidate.isOccupied()) ? candidate : null;
    }

    private Unit findClosestEnemy(Unit self, List<Unit> allUnits, GameManager gm) {
        Unit closest = null;
        double min = Double.MAX_VALUE;
        Cell selfCell = gm.getBackendGrid().getCell(self);

        for (Unit u : allUnits) {
            if (!self.isTargetFriendly(u)) {
                double d = getDistance(selfCell, gm.getBackendGrid().getCell(u));
                if (d < min) { min = d; closest = u; }
            }
        }
        return closest;
    }

    private double getDistance(Cell a, Cell b) {
        return Math.hypot(a.getCol() - b.getCol(), a.getRow() - b.getRow());
    }
}