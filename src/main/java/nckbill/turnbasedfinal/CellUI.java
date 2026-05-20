package nckbill.turnbasedfinal;

import Unit.ImageCache;
import Unit.Unit;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CellUI extends StackPane {

    private int row;
    private int col;
    private Unit currentUnit;

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

        // Setup Background Shape
        background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setFill(Color.LIGHTGRAY);
        background.setStroke(Color.BLACK);
        background.setMouseTransparent(true);

        // Set up the Old Text Icon
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
        this.currentUnit = unit;

        if (unit != null) {
            boolean isFriendly = unit.isFriendly();

            // Set cell team color
            if (isFriendly)
                background.setFill(Color.LIGHTBLUE);
            else
                background.setFill(Color.LIGHTCORAL);

            // Fetch and set the image!
            String imagePath = unit.getImagePath();
            try {
                Image unitImage = ImageCache.getImage(imagePath);
                this.unitImageView.setImage(unitImage);
                this.unitIcon.setText(""); // Hide the text letter since we have an image
            } catch (Exception e) {
                // If image fails to load, use the first letter of the name
                System.err.println("Could not load image: " + imagePath);
                this.unitIcon.setText(unit.getName().substring(0, 1));
            }

        } else {
            // Empty Cell: Clear both text and image
            unitIcon.setText("");
            this.unitImageView.setImage(null);
            background.setFill(Color.LIGHTGRAY);
        }
    }

    public Unit getUnit() { return currentUnit; }
    public boolean isEmpty() { return currentUnit == null; }

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
        if (currentUnit != null) {
            background.setFill(currentUnit.isFriendly() ? Color.LIGHTBLUE : Color.LIGHTCORAL);
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
            if (currentUnit != null) {
                sideStatsLabel.setText(currentUnit.toString());
                background.setStroke(currentUnit.isFriendly() ? Color.BLUE : Color.RED);
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
