package nckbill.turnbasedfinal.UI;

import Unit.Unit;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import nckbill.turnbasedfinal.GameGUI;

import java.util.Queue;

public class TopBarUI extends HBox {
    private final GameGUI gui;
    private final HBox queueDisplayContainer;

    public TopBarUI(GameGUI gui) {
        this.gui = gui;

        this.setSpacing(15);
        this.setStyle("-fx-background-color: #d3d3d3; -fx-padding: 10px;");
        this.setMinHeight(60);

        Label queueTitle = new Label("Turn Queue: ");
        queueTitle.setStyle("-fx-font-weight: bold;");

        queueDisplayContainer = new HBox(10);
        this.getChildren().addAll(queueTitle, queueDisplayContainer);
    }

    public void updateTurnDisplay(Unit active) {
        queueDisplayContainer.getChildren().clear();

        if (active != null) {
            Queue<Unit> turnQueue = gui.getGameManager().getTurnManager().getTurnQueue();

            Button activeUnitBtn = new Button(">> " + active.getName() + " <<");
            activeUnitBtn.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-border-color: green; -fx-border-radius: 3px;");
            activeUnitBtn.setOnAction(e -> handleTopBarUnitClick(active));
            queueDisplayContainer.getChildren().add(activeUnitBtn);

            for (Unit queueUnit : turnQueue) {
                if (queueUnit == active) continue;

                Button queuedUnitBtn = new Button(queueUnit.getName());
                String textColor = queueUnit.isFriendly() ? "blue" : "red";
                queuedUnitBtn.setStyle("-fx-text-fill: " + textColor + ";");

                queuedUnitBtn.setOnAction(e -> handleTopBarUnitClick(queueUnit));
                queueDisplayContainer.getChildren().add(queuedUnitBtn);
            }

            gui.getGameManager().setSelectedViewUnit(active);
            gui.updateSidebarUnitStats(active);
            gui.getBottomBar().updateBottomBarSkills(active);
        } else {
            queueDisplayContainer.getChildren().add(new Label("Calculating next round..."));
            gui.getBottomBar().getChildren().clear();
        }

        gui.refreshVisualGrid();
    }


    private void handleTopBarUnitClick(Unit clickedUnit) {
        gui.getGameManager().setSelectedViewUnit(clickedUnit);
        gui.updateSidebarUnitStats(clickedUnit);
        gui.getBottomBar().updateBottomBarSkills(clickedUnit);
    }
}