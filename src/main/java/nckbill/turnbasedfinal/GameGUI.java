package nckbill.turnbasedfinal;

import Action.Action;
import Board.Cell;
import Controller.GameManager;
import Controller.PlayerController;
import Unit.Unit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import nckbill.turnbasedfinal.UI.*;
import nckbill.turnbasedfinal.utils.AudioManager;

import java.util.List;

/**
 * <h3>Layout:</h3>
 * Build main window (StartUI)
 * and organize menus (top bar, sidebar, bottom bar)
 * Handle switching between Start Menu and in game grid.
 *
 * <h3>In game:</h3>
 * Draw square grid and keep characters in sync.
 * Run animation
 * Draw yellow movement path for player's character.
 * <p>
 * Listen to mouse click, tell GameManager to decide what to do.
 * Handle playing background music.
 * Handle all visual and audio of the game
 */
public class GameGUI extends Application {
    private BorderPane rootLayout;
    private GridPane interactiveGrid;

    private StartUI startUI;
    private TopBarUI topBar;
    private BottomBarUI bottomBar;
    private SideBarUI sideBarUI;
    private CellUI[][] grid;

    private GameManager gameManager;

    public Unit getSelectedViewUnit() {
        return gameManager != null ? gameManager.getSelectedViewUnit() : null;
    }

    public static double gameSpeed = 0.7;
    public static int row = 10;
    public static int column = 10;

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {

        rootLayout = new BorderPane();
        gameManager = new GameManager(this, row, column);
        // initialize all boxes (visual on screen)
        topBar = new TopBarUI(this);
        bottomBar = new BottomBarUI(this);
        sideBarUI = new SideBarUI();

        startUI = new StartUI(this);
        rootLayout.setCenter(startUI);

        initializeGrid();
        // Start the game
        Scene scene = new Scene(rootLayout, 1000, 800);
        primaryStage.setTitle("Glorious Battle");
        primaryStage.setScene(scene);
        primaryStage.show();
        playBGM();
    }

    // show top bar, bottom bar, sidebar, grid
    public void showGameGUI() {
        rootLayout.setTop(topBar);
        rootLayout.setBottom(bottomBar);
        rootLayout.setRight(sideBarUI);
        rootLayout.setCenter(interactiveGrid);
    }
    public void hideGameGUI() {
        rootLayout.setTop(null);
        rootLayout.setBottom(null);
        rootLayout.setRight(null);
    }

    // Return to start menu after game end (win/loss)
    private void returnToMainMenu() {
        javafx.application.Platform.runLater(() -> {
            hideGameGUI();
            rootLayout.setCenter(startUI);
            interactiveGrid.setDisable(false);
            bottomBar.initializeDefault();
        });
    }

