package com.audiovisualizer.model;

import java.util.Objects;

public class Track {
    private String filePath;
    private String title;
    private String duration;
    public Track() {}
    public Track(String filePath, String title, String duration) {
        this.filePath = filePath != null ? filePath : "";
        this.title = title != null ? title : "Nieznany tytuł";
        this.duration = duration != null ? duration : "00:00";
    }
    public String getFilePath() { return filePath; }
    public String getTitle() { return title; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) {
        this.duration = duration != null ? duration : "00:00";
    }
    @Override
    public String toString() {
        return title + " (" + duration + ")";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track track)) return false;
        return Objects.equals(filePath, track.filePath);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(filePath);
    }
}