package nckbill.turnbasedfinal.UI;

import Action.Action;
import Controller.GameManager;
import Unit.Unit;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import nckbill.turnbasedfinal.GameGUI;

/**
 * <h1>Bottom Interface panel:</h1>

 * Left Interface:
 * End turn button: end player's turn
 * Skill buttons: contain all skill buttons of the currently selected unit.

 * Right Interface:
 * Return to menu button (self-explanatory)
 * Slider to adjust global animation speed and delay.
 */
public class BottomBarUI extends HBox {
    private final GameManager gameManager;
    private final GameGUI gui;

    // UI Elements
    private final Button restartButton;
    private final HBox speedControlBox;
    private final Button endTurnButton;

    // Structural Containers
    private final HBox leftBox;
    private final HBox rightBox;

    public BottomBarUI(GameGUI gui) {
        this.gui = gui;
        this.gameManager = gui.getGameManager();
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #c0c0c0; -fx-padding: 10px;");

        // Setup Right Side elements
        restartButton = new Button("Return to Main Menu");
        restartButton.setOnAction(e -> gui.restartGame());

        Label speedLabel = new Label(String.format("Speed: %.1fx", GameGUI.getGameSpeed()));
        speedLabel.setStyle("-fx-font-weight: bold;");

        Slider speedSlider = new Slider(0.5, 3.0, GameGUI.getGameSpeed());
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setPrefWidth(150);

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            GameGUI.setGameSpeed(newVal.doubleValue());
            speedLabel.setText(String.format("Speed: %.1fx", newVal.doubleValue()));
        });

        speedControlBox = new HBox(10, speedLabel, speedSlider);
        speedControlBox.setAlignment(Pos.CENTER);

        // Create the End Turn button
        endTurnButton = new Button("End Turn");
        endTurnButton.setOnAction(e -> gameManager.endPlayerTurn());

        leftBox = new HBox(15);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.getChildren().add(endTurnButton);

        rightBox = new HBox(15);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.getChildren().addAll(speedControlBox, restartButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(leftBox, spacer, rightBox);
    }

    public void initializeDefault() {
        // Remove all skill buttons (keep end turn button since it's always the same)
        leftBox.getChildren().clear();
        leftBox.getChildren().add(endTurnButton);

        // Update disabled state based on whose turn it is
        Unit unit = gameManager.getTurnManager().getActiveUnit();
        boolean isPlayerTurn = (unit != null &&
                unit.getUnitController().getClass().getSimpleName().equals("PlayerController"));
        endTurnButton.setDisable(!isPlayerTurn);
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

                leftBox.getChildren().add(skillButton);
            }
        }
    }
}