package nckbill.turnbasedfinal;

import Board.*;
import Controller.*;
import Unit.*;
import java.util.*;

public class UnitGenerator {
    private Random rand = new Random();
    public int allyCount;
    public int enemyCount;

    public UnitGenerator(int allyCount, int enemyCount) {
        this.allyCount = allyCount;
        this.enemyCount = enemyCount;
    }

    public UnitGenerator() {
        this.allyCount = 4;
        this.enemyCount = 4;
    }
    public List<Unit> setup(GameManager gm) {
        List<Unit> allUnits = new ArrayList<>();
        Grid grid = gm.getBackendGrid();
        // Spawn ally AI units
        for (int i = 0; i < allyCount; i++) {
            Controller controller = new PlayerController(gm);
            Unit ally = getRandomUnit(true, null, gm);
            if (spawnUnitRandom(ally, gm)) {
                allUnits.add(ally);
            }
        }
        // Spawn enemy AI units
        for (int i = 0; i < enemyCount; i++) {
            Unit enemy = getRandomUnit(false, null, gm); // Controller assigned in getRandomUnit
            if (spawnUnitRandom(enemy, gm)) {
                allUnits.add(enemy);
            }
        }

        return allUnits;
    }

    private Unit getRandomUnit(boolean isFriendly, Controller forcedController, GameManager gm) {
        int type = rand.nextInt(4);
        Controller aiRanged = new AIController(gm, "ranged");
        Controller aiTank = new AIController(gm, "tank");

        return switch (type) {
            case 0 -> new Mage(isFriendly, forcedController != null ? forcedController : aiRanged);
            case 1 -> new Rogue(isFriendly, forcedController != null ? forcedController : aiTank);
            default -> new Tank(isFriendly, forcedController != null ? forcedController : aiTank);
        };
    }

    private boolean spawnUnitRandom(Unit unit, GameManager gm) {
        Grid grid = gm.getBackendGrid();
        int rows = grid.getRows();
        int cols = grid.getColumns();
        int attempts = 0;

        while (attempts < 50) {
            int r, c;

            if (unit.isFriendly()) {
                r = rand.nextInt(rows / 2 + 1, rows);
            } else {
                r = rand.nextInt(0, (int)(rows * 0.3));
            }
            c = rand.nextInt(0, cols);

            Cell targetCell = grid.getCell(r, c);
            if (targetCell != null && !targetCell.isOccupied()) {
                targetCell.setUnit(unit);
                return true;
            }
            attempts++;
        }
        return false;
    }

    public List<Unit> generate(GameManager gameManager, String choice) {
        List<Unit> allUnits = new ArrayList<>();
        String[] classPool = {"Tank", "Mage", "Healer", "Rogue"};

        boolean playerSpawned = false;
        for (String className : classPool) {

            Controller controller;

            if (className.equalsIgnoreCase(choice) && !playerSpawned) {
                controller = new PlayerController(gameManager);
                playerSpawned = true;
            } else {
                controller = new AIController(gameManager, getStrategyForClass(className));
            }
            Unit ally;
            switch (className) {
                case "Healer" -> ally = new Healer(true, controller);
                case "Mage" -> ally = new Mage(true, controller);
                case "Rogue" -> ally = new Rogue(true, controller);
                default -> ally = new Tank(true, controller);
            };

            if (spawnUnitRandom(ally, gameManager)) {
                allUnits.add(ally);
            }
        }

        for (int i = 0; i < 4; i++) {
            Unit enemy = getRandomUnit(false, null, gameManager);
            if (spawnUnitRandom(enemy, gameManager)) {
                allUnits.add(enemy);
            }
        }
        return allUnits;
    }

    private String getStrategyForClass(String className) {
        return (className.equals("Mage") || className.equals("Healer")) ? "ranged" : "tank";
    }

}