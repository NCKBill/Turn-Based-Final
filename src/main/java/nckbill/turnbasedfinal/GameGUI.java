package nckbill.turnbasedfinal;

import Board.*;
import Unit.*;
import Controller.*;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;

import java.util.*;

public class GameGUI extends Application {

    private BorderPane rootLayout;
    private GridPane interactiveGrid;

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

        // Initialize modular components
        topBar = new TopBarUI(this);
        topBar.setGameManager(gameManager);

        bottomBar = new BottomBarUI();
        bottomBar.setGameManager(gameManager);

        sideBar = new SideBarUI();

        // Assign to layout
        rootLayout.setTop(topBar);
        rootLayout.setBottom(bottomBar);
        rootLayout.setRight(sideBar);

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
            gameManager.startGame(allActiveUnits);
        });

        mainMenuPane.getChildren().add(startButton);
        rootLayout.setCenter(mainMenuPane);

        Scene scene = new Scene(rootLayout, 1000, 800);
        primaryStage.setTitle("Turn-Based Strategy Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeGrid() {
        interactiveGrid = new GridPane();
        interactiveGrid.setStyle("-fx-background-color: #ffffff; -fx-alignment: center;");

        Controller AIController = new AIController(gameManager);
        Controller playerController = new PlayerController(gameManager);

        Unit playerTank = new Tank(true, playerController);
        Unit playerHealer = new Healer(true, playerController);
        Unit enemyMage = new Mage(false, AIController);

        if (gameManager.getBackendGrid().getCell(2, 2) != null) gameManager.getBackendGrid().getCell(2, 2).setUnit(playerTank);
        if (gameManager.getBackendGrid().getCell(1, 1) != null) gameManager.getBackendGrid().getCell(1, 1).setUnit(playerHealer);
        if (gameManager.getBackendGrid().getCell(7, 7) != null) gameManager.getBackendGrid().getCell(7, 7).setUnit(enemyMage);

        int rows = gameManager.getBackendGrid().getRows();
        int cols = gameManager.getBackendGrid().getColumns();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int finalRow = r;
                int finalCol = c;

                // Pass the specific sidebar label directly to the cell
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

    public void executeMovement(Unit currentUnit, List<Cell> path, Runnable onCompleted) {
        interactiveGrid.setDisable(true);
        Timeline timeline = new Timeline();
        int delay = 300; // 300ms per step

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

            // Trigger the callback to let the AI know it can attack now
            if (onCompleted != null) {
                onCompleted.run();
            }
        });

        timeline.play();
    }
}