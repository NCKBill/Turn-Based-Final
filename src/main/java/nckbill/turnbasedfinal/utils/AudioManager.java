package nckbill.turnbasedfinal.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Manage looping background music
 * Contain methods for musics: play, pause, resume, change volume, etc
 * Fade out current music then play the next one
 */
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
            Media media = new Media(Objects.requireNonNull(AudioManager.class.getResource(resourcePath)).toExternalForm());
            bgMusicPlayer = new MediaPlayer(media);
            bgMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgMusicPlayer.setVolume(0.35);
            bgMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("Could not play music: " + resourcePath);
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

    public static void playBGMWithFade(double fadeDuration, double delaySeconds, String resourcePath) {
        fadeOutBGM(fadeDuration);
        javafx.application.Platform.runLater(() -> {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                    javafx.util.Duration.seconds(fadeDuration + delaySeconds)
            );
            pause.setOnFinished(e -> playBGM(resourcePath));
            pause.play();
        });
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