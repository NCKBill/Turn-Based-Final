package Controller;

import Board.Cell;
import Unit.Unit;
import java.util.List;

public class AIController implements Controller {
    private GameManager gameManager;
    private AIStrategy currentStrategy;

    public AIController(GameManager gameManager, String strategy) {
        this.gameManager = gameManager;

        if (strategy.equals("ranged"))
            currentStrategy = new AIStrategyRanged();
        else
            currentStrategy = new AIStrategyTank();
    }

    public void setStrategy(AIStrategy strategy) {
        this.currentStrategy = strategy;
    }

    @Override
    public void takeTurn(Unit unit) {
        // The Controller handles the turn flow, delegating logic to the strategy
        if (currentStrategy != null) {
            // Fetch the current list of active units from the manager
            List<Unit> allUnits = gameManager.getTurnManager().getAllActiveUnits();

            // Execute the specific behavior (Tank or Ranged)
            currentStrategy.executeTurn(unit, allUnits, gameManager);
        }
    }

    @Override
    public void handleInput(Cell clickedCell, Unit activeUnit) {
        // AI does not process user input
    }
}