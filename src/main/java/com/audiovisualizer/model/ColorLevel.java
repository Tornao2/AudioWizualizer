package com.audiovisualizer.model;

import java.util.Objects;
import java.util.regex.Pattern;

public class ColorLevel {
    private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private String levelName;
    private String hexCode;

    public ColorLevel() {}

    public ColorLevel(String levelName, String hexCode) {
        this.levelName = levelName != null ? levelName : "";
        this.hexCode = sanitizeHex(hexCode);
    }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName != null ? levelName : ""; }
    public String getHexCode() { return hexCode; }
    public void setHexCode(String hexCode) { this.hexCode = sanitizeHex(hexCode); }
    private static String sanitizeHex(String hex) {
        if (hex == null) return "#000000";
        String cleaned = hex.trim().toUpperCase();
        return HEX_PATTERN.matcher(cleaned).matches() ? cleaned : "#000000";
    }
    @Override
    public String toString() {
        return levelName + ": " + hexCode;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColorLevel that)) return false;
        return Objects.equals(levelName, that.levelName) &&
                Objects.equals(hexCode, that.hexCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(levelName, hexCode);
    }
}