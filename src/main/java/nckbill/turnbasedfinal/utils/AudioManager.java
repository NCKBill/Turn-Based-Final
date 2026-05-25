package nckbill.turnbasedfinal.utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

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
     * Adjust volume between 0.0 and 1.0
     */
    public static void setVolume(double volume) {
        if (bgMusicPlayer != null) {
            bgMusicPlayer.setVolume(volume);
        }
    }
}