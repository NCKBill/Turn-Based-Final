package nckbill.turnbasedfinal;

import Action.Action;
import Controller.GameManager;
import Unit.Unit;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class BottomBarUI extends HBox {
    private GameManager gameManager;

    public BottomBarUI() {
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #c0c0c0; -fx-padding: 10px;");
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
        initializeDefault();
    }

    public void initializeDefault() {
        this.getChildren().clear();
        Button endTurnButton = new Button("End Turn");
        endTurnButton.setOnAction(e -> {
            if (gameManager != null) {
                gameManager.endPlayerTurn();
            }
        });
        this.getChildren().add(endTurnButton);
    }

    public void updateBottomBarSkills(Unit selectedUnit) {
        this.getChildren().clear();

        Button endTurnButton = new Button("End Turn");
        endTurnButton.setOnAction(e -> {
            if (gameManager != null) {
                gameManager.endPlayerTurn();
            }
        });
        this.getChildren().add(endTurnButton);

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