package nckbill.turnbasedfinal;

import Action.Action;
import Controller.GameManager;
import Unit.Unit;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class BottomBarUI extends HBox {
    private final GameManager gameManager;
    private final GameGUI gui;
    private Button restartButton;

    public BottomBarUI(GameGUI gui) {
        this.gui = gui;
        this.gameManager = gui.getGameManager();
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #c0c0c0; -fx-padding: 10px;");
        restartButton = new Button("Return to Main Menu");
        restartButton.setOnAction(e -> {
            gui.restartGame();
        });

    }

    public void initializeDefault() {
        this.getChildren().clear();
        Button endTurnButton = new Button("End Turn");

        boolean isPlayerTurn = false;
        Unit unit = gameManager.getTurnManager().getActiveUnit();
        if (unit != null && unit.getUnitController().getClass().getSimpleName().equals("AIController"))
            endTurnButton.setDisable(true);

        endTurnButton.setDisable(!isPlayerTurn);
        endTurnButton.setOnAction(e -> {
            gameManager.endPlayerTurn();
        });

        this.getChildren().add(restartButton);
        this.getChildren().add(endTurnButton);
    }

    public void updateBottomBarSkills(Unit selectedUnit) {
        if (gameManager != null && gameManager.isMatchOver()) {
            return;
        }
        initializeDefault();

        if (selectedUnit != null && selectedUnit.getAvailableActions() != null) {
            Unit activeUnit = gameManager.getTurnManager().getActiveUnit();
            boolean isMyTurn = (selectedUnit == activeUnit);

            for (Action action : selectedUnit.getAvailableActions()) {
                Button skillButton = new Button(action.getName() + " (" + action.getApCost() + " AP)");
                skillButton.setDisable(!isMyTurn);
                skillButton.setOnAction(event -> {
                    gameManager.setSelectedAction(action);
                    System.out.println("Selected skill: " + action.getName() + " (Click a target cell!)");
                    gui.updateSideBarActionStats(action);
                });
                this.getChildren().add(skillButton);
            }
        }
    }
}