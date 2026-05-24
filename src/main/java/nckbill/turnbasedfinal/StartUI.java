package nckbill.turnbasedfinal;

import Unit.Unit;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;

public class StartUI extends VBox {
    private Button startButton;
    private Button startButtonAI;
    private String selectedClass = "Tank"; // Default choice
    private int selectedMap = 0; // Default map
    private final GameGUI gui;

    public StartUI(GameGUI gui) {
        this.gui = gui;
        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Label title = new Label("Select Your Character");
        title.setFont(new Font("Arial", 30));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Character Selection Row
        HBox selectionBox = createSelectionBox();

        Label mapTitle = new Label("Select Map");
        mapTitle.setFont(new Font("Arial", 30));
        mapTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Map Selection Row
        HBox mapSelectionBox = createMapSelectionBox();

        initializeButtons();

        this.getChildren().addAll(title, selectionBox, mapTitle, mapSelectionBox, startButton, startButtonAI);
    }

    private HBox createMapSelectionBox() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);

        Button[] mapButtons = new Button[4];
        for (int i = 0; i < 4; i++) {
            int mapIndex = i;
            String path = "/assets/map-thumbnails/map-thumbnail" + (i + 1) + ".png";
            javafx.scene.image.ImageView thumb = new javafx.scene.image.ImageView(ImageCache.getImage(path));
            thumb.setFitWidth(100);
            thumb.setFitHeight(100);

            Button mapBtn = new Button();
            mapBtn.setGraphic(thumb);
            mapBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #7f8c8d; -fx-border-width: 3px; -fx-padding: 0;");
            
            mapButtons[i] = mapBtn;

            mapBtn.setOnAction(e -> {
                selectedMap = mapIndex;
                System.out.println("Selected Map: " + (mapIndex + 1));
                for (Button btn : mapButtons) {
                    btn.setStyle("-fx-background-color: transparent; -fx-border-color: #7f8c8d; -fx-border-width: 3px; -fx-padding: 0;");
                }
                mapBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-width: 3px; -fx-padding: 0;");
            });
            box.getChildren().add(mapBtn);
        }
        mapButtons[selectedMap].setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-width: 3px; -fx-padding: 0;");
        return box;
    }

    // Initialize buttons
    private void initializeButtons() {
        this.startButton = new Button("Enter Game");
        this.startButton.setPrefSize(150, 20);
        this.startButton.setStyle("-fx-font-size: 18px; -fx-base: #2ecc71; -fx-text-fill: white;");

        this.startButtonAI = new Button("Enter AI-only Game");
        this.startButtonAI.setPrefSize(200, 20);
        this.startButtonAI.setStyle("-fx-font-size: 18px; -fx-base: #2ecc71; -fx-text-fill: white;");

        // start game with player chosen unit, and 3 others
        // enemy team is random
        this.getStartButton().setOnAction(event -> {
            String choice = this.getSelectedClass();
            gui.getGameManager().resetGame();
            gui.getGameManager().getBackendGrid().loadMap(selectedMap);
            transitionToGame();

            UnitGenerator unitGenerator = new UnitGenerator();
            List<Unit> units = unitGenerator.generate(gui.getGameManager(), choice);

            startGame(units);
        });
        // start game with 4 random units
        this.getStartButtonAI().setOnAction(e -> {
            gui.getGameManager().resetGame();
            gui.getGameManager().getBackendGrid().loadMap(selectedMap);

            transitionToGame();

            UnitGenerator unitGenerator = new UnitGenerator();
            List<Unit> units = unitGenerator.setupAI(gui.getGameManager());

            startGame(units);
        });
    }

    private void startGame(List<Unit> units) {
        gui.refreshVisualGrid();
        gui.getGameManager().startGame(units);
    }

    private void transitionToGame() {
        gui.getRootLayout().setCenter(gui.getInteractiveGrid());
        gui.showGameGUI();
    }

    // Create selection boxes (button) to choose 1 of 4 classes:
    // Tank, Mage, Healer, Rogue
    private HBox createSelectionBox() {
        HBox selectionBox = new HBox(15);
        selectionBox.setAlignment(Pos.CENTER);

        String[] classes = {"Tank", "Mage", "Healer", "Rogue"};
        for (String className : classes) {
            Button classBtn = new Button(className);
            classBtn.setPrefSize(100, 40);
            classBtn.setOnAction(e -> {
                selectedClass = className;
                System.out.println("Selected: " + className);
            });
            selectionBox.getChildren().add(classBtn);
        }

        return selectionBox;
    }
    public String getSelectedClass() {
        return selectedClass;
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getStartButtonAI() {
        return startButtonAI;
    }
}
