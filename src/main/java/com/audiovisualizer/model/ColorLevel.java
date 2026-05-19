package com.audiovisualizer.model;

public class ColorLevel {
    private String levelName;
    private String hexCode;

    public ColorLevel() {}

    public ColorLevel(String levelName, String hexCode) {
        this.levelName = levelName;
        this.hexCode = hexCode;
    }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public String getHexCode() { return hexCode; }
    public void setHexCode(String hexCode) { this.hexCode = hexCode; }
}