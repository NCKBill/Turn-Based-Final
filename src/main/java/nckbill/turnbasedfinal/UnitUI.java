package nckbill.turnbasedfinal;

import Unit.Unit;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UnitUI extends VBox {
    private final Unit unit;
    private final ImageView unitImage;
    private final ProgressBar hpBar;
    private Label hpText;

    public UnitUI(Unit unit) {
        this.unit = unit;

        this.setAlignment(Pos.CENTER);
        this.setSpacing(2);

        // Fetch image
        String imagePath = unit.getImagePath();
        Image img = ImageCache.getImage(imagePath);

        if (img != null) {
            this.unitImage = new ImageView(img);
        } else {
            this.unitImage = new ImageView();
            System.out.println("Failed to load visual for " + unit.getName());
        }

        this.unitImage.setFitWidth(40);
        this.unitImage.setFitHeight(40);

        // HP Bar
        this.hpBar = new ProgressBar();
        this.hpBar.setPrefWidth(50);
        this.hpBar.setPrefHeight(10);
        this.hpBar.setStyle(
                "-fx-accent: red; " +
                        "-fx-control-inner-background: transparent; " +
                        "-fx-box-border: transparent; " +
                        "-fx-background-color: black; " +
                        "-fx-padding: 0;"
        );
        // HP text
        this.hpText = new Label();
        this.hpText.setStyle("-fx-font-size: 7px; -fx-font-weight: bold; -fx-text-fill: white");
        StackPane hpContainer = new StackPane(hpBar, hpText);

        this.getChildren().addAll(unitImage, hpContainer);

        // Set the initial HP state
        updateHP();
    }

    public void updateHP() {
        double hpPercentage = (double) unit.getHP() / unit.getMaxHP();
        hpText.setText(unit.getHP() + "/" + unit.getMaxHP());
        hpBar.setProgress(hpPercentage);
    }

    public Unit getUnit() {
        return unit;
    }

    public void playAttackAnimation(double deltaX, double deltaY) {
        TranslateTransition forward = new TranslateTransition(Duration.millis(150), this);
        forward.setByX(deltaX);
        forward.setByY(deltaY);

        TranslateTransition back = new TranslateTransition(Duration.millis(150), this);
        back.setByX(-deltaX);
        back.setByY(-deltaY);

        SequentialTransition attackAnimation = new SequentialTransition(forward, back);

        attackAnimation.play();
    }
}