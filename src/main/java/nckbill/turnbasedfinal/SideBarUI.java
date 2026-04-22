package nckbill.turnbasedfinal;

import Unit.Unit;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SideBarUI extends VBox {
    private Label sidebarStatsLabel;

    public SideBarUI() {
        // VBox styling and spacing
        this.setSpacing(15);
        this.setStyle("-fx-background-color: #e6e6e6; -fx-padding: 10px;");
        this.setMinWidth(180);

        Label statsTitle = new Label("Unit Stats:");
        statsTitle.setStyle("-fx-font-weight: bold;");

        sidebarStatsLabel = new Label("Hover over a unit to see stats:");
        sidebarStatsLabel.setWrapText(true);

        this.getChildren().addAll(statsTitle, sidebarStatsLabel);
    }

    public void updateSidebarStats(Unit unit) {
        if (unit != null) {
            sidebarStatsLabel.setText(unit.toString());
        } else {
        }
    }

    // Expose the label so CellUI hover events can update it directly
    public Label getSidebarStatsLabel() {
        return sidebarStatsLabel;
    }
}