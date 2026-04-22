package nckbill.turnbasedfinal;

import Controller.GameManager;
import Unit.Unit;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.util.Queue;

public class TopBarUI extends HBox {
    private GameManager gameManager;
    private GameGUI mainGUI;
    private HBox queueDisplayContainer;

    public TopBarUI(GameGUI mainGUI) {
        this.mainGUI = mainGUI;

        this.setSpacing(15);
        this.setStyle("-fx-background-color: #d3d3d3; -fx-padding: 10px;");
        this.setMinHeight(60);

        Label queueTitle = new Label("Turn Queue: ");
        queueTitle.setStyle("-fx-font-weight: bold;");

        queueDisplayContainer = new HBox(10);
        this.getChildren().addAll(queueTitle, queueDisplayContainer);
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void updateTurnDisplay(Unit active) {
        queueDisplayContainer.getChildren().clear();

        if (active != null) {
            Queue<Unit> turnQueue = gameManager.getTurnManager().getTurnQueue();

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

            gameManager.setSelectedViewUnit(active);
            mainGUI.updateSidebarStats(active);
            mainGUI.getBottomBar().updateBottomBarSkills(active);
        } else {
            queueDisplayContainer.getChildren().add(new Label("Calculating next round..."));
            mainGUI.getBottomBar().getChildren().clear();
        }

        mainGUI.refreshVisualGrid();
    }

    private void handleTopBarUnitClick(Unit clickedUnit) {
        gameManager.setSelectedViewUnit(clickedUnit);
        mainGUI.updateSidebarStats(clickedUnit);
        mainGUI.getBottomBar().updateBottomBarSkills(clickedUnit);
    }
}