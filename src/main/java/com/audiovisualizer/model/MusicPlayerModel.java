package com.audiovisualizer.model;

import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MusicPlayerModel {
    private MediaPlayer mediaPlayer;
    private final Playlist playlist = new Playlist();
    private double volume = 0.5;

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void stopAndReset() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void skipTime(double seconds) {
        if (mediaPlayer != null) {
            Duration current = mediaPlayer.getCurrentTime();
            Duration target = current.add(Duration.seconds(seconds));
            mediaPlayer.seek(target);
        }
    }

    public void setVolume(double val) {
        this.volume = val;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(val);
        }
    }

    public Playlist getPlaylist() { return playlist; }
}