package utils;

import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class GlobalVideoManager {

    private static MediaPlayer mediaPlayer;
    private static MediaView mediaView;
    private static String currentVideoFile = "";

    /**
     * Prepares the video based on the weather.
     * If the requested video is ALREADY playing, it does nothing (Zero Lag).
     * @param videoFileName
     */
    public static void updateWeatherVideo(String videoFileName) {
        // Optimization: If the correct video is already playing, don't reload it!
        if (mediaPlayer != null && currentVideoFile.equals(videoFileName)) {
            return; 
        }

        try {
            // 1. Dispose previous player to free memory
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            // 2. Load new video
            String path = GlobalVideoManager.class.getResource("/assets/" + videoFileName).toExternalForm();
            Media media = new Media(path);
            
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);
            mediaPlayer.setAutoPlay(true);

            // 3. Create or Update View
            if (mediaView == null) {
                mediaView = new MediaView(mediaPlayer);
            } else {
                mediaView.setMediaPlayer(mediaPlayer);
            }
            
            currentVideoFile = videoFileName;

        } catch (Exception e) {
            System.err.println("Error loading video: " + videoFileName);
        }
    }

    /**
     * Returns the shared MediaView.
     * It automatically removes itself from any previous screen to prevent "Duplicate Node" errors.
     * @return 
     */
    public static MediaView getSharedMediaView() {
        if (mediaView == null) return null;

        // JavaFX requires a node to be removed from its old parent before adding to a new one
        if (mediaView.getParent() != null) {
            ((Pane) mediaView.getParent()).getChildren().remove(mediaView);
        }
        return mediaView;
    }
}