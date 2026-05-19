package com.audiovisualizer.model;

public class Track {
    private final String filePath;
    private final String title;
    private final String duration;
    public Track(String filePath, String title, String duration) {
        this.filePath = filePath;
        this.title = title;
        this.duration = duration;
    }
    public String getFilePath() { return filePath; }
    public String getTitle() { return title; }
    public String getDuration() { return duration; }
    @Override
    public String toString() {
        return title + " (" + duration + ")";
    }
}