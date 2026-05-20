package nckbill.turnbasedfinal;

import Unit.Unit;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SideBarUI extends BorderPane {
    private Label sidebarStatsLabel;
    private VBox logMessageContainer;
    private ScrollPane logPane;

    public SideBarUI() {
        this.setStyle("-fx-background-color: #e6e6e6; -fx-padding: 10px;");
        this.setMaxWidth(300);
        this.setWidth(300);
        this.setMinWidth(300);

        // Top Section Container
        VBox topStatsContainer = new VBox(15);

        Label statsTitle = new Label("Unit Stats:");
        statsTitle.setStyle("-fx-font-weight: bold;");

        sidebarStatsLabel = new Label("Hover over a unit to see stats:");
        sidebarStatsLabel.setWrapText(true);

        topStatsContainer.getChildren().addAll(statsTitle, sidebarStatsLabel);

        this.setTop(topStatsContainer);

        // Set up the scrolling log box
        setupLogContainer();
    }

    public void updateSidebarStats(Unit unit) {
        if (unit != null) {
            sidebarStatsLabel.setText(unit.toString());
        }
    }

    public Label getSidebarStatsLabel() {
        return sidebarStatsLabel;
    }


    private void setupLogContainer() {
        logMessageContainer = new VBox(5);
        logMessageContainer.setPadding(new Insets(10));

        logPane = new ScrollPane(logMessageContainer);
        logPane.setFitToWidth(true);
        logPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        logPane.setPrefHeight(300);

        logMessageContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            logPane.setVvalue(1.0);
        });

        this.setBottom(logPane);
    }

    /**
     * Method to be called by GameGUI to drop new messages into the sidebar.
     */
    public void addLogMessage(String message) {
        Platform.runLater(() -> {
            Label logLabel = new Label(message.trim());
            logLabel.setWrapText(true);
            logLabel.setMaxWidth(Double.MAX_VALUE);

            logMessageContainer.getChildren().add(logLabel);

//            if (logMessageContainer.getChildren().size() > 50) {
//                logMessageContainer.getChildren().remove(0);
//            }
        });
    }
}