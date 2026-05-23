package nckbill.turnbasedfinal;

import Board.Cell;
import Unit.Unit;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CellUI extends StackPane {

    private int row;
    private int col;
    private Unit unit;
    private UnitUI unitVisual;

    private Rectangle background;
    private ImageView terrainImageView;
    private Label unitIcon;
    private Label sideStatsLabel;
    private boolean isReachable = false;
    private GameGUI gui;

    private ImageView unitImageView;
    private static final int CELL_SIZE = 50;


    private int terrainType = 0; // 0: Grass, 1: Water, 2: Woods, 3: Wall
    private int variant = 1;

    public CellUI(int row, int col, int terrainType, Label sideStatsLabel, GameGUI gui) {
        this.row = row;
        this.col = col;
        this.terrainType = terrainType;
        this.sideStatsLabel = sideStatsLabel;
        this.gui = gui;

        // Try to fetch variant from backend if possible
        if (gui.getGameManager() != null && gui.getGameManager().getBackendGrid() != null) {
            Cell backendCell = gui.getGameManager().getBackendGrid().getCell(row, col);
            this.variant = (backendCell != null) ? backendCell.getVariant() : 1;
        }

        this.setPrefSize(CELL_SIZE, CELL_SIZE);
        this.setStyle("-fx-border-color: #333333; -fx-border-width: 1px;");
        
        // Set up Terrain Image View
        terrainImageView = new ImageView();
        terrainImageView.setFitWidth(CELL_SIZE);
        terrainImageView.setFitHeight(CELL_SIZE);
        terrainImageView.setMouseTransparent(true);
        setTerrainImage();

        // Set up Background Overlay for highlights
        background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setMouseTransparent(true);
        background.setOpacity(0.4); // Mild highlight
        resetColor();

        // Set up the Icon
        unitIcon = new Label("");
        unitIcon.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: black;");
        unitIcon.setMouseTransparent(true);

        // Set up Image View
        unitImageView = new ImageView();
        unitImageView.setFitWidth(CELL_SIZE - 4);
        unitImageView.setFitHeight(CELL_SIZE - 4);
        unitImageView.setPreserveRatio(true);
        unitImageView.setMouseTransparent(true);

        // Add to StackPane
        this.getChildren().addAll(terrainImageView, background, unitIcon, unitImageView);

        setupHoverEvents();
    }

    private void setTerrainImage() {
        String type;
        switch (terrainType) {
            case 1: type = "water"; break;
            case 2: type = "tree"; break;
            case 3: type = "mountain"; break;
            case 0:
            default: type = "grass"; break;
        }
        String path = "/assets/terrains/" + type + "-" + variant + ".png";
        terrainImageView.setImage(ImageCache.getImage(path));
    }


    public void setUnit(Unit unit) {
        // This prevents the ongoing attack animation from being destroyed.
        if (this.unit == unit && this.unit != null && this.unitVisual != null) {
            this.unitVisual.updateHP();
            resetColor();
            return;
        }

        this.unit = unit;
        this.getChildren().clear();
        this.getChildren().addAll(terrainImageView, background);

        if (unit != null) {
            this.unitVisual = new UnitUI(unit);
            this.getChildren().add(this.unitVisual);
        } else {
            this.unitVisual = null;
        }
        resetColor();
    }

    public void setTerrainType(int terrainType) {
        this.terrainType = terrainType;
        if (gui.getGameManager() != null && gui.getGameManager().getBackendGrid() != null) {
            Cell backendCell = gui.getGameManager().getBackendGrid().getCell(row, col);
            this.variant = (backendCell != null) ? backendCell.getVariant() : 1;
        }
        setTerrainImage();
        resetColor();
    }

    public UnitUI getUnitUI() {
        return unitVisual;
    }

    public Unit getUnit() {
        return unit;
    }

    public boolean isEmpty() {
        return unit == null;
    }

    public void setHighlight(boolean isReachable) {
        this.isReachable = isReachable;
        resetColor();
    }

    // Draws path color
    public void setPathHighlight(boolean isPath) {
        if (isPath) {
            background.setFill(Color.YELLOW);
            background.setOpacity(0.6);
            background.setStroke(Color.ORANGE);
        } else {
            setHighlight(this.isReachable);
        }
    }

    private void resetColor() {
        if (isReachable) {
            background.setFill(Color.LIGHTGREEN);
            background.setOpacity(0.5);
            background.setStroke(Color.DARKGREEN);
            return;
        }

        if (unit != null) {
            background.setFill(unit.isFriendly() ? Color.CYAN : Color.TOMATO);
            background.setOpacity(0.3);
        } else {
            background.setFill(Color.TRANSPARENT);
            background.setOpacity(0);
        }
        background.setStroke(Color.TRANSPARENT);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    private void setupHoverEvents() {
        this.setOnMouseEntered(event -> {
            if (unit != null) {
                sideStatsLabel.setText(unit.toString());
                background.setStroke(unit.isFriendly() ? Color.BLUE : Color.RED);
            } else {
                sideStatsLabel.setText("Empty Terrain:\n[" + row + ", " + col + "]");
                background.setStroke(Color.GREEN);
            }

            gui.drawPathHighlight(row, col);
        });

        this.setOnMouseExited(event -> {
            background.setStroke(Color.BLACK);
            Unit lockedUnit = gui.getSelectedViewUnit();

            if (lockedUnit != null) {
                sideStatsLabel.setText(lockedUnit.toString());
            }

            gui.clearPathHighlight();
        });
    }
}
