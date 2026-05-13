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
    private final Button startButton;
    private final Button startButtonAI;
    private String selectedClass = "Tank"; // Default choice

    public StartUI(GameGUI gameGUI) {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(30);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Label title = new Label("Select Your Hero");
        title.setFont(new Font("Arial", 40));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Character Selection Row
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

        this.startButton = new Button("Enter Game");
        this.startButton.setPrefSize(200, 40);
        this.startButton.setStyle("-fx-font-size: 18px; -fx-base: #2ecc71; -fx-text-fill: white;");

        this.startButtonAI = new Button("Enter AI only Game");
        this.startButtonAI.setPrefSize(400, 40);
        this.startButtonAI.setStyle("-fx-font-size: 18px; -fx-base: #2ecc71; -fx-text-fill: white;");

        this.getChildren().addAll(title, selectionBox, startButton, startButtonAI);

        this.getStartButton().setOnAction(event -> {
            String choice = this.getSelectedClass();
            gameGUI.getRootLayout().setCenter(gameGUI.getInteractiveGrid()); // Switch to game board
            gameGUI.showGameGUI();
            UnitGenerator unitGenerator = new UnitGenerator();
            // Use the new custom match setup
            List<Unit> units = unitGenerator.generate(gameGUI.getGameManager(), choice);

            gameGUI.refreshVisualGrid();
            gameGUI.getGameManager().startGame(units);
        });

        this.getStartButtonAI().setOnAction(e -> {
            gameGUI.getRootLayout().setCenter(gameGUI.getInteractiveGrid());
            gameGUI.showGameGUI();
            UnitGenerator unitGenerator = new UnitGenerator();
            List<Unit> units = unitGenerator.setup(gameGUI.getGameManager());
            gameGUI.refreshVisualGrid();
            gameGUI.getGameManager().startGame(units);
        });
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