package nckbill.turnbasedfinal.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.Objects;

public final class AudioManager {

    private static MediaPlayer bgMusicPlayer;

    private AudioManager() {
    }

    /**
     * Play looping background music
     */
    public static void playBGM(String resourcePath) {
        try {
            stopBGM();

            Media media = new Media(
                    Objects.requireNonNull(AudioManager.class
                                    .getResource(resourcePath))
                            .toExternalForm()
            );

            bgMusicPlayer = new MediaPlayer(media);

            bgMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgMusicPlayer.setVolume(0.35);

            bgMusicPlayer.play();

        } catch (Exception e) {
            System.err.println("Could not play music: " + resourcePath);
            e.printStackTrace();
        }
    }

    /**
     * Pause current music
     */
    public static void pauseBGM() {
        if (bgMusicPlayer != null) {
            bgMusicPlayer.pause();
        }
    }

    /**
     * Resume paused music
     */
    public static void resumeBGM() {
        if (bgMusicPlayer != null) {
            bgMusicPlayer.play();
        }
    }

    /**
     * Stop current music
     */
    public static void stopBGM() {
        if (bgMusicPlayer != null) {
            bgMusicPlayer.stop();
            bgMusicPlayer.dispose();
            bgMusicPlayer = null;
        }
    }

    /**
     * Fade out music over duration in seconds
     * then stop
     */
    public static void fadeOutBGM(double seconds) {
        if (bgMusicPlayer == null) return;

        MediaPlayer playerToFade = bgMusicPlayer;

        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(playerToFade.volumeProperty(), playerToFade.getVolume())),
                new KeyFrame(Duration.seconds(seconds),
                        new KeyValue(playerToFade.volumeProperty(), 0.0))
        );

        fadeOut.setOnFinished(e -> {
            playerToFade.stop();
            playerToFade.dispose();
            if (bgMusicPlayer == playerToFade) bgMusicPlayer = null;
        });

        fadeOut.play();
    }

    /**
     * Adjust volume between 0.0 and 1.0
     */
    public static void setVolume(double volume) {
        if (bgMusicPlayer != null) {
            bgMusicPlayer.setVolume(volume);
        }
    }
}