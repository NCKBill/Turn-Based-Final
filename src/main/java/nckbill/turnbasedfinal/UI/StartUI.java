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

public class StartUI extends VBox {
    private Button startButton;
    private Button startButtonAI;
    private Button tutorialButton;
    private String selectedClass = "Tank";
    private int selectedMap = 0;
    private final GameGUI gui;

    private static final String STYLE_CLASS_UNSELECTED = "-fx-background-color: transparent; -fx-border-color: #7f8c8d; -fx-border-width: 3px; -fx-padding: 8;";
    private static final String STYLE_CLASS_SELECTED = "-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 3px; -fx-padding: 8;";
    private static final String STYLE_MAP_UNSELECTED = "-fx-background-color: transparent; -fx-border-color: #7f8c8d; -fx-border-width: 3px; -fx-padding: 0;";
    private static final String STYLE_MAP_SELECTED = "-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-width: 3px; -fx-padding: 0;";

    public StartUI(GameGUI gui) {
        this.gui = gui;
        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Label title = new Label("Select Your Character");
        title.setFont(new Font("Arial", 30));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        HBox selectionBox = createSelectionBox();

        Label mapTitle = new Label("Select Map");
        mapTitle.setFont(new Font("Arial", 30));
        mapTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

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
            mapBtn.setStyle(STYLE_MAP_UNSELECTED);
            mapButtons[i] = mapBtn;

            mapBtn.setOnAction(e -> {
                selectedMap = mapIndex;
                System.out.println("Selected Map: " + (mapIndex + 1));
                for (Button btn : mapButtons) {
                    btn.setStyle(STYLE_MAP_UNSELECTED);
                }
                mapBtn.setStyle(STYLE_MAP_SELECTED);
            });
            box.getChildren().add(mapBtn);
        }
        mapButtons[selectedMap].setStyle(STYLE_MAP_SELECTED);
        return box;
    }

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

        this.getStartButton().setOnAction(event -> {
            String choice = this.getSelectedClass();
            gui.getGameManager().resetGame();
            gui.getGameManager().getBackendGrid().loadMap(selectedMap);
            transitionToGame();

            UnitGenerator unitGenerator = new UnitGenerator();
            List<Unit> units = unitGenerator.generate(gui.getGameManager(), choice);
            startGame(units);
        });

        this.getStartButtonAI().setOnAction(e -> {
            gui.getGameManager().resetGame();
            gui.getGameManager().getBackendGrid().loadMap(selectedMap);
            transitionToGame();

            UnitGenerator unitGenerator = new UnitGenerator();
            List<Unit> units = unitGenerator.setupAI(gui.getGameManager());
            startGame(units);
        });

        this.tutorialButton.setOnAction(e -> gui.getRootLayout().setCenter(new TutorialUI(gui)));
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

    private HBox createSelectionBox() {
        HBox selectionBox = new HBox(20);
        selectionBox.setAlignment(Pos.CENTER);

        Controller playerController = new PlayerController(gui.getGameManager());
        Unit[] classes = {
                new Tank(true, playerController),
                new Mage(true, playerController),
                new Healer(true, playerController),
                new Rogue(true, playerController)
        };
        Button[] classButtons = new Button[classes.length];

        for (int i = 0; i < classes.length; i++) {
            String className = classes[i].getClass().getSimpleName();

            Label nameLabel = new Label(className);
            nameLabel.setStyle("""
                        -fx-text-fill: white;
                        -fx-font-size: 18px;
                        -fx-font-weight: bold;
                    """);

            javafx.scene.image.ImageView portrait =
                    new javafx.scene.image.ImageView(ImageCache.getImage(classes[i].getImagePath()));
            portrait.setFitWidth(80);
            portrait.setFitHeight(80);
            portrait.setPreserveRatio(true);

            VBox content = new VBox(8);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(nameLabel, portrait);

            Button classBtn = new Button();
            classBtn.setGraphic(content);
            classBtn.setStyle(STYLE_CLASS_UNSELECTED);
            classButtons[i] = classBtn;

            classBtn.setOnAction(e -> {
                selectedClass = className;
                for (Button btn : classButtons) {
                    btn.setStyle(STYLE_CLASS_UNSELECTED);
                }
                classBtn.setStyle(STYLE_CLASS_SELECTED);
                System.out.println("Selected: " + className);
            });

            selectionBox.getChildren().add(classBtn);
        }

        // Default selection highlight
        classButtons[0].setStyle(STYLE_CLASS_SELECTED);
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