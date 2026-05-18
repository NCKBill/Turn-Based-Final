package nckbill.turnbasedfinal;

import Action.Action;
import Controller.GameManager;
import Unit.Unit;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class BottomBarUI extends HBox {
    private final GameManager gameManager;
    private final GameGUI gui;

    public BottomBarUI(GameGUI gui) {
        this.gui = gui;
        this.gameManager = gui.getGameManager();
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #c0c0c0; -fx-padding: 10px;");
    }

    public void initializeDefault() {
        this.getChildren().clear();
        Button endTurnButton = new Button("End Turn");
        Button restartButton = new Button("Restart");

        endTurnButton.setOnAction(e -> {
            if (gameManager != null) {
                gameManager.endPlayerTurn();
            }
        });

        restartButton.setOnAction(e -> {
            if (gui != null) {
                gui.restartGame();
            }
        });
        this.getChildren().add(endTurnButton);
        this.getChildren().add(restartButton);
    }

    public void updateBottomBarSkills(Unit selectedUnit) {
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
                });
                this.getChildren().add(skillButton);
            }
        }
    }
}