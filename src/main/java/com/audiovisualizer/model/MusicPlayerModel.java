package com.audiovisualizer.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class MusicPlayerModel {
    private MediaPlayer mediaPlayer;
    private final Playlist playlist = new Playlist();
    private double volume = 0.5;
    private Track currentTrack;
    private Timeline fadeTimeline;
    private boolean isDisposed = false;

    public MusicPlayerModel() {}

    public void loadTrack(Track track) {
        if (fadeTimeline != null) {
            fadeTimeline.stop();
            fadeTimeline = null;
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setAudioSpectrumListener(null);
                if (mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN) {
                    mediaPlayer.stop();
                }
            } catch (Exception e) {
                System.err.println("Warning during player cleanup: " + e.getMessage());
            } finally {
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
        }
        this.currentTrack = track;
        isDisposed = false;
        if (track == null) return;
        try {
            File file = new File(track.getFilePath());
            if (!file.exists() || !file.canRead()) {
                throw new IllegalArgumentException("Plik nie istnieje lub nie można go odczytać: " + file.getPath());
            }
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volume);
            mediaPlayer.setOnEndOfMedia(this::handleTrackEnd);
        } catch (Exception e) {
            System.err.println("Błąd ładowania pliku audio: " + e.getMessage());
            this.currentTrack = null;
            this.mediaPlayer = null;
            isDisposed = true;
        }
    }

    private void handleTrackEnd() {
        int nextIndex = playlist.getCurrentIndex() + 1;
        if (nextIndex < playlist.getTracks().size()) {
            Platform.runLater(() -> {
                playlist.setCurrentIndex(nextIndex);
                loadTrack(playlist.getCurrentTrack());
                play();
            });
        }
    }

    public void play() {
        if (mediaPlayer == null || isDisposed) return;
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) return;
        if (fadeTimeline != null) fadeTimeline.stop();
        double currentVol = mediaPlayer.getVolume();
        if (currentVol < 0.01 && volume > 0) {
            fadeVolume(0, volume, 100, null);
        } else {
            mediaPlayer.setVolume(volume);
        }
        mediaPlayer.play();
    }

    public void pause() {
        if (mediaPlayer == null || isDisposed) return;
        if (fadeTimeline != null) fadeTimeline.stop();
        fadeVolume(mediaPlayer.getVolume(), 0, 300, () -> {
            if (mediaPlayer != null && !isDisposed) {
                mediaPlayer.pause();
            }
        });
    }

    public void stopAndReset() {
        if (mediaPlayer == null || isDisposed) return;
        if (fadeTimeline != null) fadeTimeline.stop();
        fadeVolume(mediaPlayer.getVolume(), 0, 300, null);
        mediaPlayer.pause();
        mediaPlayer.seek(Duration.ZERO);
    }

    public void setVolume(double val) {
        this.volume = Math.max(0, Math.min(1, val));
        if (mediaPlayer != null && !isDisposed) {
            if (fadeTimeline == null || fadeTimeline.getStatus() != Timeline.Status.RUNNING) {
                mediaPlayer.setVolume(this.volume);
            }
        }
    }

    private void fadeVolume(double start, double end, double durationMs, Runnable onFinished) {
        if (mediaPlayer == null || isDisposed) return;
        MediaPlayer mp = this.mediaPlayer;
        if (fadeTimeline != null) fadeTimeline.stop();
        int steps = 20;
        double stepDuration = durationMs / steps;
        double volumeDelta = (end - start) / steps;
        fadeTimeline = new Timeline();
        for (int i = 0; i <= steps; i++) {
            final double targetVol = Math.max(0, Math.min(1, start + volumeDelta * i));
            KeyFrame kf = new KeyFrame(
                    Duration.millis(stepDuration * i),
                    ae -> {
                        if (!isDisposed && mp.getStatus() != MediaPlayer.Status.DISPOSED) {
                            mp.setVolume(targetVol);
                        }
                    }
            );
            fadeTimeline.getKeyFrames().add(kf);
        }
        if (onFinished != null) {
            fadeTimeline.setOnFinished(ae -> {
                if (!isDisposed) onFinished.run();
            });
        }
        fadeTimeline.play();
    }

    public Playlist getPlaylist() { return playlist; }
    public Track getCurrentTrack() { return currentTrack; }
    public MediaPlayer getMediaPlayer() { return mediaPlayer; }
    public double getVolume() { return volume; }
    public boolean isDisposed() { return isDisposed; }
}