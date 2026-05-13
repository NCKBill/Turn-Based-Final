package nckbill.turnbasedfinal;

import Board.*;
import Unit.*;
import Controller.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;

import java.util.*;

public class GameGUI extends Application {
    private BorderPane rootLayout;
    private GridPane interactiveGrid;

    private StartUI startUI;
    private TopBarUI topBar;
    private BottomBarUI bottomBar;
    private SideBarUI sideBar;

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
        // initialize all boxes (visual on screen)
        topBar = new TopBarUI(this, gameManager);
        bottomBar = new BottomBarUI(gameManager);
        sideBar = new SideBarUI();

        startUI = new StartUI(this);
        rootLayout.setCenter(startUI);

        initializeGrid();
        // Start the game
        Scene scene = new Scene(rootLayout, 1000, 800);
        primaryStage.setTitle("Turn-Based Strategy Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public void showGameGUI() {
        rootLayout.setTop(topBar);
        rootLayout.setBottom(bottomBar);
        rootLayout.setRight(sideBar);
    }
    public void hideGameGUI() {
        rootLayout.setTop(null);
        rootLayout.setBottom(null);
        rootLayout.setRight(null);
    }
    // Method to return to start menu after game end (win/loss)
    private void returnToMainMenu() {
        javafx.application.Platform.runLater(() -> {
            hideGameGUI();
            rootLayout.setCenter(startUI);
            interactiveGrid.setDisable(false);
            bottomBar.initializeDefault();
        });
    }

    // method to initialize grid
    private void initializeGrid() {
        interactiveGrid = new GridPane();
        interactiveGrid.setStyle("-fx-background-color: #ffffff; -fx-alignment: center;");

        int rows = gameManager.getBackendGrid().getRows();
        int cols = gameManager.getBackendGrid().getColumns();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int finalRow = r;
                int finalCol = c;

                CellUI visualCell = new CellUI(finalRow, finalCol, sideBar.getSidebarStatsLabel(), this);

                Cell currentCell = gameManager.getBackendGrid().getCell(finalRow, finalCol);
                if (currentCell != null && currentCell.getUnit() != null) {
                    visualCell.setUnit(currentCell.getUnit());
                }

                visualCell.setOnMouseClicked(e -> gameManager.handleCellClick(finalRow, finalCol));
                interactiveGrid.add(visualCell, c, r);
            }
        }
    }

    public BottomBarUI getBottomBar() {
        return bottomBar;
    }

    public void updateTurnDisplay(Unit active) {
        topBar.updateTurnDisplay(active);
    }

    public void updateSidebarStats(Unit unit) {
        sideBar.updateSidebarStats(unit);
    }

    public void refreshVisualGrid() {
        for (Node node : interactiveGrid.getChildren()) {
            if (node instanceof CellUI) {
                CellUI visualCell = (CellUI) node;
                Cell currentCell = gameManager.getBackendGrid().getCell(visualCell.getRow(), visualCell.getCol());
                visualCell.setUnit(currentCell != null ? currentCell.getUnit() : null);
            }
        }
    }

    public void drawPathHighlight(int targetRow, int targetCol) {
        Unit selected = getSelectedViewUnit();
        Unit active = gameManager.getTurnManager().getActiveUnit();

        if (selected != null && selected == active && selected.isFriendly() && selected.getMovementPoint() > 0 && selected.getUnitController() instanceof PlayerController) {
            Cell startCell = gameManager.getBackendGrid().getCell(selected);
            Cell targetCell = gameManager.getBackendGrid().getCell(targetRow, targetCol);

            if (targetCell != null && targetCell.getUnit() == null) {
                List<Cell> fullPath = gameManager.getBackendGrid().calculatePathDijkstra(startCell, targetCell);

                if (fullPath != null && !fullPath.isEmpty()) {
                    int moveLimit = Math.min(fullPath.size(), selected.getMovementPoint() + 1);
                    for (int i = 0; i < moveLimit; i++) {
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

    public void executeMovement(Unit currentUnit, List<Cell> path, Runnable onCompleted) {
        interactiveGrid.setDisable(true);
        Timeline timeline = new Timeline();
        int delay = 300; // 300ms every step

        for (int i = 0; i < path.size(); i++) {
            Cell step = path.get(i);

            if (step.getUnit() == currentUnit) continue;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(delay), event -> {
                Cell currentCell = gameManager.getBackendGrid().getCell(currentUnit);
                if (currentCell != null) currentCell.setUnit(null);

                step.setUnit(currentUnit);
                currentUnit.setMovementPoint(currentUnit.getMovementPoint() - 1);

                refreshVisualGrid();
                updateSidebarStats(currentUnit); // Update stats to reflect remaining MP
            });

            timeline.getKeyFrames().add(keyFrame);
            delay += 300;
        }
        timeline.setOnFinished(event -> {
            System.out.println(currentUnit.getName() + " finished moving.");
            interactiveGrid.setDisable(false);

            // Let the AI know it can attack now
            if (onCompleted != null) {
                onCompleted.run();
            }
        });

        timeline.play();
    }

    //  disable input after game over
    public void showGameOver(String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.showAndWait();

            interactiveGrid.setDisable(true);

            returnToMainMenu();
            gameManager.resetGame();
        });
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public BorderPane getRootLayout() {
        return rootLayout;
    }

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    public GridPane getInteractiveGrid() {
        return interactiveGrid;
    }

    public void setInteractiveGrid(GridPane interactiveGrid) {
        this.interactiveGrid = interactiveGrid;
    }
}