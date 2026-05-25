package com.audiovisualizer.model;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private final List<Track> tracks = new ArrayList<>();
    private int currentIndex = -1;

    public Playlist() {}

    public void addTrack(Track track) {
        if (track == null || tracks.contains(track)) return;
        tracks.add(track);
        if (currentIndex == -1) currentIndex = 0;
    }

    public void removeTrack(int index) {
        if (index < 0 || index >= tracks.size()) return;
        tracks.remove(index);
        if (tracks.isEmpty()) {
            currentIndex = -1;
        } else if (currentIndex >= tracks.size()) {
            currentIndex = tracks.size() - 1;
        }
    }

    public void clear() {
        tracks.clear();
        currentIndex = -1;
    }

    public Track getCurrentTrack() {
        return (currentIndex >= 0 && currentIndex < tracks.size()) ? tracks.get(currentIndex) : null;
    }

    public String getCurrentIndexInfo() {
        if (tracks.isEmpty()) return "0 / 0";
        return (currentIndex + 1) + " / " + tracks.size();
    }

    public List<Track> getTracks() { return tracks; }
    public int getCurrentIndex() { return currentIndex; }

    public void setCurrentIndex(int index) {
        if (index >= 0 && index < tracks.size()) {
            this.currentIndex = index;
        } else if (index == -1) {
            this.currentIndex = -1;
        }
    }
}