package nckbill.turnbasedfinal;

import Board.Cell;
import Board.Grid;
import Controller.AIController;
import Controller.Controller;
import Controller.GameManager;
import Controller.PlayerController;
import Unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UnitGenerator {
    private final Random rand = new Random();
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

    // Spawns 2 teams controlled by AI
    // Called when user choose AI-only mode
    public List<Unit> setupAI(GameManager gm) {
        List<Unit> allUnits = new ArrayList<>();

        // Spawn ally AI units
        for (int i = 0; i < allyCount; i++) {
            Unit ally = getRandomUnit(true, gm);
            if (spawnUnitRandom(ally, gm)) {
                allUnits.add(ally);
            }
        }
        // Spawn enemy AI units
        for (int i = 0; i < enemyCount; i++) {
            Unit enemy = getRandomUnit(false, gm); // Controller assigned in getRandomUnit
            if (spawnUnitRandom(enemy, gm)) {
                allUnits.add(enemy);
            }
        }

        return allUnits;
    }

    // Spawn a unit in a random, valid location on the grid
    private boolean spawnUnitRandom(Unit unit, GameManager gm) {
        Grid grid = gm.getBackendGrid();
        int rows = grid.getRows();
        int cols = grid.getColumns();
        int attempts = 0;
        int maxAttempts = rows * cols; // Try every cell if needed

        while (attempts < maxAttempts) {
            int r, c;

            if (unit.isFriendly()) {
                r = rand.nextInt((int) (((double) rows / 2) * 1.3), rows);
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
            }

            if (spawnUnitRandom(ally, gameManager)) {
                allUnits.add(ally);
            }
        }

        for (int i = 0; i < 4; i++) {
            Unit enemy = getRandomUnit(false, gameManager);
            if (spawnUnitRandom(enemy, gameManager)) {
                allUnits.add(enemy);
            }
        }
        return allUnits;
    }

    private String getStrategyForClass(String className) {
        return (className.equals("Mage") || className.equals("Healer")) ? "ranged" : "tank";
    }

    private Unit getRandomUnit(boolean isFriendly, GameManager gm) {
        int type = rand.nextInt(4);
        Controller aiRanged = new AIController(gm, "ranged");
        Controller aiTank = new AIController(gm, "tank");

        Unit unit;
        switch (type) {
            case 0:
                unit = new Mage(isFriendly, aiRanged);
                break;
            case 1:
                unit = new Rogue(isFriendly, aiTank);
                break;
            default:
                unit = new Tank(isFriendly, aiTank);
                break;
        }
        return unit;
    }
}