    // Method to initialize grid
    public void initializeGrid() {
        interactiveGrid = new GridPane();
        interactiveGrid.setStyle("-fx-background-color: #ffffff; -fx-alignment: center;");

        int rows = gameManager.getBackendGrid().getRows();
        int cols = gameManager.getBackendGrid().getColumns();
        grid = new CellUI[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int finalRow = r;
                int finalCol = c;

                Cell currentCell = gameManager.getBackendGrid().getCell(finalRow, finalCol);
                int terrainType = (currentCell != null) ? currentCell.getTerrainType() : 0;

                CellUI visualCell = new CellUI(finalRow, finalCol, terrainType, sideBarUI.getUnitStatsLabel(), sideBarUI.getCellInfoLabel(), this);
                grid[r][c] = visualCell;
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

    public void updateSidebarUnitStats(Unit unit) {
        sideBarUI.updateSidebarUnitStats(unit);
    }

    public void updateSideBarActionStats(Action action) {
        sideBarUI.updateActionStats(action);
    }

    public void refreshVisualGrid() {
        for (Node node : interactiveGrid.getChildren()) {
            Unit activeUnit = gameManager.getTurnManager().getActiveUnit();
            if (node instanceof CellUI visualCell) {
                Cell currentCell = gameManager.getBackendGrid().getCell(visualCell.getRow(), visualCell.getCol());
                if (currentCell != null) {
                    visualCell.setTerrainType(currentCell.getTerrainType());
                    visualCell.setUnit(currentCell.getUnit());

                    if (visualCell.getUnitUI() != null) {
                        boolean isActive = (currentCell.getUnit() == activeUnit);
                        visualCell.getUnitUI().setActiveHighlight(isActive);
                    }
                }
            }

        }
    }

    // draw highlight path for player unit
    public void drawPathHighlight(int targetRow, int targetCol) {
        Unit selected = getSelectedViewUnit();
        Unit active = gameManager.getTurnManager().getActiveUnit();

        if (selected != null && selected == active && selected.isFriendly() && selected.getMP() > 0 && selected.getUnitController() instanceof PlayerController) {
            Cell startCell = gameManager.getBackendGrid().getCell(selected);
            Cell targetCell = gameManager.getBackendGrid().getCell(targetRow, targetCol);

            if (targetCell != null && targetCell.getUnit() == null) {
                List<Cell> fullPath = gameManager.getBackendGrid().calculatePathDijkstra(startCell, targetCell);

                if (fullPath != null && !fullPath.isEmpty()) {
                    int remainingMP = selected.getMP();
                    // Skip start cell in path if it is included
                    int startIndex = (fullPath.get(0).equals(startCell)) ? 1 : 0;

                    for (int i = startIndex; i < fullPath.size(); i++) {
                        Cell step = fullPath.get(i);
                        int cost = step.getTerrainCost();

                        if (remainingMP >= cost) {
                            remainingMP -= cost;
                            for (Node node : interactiveGrid.getChildren()) {
                                if (node instanceof CellUI cellUI) {
                                    if (cellUI.getRow() == step.getRow() && cellUI.getCol() == step.getCol()) {
                                        cellUI.setPathHighlight(true);
                                    }
                                }
                            }
                        } else {
                            break; // Out of MP
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
        Timeline timeline = new Timeline();
        double defaultDelay = 300 / gameSpeed;
        double delay = defaultDelay;
        int[] stepsAdded = {0};

        for (Cell step : path) {
            if (step.getUnit() == currentUnit) continue;

            KeyFrame keyFrame = new KeyFrame(Duration.millis(delay), event -> {
                Cell oldCell = gameManager.getBackendGrid().getCell(currentUnit);
                int oldR = oldCell.getRow();
                int oldC = oldCell.getCol();

                oldCell.setUnit(null);
                step.setUnit(currentUnit);
                currentUnit.setMP(currentUnit.getMP() - step.getTerrainCost());

                updateVisualCell(oldR, oldC);
                updateVisualCell(step.getRow(), step.getCol());

                updateSidebarUnitStats(currentUnit);
            });

            timeline.getKeyFrames().add(keyFrame);
            delay += defaultDelay;
            stepsAdded[0]++;
        }

        timeline.setOnFinished(event -> {
            if (currentUnit == null || currentUnit.getHP() <= 0) {
                System.out.println("Animation cancelled: Unit is dead.");
                return;
            }

            System.out.println(currentUnit.getName() + " finished moving.");

            if (onCompleted != null) {
                if (stepsAdded[0] > 0) {
                    // Unit actually moved — brief pause so the last step is visible
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(500));
                    pause.setOnFinished(e -> onCompleted.run());
                    pause.play();
                } else {
                    // No movement — run callback immediately with no delay
                    onCompleted.run();
                }
            }
        });

        timeline.play();
    }

    public void executeUnitAnimation(CellUI currentUnitCell, CellUI targetCell, Runnable onAnimationComplete) {
        UnitUI currentUnitUI = currentUnitCell.getUnitUI();

        if (currentUnitUI != null) {
            double dx = (targetCell.getCol() - currentUnitCell.getCol()) * 30;
            double dy = (targetCell.getRow() - currentUnitCell.getRow()) * 30;

            // Fire callback only after the bump animation fully completes,
            // so fast game speeds can't start the next attack mid-animation.
            currentUnitUI.playActionAnimation(dx, dy, onAnimationComplete);
        } else {
            if (onAnimationComplete != null) {
                onAnimationComplete.run();
            }
        }
    }

    public void delayExecution(double seconds, Runnable onComplete) {
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(seconds));
        if (onComplete != null) {
            pause.setOnFinished(e -> onComplete.run());
        }
        pause.play();
    }

    public void showGameOver(String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Game End.");
            alert.setHeaderText(null);
            alert.setContentText(message);

            if (message.equalsIgnoreCase("victory!"))
                playBGMWithFade("/assets/audio/victory.mp3");
            else
                playBGMWithFade("/assets/audio/defeat.mp3");

            alert.showAndWait();
        });
    }

    public void restartGame() {
        Platform.runLater(() -> {
            interactiveGrid.setDisable(true);
            returnToMainMenu();
            gameManager.resetGame();
            sideBarUI.clearLog();
            playBGMWithFade("/assets/audio/menu-theme.mp3");
        });
    }

    private void updateVisualCell(int row, int col) {
        if (row >= 0 && row < grid.length && col >= 0 && col < grid[0].length) {
            CellUI visualCell = grid[row][col];
            Cell backendCell = gameManager.getBackendGrid().getCell(row, col);
            visualCell.setUnit(backendCell != null ? backendCell.getUnit() : null);

            if (visualCell.getUnitUI() != null && backendCell != null) {
                Unit activeUnit = gameManager.getTurnManager().getActiveUnit();
                visualCell.getUnitUI().setActiveHighlight(backendCell.getUnit() == activeUnit);
            }
        }
    }

    public void logMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        if (sideBarUI != null) {
            sideBarUI.addLogMessage(message);
        } else {
            System.out.println("Log (UI not ready): " + message);
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public BorderPane getRootLayout() {
        return rootLayout;
    }

    public GridPane getInteractiveGrid() {
        return interactiveGrid;
    }

    public CellUI[][] getGrid() {
        return grid;
    }

    public void playBGM() {
        AudioManager.playBGM("/assets/audio/menu-theme.mp3");
    }

    public static double getGameSpeed() {
        return gameSpeed;
    }

    public static void setGameSpeed(double gameSpeed) {
        GameGUI.gameSpeed = gameSpeed;
    }

    public void playBGMWithFade(String path) {
        AudioManager.playBGMWithFade(1.0, 0.2, path);
    }

}