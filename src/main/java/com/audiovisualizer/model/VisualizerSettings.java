package com.audiovisualizer.model;

import java.util.ArrayList;
import java.util.List;

public class VisualizerSettings {
    private int barsCount = 64;
    private double sensitivity = 1.0;
    private int barsGap = 2;
    private int colorsCount = 5;
    private boolean isMirrorMode = false;
    private boolean isVerticalDirection = true;
    private List<ColorLevel> barColors = new ArrayList<>();
    private List<ColorLevel> backgroundColors = new ArrayList<>();

    public VisualizerSettings() {
        barColors.add(new ColorLevel("Start", "#00bf63"));
        barColors.add(new ColorLevel("Niski", "#00e676"));
        barColors.add(new ColorLevel("Średni", "#ffea00"));
        barColors.add(new ColorLevel("Wysoki", "#ff9100"));
        barColors.add(new ColorLevel("Ekstremalny", "#ff1744"));
        backgroundColors.add(new ColorLevel("Góra", "#201d40"));
        backgroundColors.add(new ColorLevel("Dół", "#080819"));
    }

    public void applyBackgroundPreset(int presetId) {
        backgroundColors.clear();
        switch (presetId) {
            case 1:
                backgroundColors.add(new ColorLevel("Góra", "#7b2d9f"));
                backgroundColors.add(new ColorLevel("Dół", "#fdd82e"));
                break;
            case 2:
                backgroundColors.add(new ColorLevel("Góra", "#5de0e6"));
                backgroundColors.add(new ColorLevel("Dół", "#004aad"));
                break;
            default:
                backgroundColors.add(new ColorLevel("Góra", "#201d40"));
                backgroundColors.add(new ColorLevel("Dół", "#080819"));
                break;
        }
    }

    public void updateBackgroundColor(int index, String newHex) {
        if (index >= 0 && index < backgroundColors.size()) {
            backgroundColors.get(index).setHexCode(newHex);
        }
    }

    public int getBarsCount() { return barsCount; }
    public void setBarsCount(int barsCount) { this.barsCount = barsCount; }
    public double getSensitivity() { return sensitivity; }
    public void setSensitivity(double sensitivity) { this.sensitivity = sensitivity; }
    public int getBarsGap() { return barsGap; }
    public int getColorsCount() { return colorsCount; }
    public void setColorsCount(int colorsCount) { this.colorsCount = colorsCount; }
    public boolean isMirrorMode() { return isMirrorMode; }
    public void setBarsGap(int gap) {this.barsGap=gap;}
    public void setMirrorMode(boolean mirrorMode) { this.isMirrorMode = mirrorMode; }
    public boolean isVerticalDirection() { return isVerticalDirection; }
    public void setVerticalDirection(boolean verticalDirection) { this.isVerticalDirection = verticalDirection; }
    public List<ColorLevel> getBarColors() { return barColors; }
    public List<ColorLevel> getBackgroundColors() { return backgroundColors; }
}