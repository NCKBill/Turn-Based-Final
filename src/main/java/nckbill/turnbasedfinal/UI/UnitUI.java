package nckbill.turnbasedfinal.UI;

import Unit.Unit;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nckbill.turnbasedfinal.GameGUI;
import nckbill.turnbasedfinal.utils.ImageCache;

/**
 * Contain UI for unit in battle
 * Each unit wrap in a container containing their image + health bar
 * Health-bar will update when unit is damaged/healed
 * Play action animation (sprite move toward enemy, then back)
 */
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

        String hpBarColor = "blue";
        if (unit.isFriendly())
            hpBarColor = unit.getUnitController().getClass().getSimpleName().equals("PlayerController") ? "green" : "blue";
        else hpBarColor = "red";

        this.hpBar.setStyle(
                "-fx-accent: " + hpBarColor + "; " +
                        "-fx-control-inner-background: transparent; " +
                        "-fx-box-border: transparent; " +
                        "-fx-background-color: transparent; " +
                        "-fx-background-insets: 0; " +
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

    public void playActionAnimation(double deltaX, double deltaY, Runnable onComplete) {
        this.setTranslateX(0);
        this.setTranslateY(0);

        double duration = 150 / GameGUI.getGameSpeed();
        TranslateTransition forward = new TranslateTransition(Duration.millis(duration), this);
        forward.setByX(deltaX);
        forward.setByY(deltaY);

        TranslateTransition back = new TranslateTransition(Duration.millis(duration), this);
        back.setByX(-deltaX);
        back.setByY(-deltaY);

        SequentialTransition attackAnimation = new SequentialTransition(forward, back);

        attackAnimation.setOnFinished(e -> {
            this.setTranslateX(0);
            this.setTranslateY(0);
            if (onComplete != null) onComplete.run();
        });

        attackAnimation.play();
    }

    // Glowing around unit sprite
    public void setActiveHighlight(boolean isActive) {
        if (isActive) {
            DropShadow glow = new DropShadow();
            // Friendly units glow Yellow, enemies glow Orange
            glow.setColor(unit.isFriendly() ? javafx.scene.paint.Color.YELLOW : javafx.scene.paint.Color.ORANGE);
            glow.setRadius(15);
            glow.setSpread(0.6);
            this.unitImage.setEffect(glow);
        } else {
            this.unitImage.setEffect(null); // Remove glow when turn ends
        }
    }
}