package nckbill.turnbasedfinal.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import nckbill.turnbasedfinal.GameGUI;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Read from assets/text/tutorial.txt and display tutorial.
 */
public class TutorialUI extends BorderPane {
    private final GameGUI gui;
    public TutorialUI(GameGUI gui) {
        this.gui = gui;
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        this.setPadding(new Insets(30, 50, 30, 50));

        // Title
        Label title = new Label("How to Play");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: white;");
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(0, 0, 20, 0));
        this.setTop(title);

        // Content
        VBox contentBox = new VBox(20);
        contentBox.setStyle("-fx-background-color: transparent;");
        contentBox.setAlignment(Pos.TOP_LEFT);

        // Read from the file
        loadTutorialFromFile(contentBox);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 10;");
        this.setCenter(scrollPane);

        // Back Button
        Button backBtn = new Button("Back to Menu");
        backBtn.setPrefSize(200, 40);
        backBtn.setStyle("-fx-font-size: 18px; -fx-base: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        backBtn.setOnAction(e -> gui.getRootLayout().setCenter(new StartUI(gui)));

        VBox bottomBox = new VBox(backBtn);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        this.setBottom(bottomBox);
    }

    private void loadTutorialFromFile(VBox contentBox) {
        try (InputStream is = getClass().getResourceAsStream("/assets/text/tutorial.txt")) {
            if (is == null) {
                System.out.println("Could not find tutorial.txt!");
                contentBox.getChildren().add(createSection("Error", "Tutorial file is missing."));
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            String currentTitle = "";
            StringBuilder currentBody = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("# ")) {
                    if (!currentTitle.isEmpty()) {
                        contentBox.getChildren().add(createSection(currentTitle, currentBody.toString().trim()));
                        currentBody.setLength(0);
                    }
                    currentTitle = line.substring(2);
                } else {
                    currentBody.append(line).append("\n");
                }
            }

            if (!currentTitle.isEmpty()) {
                contentBox.getChildren().add(createSection(currentTitle, currentBody.toString().trim()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            contentBox.getChildren().add(createSection("Error", "Failed to load tutorial: " + e.getMessage()));
        }
    }

    private VBox createSection(String titleText, String bodyText) {
        VBox section = new VBox(8);

        Label header = new Label(titleText);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        header.setStyle("-fx-text-fill: #3498db;");

        Label body = new Label(bodyText);
        body.setFont(Font.font("Arial", 16));
        body.setStyle("-fx-text-fill: #ecf0f1; -fx-line-spacing: 5px;");
        body.setWrapText(true);
        body.setMaxWidth(800);

        section.getChildren().addAll(header, body);
        return section;
    }
}