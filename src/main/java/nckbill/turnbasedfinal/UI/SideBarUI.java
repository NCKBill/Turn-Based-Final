package nckbill.turnbasedfinal.UI;

import Action.Action;
import Unit.Unit;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Sidebar in the battle.
 * <p>
 * Visual Composition:
 * Unit info: Hold unit stats (hp, mp, ap, etc.)
 * Terrain info: Hold terrain info (name, cost to traverse, location on grid)
 * Action info: Hold action stats (cost, range, value)
 * Log messages: Hold log messages for action, death, unit's turn.
 */
public class SideBarUI extends BorderPane {
    private Label unitStatsLabel;
    private Label cellInfoLabel;
    private Label actionStatsLabel;
    private Label logLabel;
    private VBox logMessageContainer;
    private VBox actionStatsContainer;
    private VBox unitStatsContainer;
    private ScrollPane logPane;

    public SideBarUI() {
        this.setStyle("-fx-background-color: #e6e6e6; -fx-padding: 10px;");
        this.setMaxWidth(300);
        this.setWidth(350);
        this.setMinWidth(300);

        setUpContainers();
        // Set up the scrolling log box
        setupLogContainer();
    }

    private void setUpContainers() {
        // Top Section Container
        unitStatsContainer = new VBox(5);
        actionStatsContainer = new VBox(5);

        // Label for unit stats
        Label unitStatsTitle = new Label("Unit Stats:");
        unitStatsTitle.setStyle("-fx-font-weight: bold;");

        unitStatsLabel = new Label("Hover over a unit to see stats:");
        unitStatsLabel.setWrapText(true);

        // Label for terrains
        Label cellInfoTitle = new Label("Terrain Information:");
        cellInfoTitle.setStyle("-fx-font-weight: bold;");

        cellInfoLabel = new Label("Hover a cell to see information:");
        cellInfoLabel.setWrapText(true);

        // Label for actions
        Label actionStatsTitle = new Label("Action Stats:");
        actionStatsTitle.setStyle("-fx-font-weight: bold;");

        actionStatsLabel = new Label("Select a skill to see stats:");
        actionStatsLabel.setWrapText(true);

        unitStatsContainer.getChildren().addAll(unitStatsTitle, unitStatsLabel, cellInfoTitle, cellInfoLabel);
        actionStatsContainer.getChildren().addAll(actionStatsTitle, actionStatsLabel);

        this.setTop(unitStatsContainer);
        this.setCenter(actionStatsContainer);
    }
    public void updateSidebarUnitStats(Unit unit) {
        if (unit != null) {
            unitStatsLabel.setText(unit.toString());
        }
    }

    public void updateActionStats(Action action) {
        if (action != null)
            actionStatsLabel.setText(action.toString());
    }

    public Label getUnitStatsLabel() {
        return unitStatsLabel;
    }


    private void setupLogContainer() {
        logLabel = new Label("Log:");
        logLabel.setStyle("-fx-font-weight: bold;");
        logLabel.setWrapText(true);

        logMessageContainer = new VBox(5);
        logMessageContainer.setPadding(new Insets(10));

        logPane = new ScrollPane(logMessageContainer);
        logPane.setFitToWidth(true);
        logPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        logPane.setPrefHeight(300);

        logMessageContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            logPane.setVvalue(1.0);
        });
        VBox bottomWrapper = new VBox(5);
        bottomWrapper.getChildren().addAll(logLabel, logPane);
        this.setBottom(bottomWrapper);
    }

    public void clearLog() {
        Platform.runLater(() -> {
            logMessageContainer.getChildren().clear();
            unitStatsLabel.setText("");
            cellInfoLabel.setText("");
        });
    }
    /**
     * Method to be called by GameGUI to drop new messages into the sidebar.
     */
    public void addLogMessage(String message) {
        Platform.runLater(() -> {
            Label logLabel = new Label(message.trim());
            logLabel.setWrapText(true);
            logLabel.setMaxWidth(Double.MAX_VALUE);

            logMessageContainer.getChildren().add(logLabel);
        });
    }

    public Label getCellInfoLabel() {
        return cellInfoLabel;
    }
}