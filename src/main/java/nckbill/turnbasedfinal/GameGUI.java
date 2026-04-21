package nckbill.turnbasedfinal;

import Board.*;
import Unit.*;
import Controller.*;
import Action.*;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class GameGUI extends Application {

    private BorderPane rootLayout;
    private GridPane interactiveGrid;
    private HBox topBar;
    private HBox bottomBar;
    private VBox sideBar;

    private Label sidebarStatsLabel;
    private HBox queueDisplayContainer;

    private GameManager gameManager;

    public Unit getSelectedViewUnit() {
        return gameManager != null ? gameManager.getSelectedViewUnit() : null;
    }

    public static int row = 10;
    public static int column = 10;

    @Override
    public void start(Stage primaryStage) {
        rootLayout = new BorderPane();
        gameManager = new GameManager(this, row, column);

        initializeTopBar();
        initializeSideBar();
        initializeBottomBar();
        initializeGrid();

        VBox mainMenuPane = new VBox(20);
        mainMenuPane.setStyle("-fx-alignment: center;");

        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-font-size: 24px;");

        startButton.setOnAction(event -> {
            rootLayout.setCenter(interactiveGrid);

            List<Unit> allActiveUnits = new ArrayList<>();
            for (int r = 0; r < gameManager.getBackendGrid().getRows(); r++) {
                for (int c = 0; c < gameManager.getBackendGrid().getColumns(); c++) {
                    Cell cell = gameManager.getBackendGrid().getCell(r, c);
                    if (cell != null && cell.getUnit() != null) {
                        allActiveUnits.add(cell.getUnit());
                    }
                }
            }

            // Hand over control to GameManager
            gameManager.startGame(allActiveUnits);
        });

        mainMenuPane.getChildren().add(startButton);
        rootLayout.setCenter(mainMenuPane);

        Scene scene = new Scene(rootLayout, 1000, 800);
        primaryStage.setTitle("Turn-Based Strategy Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeTopBar() {
        topBar = new HBox(15);
        topBar.setStyle("-fx-background-color: #d3d3d3; -fx-padding: 10px;");
        topBar.setMinHeight(60);

        Label queueTitle = new Label("Turn Queue: ");
        queueTitle.setStyle("-fx-font-weight: bold;");

        queueDisplayContainer = new HBox(10);
        topBar.getChildren().addAll(queueTitle, queueDisplayContainer);
        rootLayout.setTop(topBar);
    }

    private void initializeSideBar() {
        sideBar = new VBox(15);
        sideBar.setStyle("-fx-background-color: #e6e6e6; -fx-padding: 10px;");
        sideBar.setMinWidth(180);

        Label statsTitle = new Label("Unit Stats:");
        statsTitle.setStyle("-fx-font-weight: bold;");

        sidebarStatsLabel = new Label("Hover over a unit\nto see their stats:");
        sidebarStatsLabel.setWrapText(true);

        sideBar.getChildren().addAll(statsTitle, sidebarStatsLabel);
        rootLayout.setRight(sideBar);
    }

    private void initializeBottomBar() {
        bottomBar = new HBox(20);
        bottomBar.setStyle("-fx-background-color: #c0c0c0; -fx-padding: 10px;");

        Button endTurnButton = new Button("End Turn");
        endTurnButton.setOnAction(e -> {
            if (gameManager != null) {
                gameManager.endPlayerTurn();
            }
        });
        bottomBar.getChildren().add(endTurnButton);
        rootLayout.setBottom(bottomBar);
    }

    private void initializeGrid() {
        interactiveGrid = new GridPane();
        interactiveGrid.setStyle("-fx-background-color: #ffffff; -fx-alignment: center;");

        int rows = gameManager.getBackendGrid().getRows();
        int cols = gameManager.getBackendGrid().getColumns();


        Controller AIController = new AIController(gameManager);
        Controller playerController = new PlayerController(gameManager);

        Unit playerTank = new Tank(true, playerController);
        Unit playerHealer = new Healer(true, playerController);
        Unit enemyMage = new Mage(false, AIController);

        if (gameManager.getBackendGrid().getCell(2, 2) != null) gameManager.getBackendGrid().getCell(2, 2).setUnit(playerTank);
        if (gameManager.getBackendGrid().getCell(1, 1) != null) gameManager.getBackendGrid().getCell(1, 1).setUnit(playerHealer);
        if (gameManager.getBackendGrid().getCell(7, 7) != null) gameManager.getBackendGrid().getCell(7, 7).setUnit(enemyMage);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int finalRow = row;
                int finalCol = col;

                CellUI visualCell = new CellUI(finalRow, finalCol, sidebarStatsLabel, this);

                Cell currentCell = gameManager.getBackendGrid().getCell(finalRow, finalCol);
                if (currentCell != null && currentCell.getUnit() != null) {
                    visualCell.setUnit(currentCell.getUnit());
                }

                // Pass to GameManager
                visualCell.setOnMouseClicked(e -> gameManager.handleCellClick(finalRow, finalCol));

                interactiveGrid.add(visualCell, col, row);
            }
        }
    }

    public void updateTurnDisplay(Unit active) {
        queueDisplayContainer.getChildren().clear();

        if (active != null) {
            // Fetch upcoming turn order from the GameManager
            Queue<Unit> turnQueue = gameManager.getTurnManager().getTurnQueue();

            // Add currently active unit first
            Button activeUnitBtn = new Button(">> " + active.getName() + " <<");
            activeUnitBtn.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-border-color: green; -fx-border-radius: 3px;");
            activeUnitBtn.setOnAction(e -> handleTopBarUnitClick(active));
            queueDisplayContainer.getChildren().add(activeUnitBtn);

            // Loop through the rest of the queue and add them to top bar
            for (Unit queueUnit : turnQueue) {
                // Skip if active unit is already added
                if (queueUnit == active) continue;

                Button queuedUnitBtn = new Button(queueUnit.getName());

                // Friendly units blue, and enemy units red
                String textColor = queueUnit.isFriendly() ? "blue" : "red";
                queuedUnitBtn.setStyle("-fx-text-fill: " + textColor + ";");

                queuedUnitBtn.setOnAction(e -> handleTopBarUnitClick(queueUnit));
                queueDisplayContainer.getChildren().add(queuedUnitBtn);
            }

            // Auto-update the sidebar and bottom bar to show the active unit by default when the turn starts
            gameManager.setSelectedViewUnit(active);
            updateSidebarStats(active);
            updateBottomBarSkills(active);
        } else {
            queueDisplayContainer.getChildren().add(new Label("Calculating next round..."));
            bottomBar.getChildren().clear();
        }

        refreshVisualGrid();
    }

    /**
     * Triggered when a player clicks a unit's name in the Top Bar queue.
     */
    private void handleTopBarUnitClick(Unit clickedUnit) {
        // Tell the backend manager that a new unit is selected
        gameManager.setSelectedViewUnit(clickedUnit);

        // Update the visual stats and skills to match the newly selected unit
        updateSidebarStats(clickedUnit);
        updateBottomBarSkills(clickedUnit);
    }

    public void updateSidebarStats(Unit unit) {
        if (unit != null) {
            sidebarStatsLabel.setText(unit.toString());
        }
    }

    public void updateBottomBarSkills(Unit selectedUnit) {
        bottomBar.getChildren().clear();

        Button endTurnButton = new Button("End Turn");
        endTurnButton.setOnAction(e -> {
            if (gameManager != null) {
                gameManager.endPlayerTurn();
            }
        });
        bottomBar.getChildren().add(endTurnButton);

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
                bottomBar.getChildren().add(skillButton);
            }
        }
    }

    public void executeVisualMovement(Unit movingUnit, List<Cell> path) {
        interactiveGrid.setDisable(true);

        for (Cell step : path) {
            if (step.getUnit() == movingUnit) continue;

            Cell currentCell = gameManager.getBackendGrid().getCell(movingUnit);
            if (currentCell != null) currentCell.setUnit(null);
            step.setUnit(movingUnit);
            movingUnit.setMovementPoint(movingUnit.getMovementPoint() - 1);
        }

        System.out.println(movingUnit.getName() + " finished moving.");
        refreshVisualGrid();
        interactiveGrid.setDisable(false);
        updateSidebarStats(movingUnit);
    }

    public void refreshVisualGrid() {
        for (Node node : interactiveGrid.getChildren()) {
            if (node instanceof CellUI) {
                CellUI visualCell = (CellUI) node;
                Cell currentCell = gameManager.getBackendGrid().getCell(visualCell.getRow(), visualCell.getCol());

                if (currentCell != null) {
                    visualCell.setUnit(currentCell.getUnit());
                } else {
                    visualCell.setUnit(null);
                }
            }
        }
    }

    public void drawPathHighlight(int targetRow, int targetCol) {
        Unit selected = gameManager.getSelectedViewUnit();
        Unit active = gameManager.getTurnManager().getActiveUnit();

        if (selected != null && selected == active && selected.isFriendly() && selected.getMovementPoint() > 0) {
            Cell startCell = gameManager.getBackendGrid().getCell(selected);
            Cell targetCell = gameManager.getBackendGrid().getCell(targetRow, targetCol);

            if (targetCell != null && targetCell.getUnit() == null) {
                List<Cell> fullPath = gameManager.getBackendGrid().calculatePathDijkstra(startCell, targetCell);

                if (fullPath != null && !fullPath.isEmpty()) {
                    int mpLimit = selected.getMovementPoint();
                    for (int i = 0; i < Math.min(fullPath.size(), mpLimit); i++) {
                        Cell step = fullPath.get(i);
                        for (Node node : interactiveGrid.getChildren()) {
                            if (node instanceof CellUI) {
                                CellUI cellUI = (CellUI) node;
                                if (cellUI.getRow() == step.getRow() && cellUI.getCol() == step.getCol()) {
                                    cellUI.setPathHighlight(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void clearPathHighlight() {
        for (Node node : interactiveGrid.getChildren()) {
            if (node instanceof CellUI) {
                ((CellUI) node).setPathHighlight(false);
            }
        }
    }
}