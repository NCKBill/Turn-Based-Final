package nckbill.turnbasedfinal;

import Board.*;
import Controller.*;
import Unit.*;

import java.util.*;

public class GenerateUnits {
    public List<Unit> generateUnits(GameManager gameManager) {
        List<Unit> units = new ArrayList<>();
        Controller AIController = new AIController(gameManager, "ranged");
        Controller AIController2 = new AIController(gameManager, "tank");

        Controller playerController = new PlayerController(gameManager);

//      Unit playerTank = new Tank(true, playerController);
        Unit allyMage = new Mage(true, AIController);
        Unit enemyTank = new Tank(false, AIController2);

        units.add(allyMage);
        units.add(enemyTank);
        return units;
    }

    public Cell unitCoordinate(Unit unit, int size) {
        int x, y = 0;
        Random rand = new Random();

        if (unit.isFriendly()) {
            y = rand.nextInt(size/2, size);
            x = rand.nextInt(size);
        } else {
            y = rand.nextInt(size, size/2);
            x = rand.nextInt(size);
        }

        return new Cell(x, y);
    }
}
