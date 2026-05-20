package nckbill.turnbasedfinal;

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
    private Label unitIcon;
    private Label sideStatsLabel;
    private boolean isReachable = false;
    private GameGUI gui;

    private ImageView unitImageView;
    private static final int CELL_SIZE = 50;


    public CellUI(int row, int col, Label sideStatsLabel, GameGUI gui) {
        this.row = row;
        this.col = col;
        this.sideStatsLabel = sideStatsLabel;
        this.gui = gui;

        this.setPrefSize(CELL_SIZE, CELL_SIZE);
        this.setStyle("-fx-border-color: #333333; -fx-border-width: 1px;");
        // Set up Background Shape
        background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setFill(Color.LIGHTGRAY);
        background.setStroke(Color.BLACK);
        background.setMouseTransparent(true);

        // Set up the Icon
        unitIcon = new Label("");
        unitIcon.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: black;");
        unitIcon.setMouseTransparent(true);

        // Set up Image View
        unitImageView = new ImageView();
        unitImageView.setFitWidth(CELL_SIZE - 4);
        unitImageView.setFitHeight(CELL_SIZE - 4);
        unitImageView.setPreserveRatio(true);
        unitImageView.setMouseTransparent(true); // Let StackPane handle clicks/hovers

        // Add to StackPane
        this.getChildren().addAll(background, unitIcon, unitImageView);

        setupHoverEvents();
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
        this.getChildren().add(background);

        if (unit != null) {
            this.unitVisual = new UnitUI(unit);
            this.getChildren().add(this.unitVisual);
        } else {
            this.unitVisual = null;
        }
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
        if (isReachable) {
            background.setFill(Color.LIGHTGREEN);
            background.setStroke(Color.DARKGREEN);
        } else {
            resetColor();
        }
    }

    // Draws path color
    public void setPathHighlight(boolean isPath) {
        if (isPath) {
            background.setFill(Color.YELLOW);
            background.setStroke(Color.ORANGE);
        } else {
            setHighlight(this.isReachable);
        }
    }

    private void resetColor() {
        if (unit != null) {
            background.setFill(unit.isFriendly() ? Color.LIGHTBLUE : Color.LIGHTCORAL);
        } else {
            background.setFill(Color.LIGHTGRAY);
        }
        background.setStroke(Color.BLACK);
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
