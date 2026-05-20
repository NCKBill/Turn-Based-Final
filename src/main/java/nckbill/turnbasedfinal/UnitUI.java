package nckbill.turnbasedfinal;

import Unit.Unit;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UnitUI extends VBox {
    private final Unit unit;
    private final ImageView unitImage;
    private final ProgressBar hpBar;

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
        this.hpBar.setPrefWidth(40);
        this.hpBar.setPrefHeight(10);
        hpBar.setStyle("-fx-accent: #cc2929;");

        this.getChildren().addAll(unitImage, hpBar);

        // Set the initial HP state
        updateHP();
    }

    public void updateHP() {
        double hpPercentage = (double) unit.getHP() / unit.getMaxHP();
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