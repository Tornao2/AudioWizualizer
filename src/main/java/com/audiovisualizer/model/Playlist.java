package com.audiovisualizer.model;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private final List<Track> tracks = new ArrayList<>();
    private int currentIndex = -1;

    public void addTrack(Track track) {
        tracks.add(track);
        if (currentIndex == -1) {
            currentIndex = 0;
        }
    }

    public void removeTrack(int index) {
        if (index >= 0 && index < tracks.size()) {
            tracks.remove(index);
            if (tracks.isEmpty()) {
                currentIndex = -1;
            } else if (currentIndex >= tracks.size()) {
                currentIndex = tracks.size() - 1;
            }
        }
    }

    public String getCurrentIndexInfo() {
        if (tracks.isEmpty()) return "0/0";
        return (currentIndex + 1) + "/" + tracks.size();
    }

    public List<Track> getTracks() { return tracks; }
    public int getCurrentIndex() { return currentIndex; }
    public void setCurrentIndex(int index) { this.currentIndex = index; }
}