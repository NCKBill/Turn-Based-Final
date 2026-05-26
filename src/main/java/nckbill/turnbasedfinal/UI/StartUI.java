package nckbill.turnbasedfinal.UI;

import Controller.Controller;
import Controller.PlayerController;
import Unit.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import nckbill.turnbasedfinal.GameGUI;
import nckbill.turnbasedfinal.utils.ImageCache;
import nckbill.turnbasedfinal.utils.UnitGenerator;

import java.util.List;

/**
 * Start menu of the game.
 * <p>
 * Visual composition:
 * Character selection: select from 4 characters portraits (Tank, Rogue, Mage, Healer)
 * Map Selection: select from 4 pre-determined maps (grass, trees, mixed, mountain)
 * Game-mode selection
 * Normal: Load into a battle with
 * 1 player-controlled unit
 * 3 allied AI units in the bottom of the map
 * 4 enemy AI units in the top of the map
 * AI-only: Load into an AI-only battle as a demo
 * Tutorial: switch to TutorialUI
 */
public class StartUI extends VBox {
    private Button startButton;
    private Button startButtonAI;
    private Button tutorialButton;
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

        this.getChildren().addAll(title, selectionBox, mapTitle, mapSelectionBox, startButton, startButtonAI, tutorialButton);
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

        this.tutorialButton = new Button("Tutorial");
        this.tutorialButton.setPrefSize(150, 20);
        this.tutorialButton.setStyle("-fx-font-size: 18px; -fx-base: #3498db; -fx-text-fill: white;");
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

        this.tutorialButton.setOnAction(e -> {
            gui.getRootLayout().setCenter(new TutorialUI(gui));
        });
    }

    private void startGame(List<Unit> units) {
        gui.refreshVisualGrid();
        gui.getGameManager().startGame(units);
    }

    private void transitionToGame() {
        gui.playBGMWithFade("/assets/audio/battle-theme-" + selectedMap + ".mp3");
        gui.getRootLayout().setCenter(gui.getInteractiveGrid());
        gui.showGameGUI();
    }

    // Create selection boxes (button) to choose 1 of 4 classes:
    // Tank, Mage, Healer, Rogue
    private HBox createSelectionBox() {
        HBox selectionBox = new HBox(20);
        selectionBox.setAlignment(Pos.CENTER);

        Controller playerController = new PlayerController(gui.getGameManager());
        Unit[] classes = {new Tank(true, playerController), new Mage(true, playerController), new Healer(true, playerController), new Rogue(true, playerController)};
        Button[] classButtons = new Button[classes.length];

        for (int i = 0; i < classes.length; i++) {
            String className = classes[i].getClass().getSimpleName();

            Label nameLabel = new Label(className);
            nameLabel.setStyle("""
                        -fx-text-fill: white;
                        -fx-font-size: 18px;
                        -fx-font-weight: bold;
                    """);

            String imagePath = classes[i].getImagePath();

            javafx.scene.image.ImageView portrait =
                    new javafx.scene.image.ImageView(
                            ImageCache.getImage(imagePath)
                    );

            portrait.setFitWidth(80);
            portrait.setFitHeight(80);
            portrait.setPreserveRatio(true);

            VBox content = new VBox(8);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(nameLabel, portrait);

            Button classBtn = new Button();
            classBtn.setGraphic(content);

            classBtn.setStyle("""
                        -fx-background-color: transparent;
                        -fx-border-color: #7f8c8d;
                        -fx-border-width: 3px;
                        -fx-padding: 8;
                    """);

            classButtons[i] = classBtn;

            classBtn.setOnAction(e -> {
                selectedClass = className;

                for (Button btn : classButtons) {
                    btn.setStyle("""
                                -fx-background-color: transparent;
                                -fx-border-color: #7f8c8d;
                                -fx-border-width: 3px;
                                -fx-padding: 8;
                            """);
                }

                classBtn.setStyle("""
                            -fx-background-color: transparent;
                            -fx-border-color: white;
                            -fx-border-width: 3px;
                            -fx-padding: 8;
                        """);

                System.out.println("Selected: " + className);
            });

            selectionBox.getChildren().add(classBtn);
        }

        classButtons[0].setStyle("""
                    -fx-background-color: transparent;
                    -fx-border-color: white;
                    -fx-border-width: 3px;
                    -fx-padding: 8;
                """);

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
