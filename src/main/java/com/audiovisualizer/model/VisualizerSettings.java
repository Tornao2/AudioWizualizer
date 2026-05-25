package com.audiovisualizer.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private int activePresetIndex = -1;
    private List<BackgroundPreset> presets = new ArrayList<>();

    private static final Path SETTINGS_PATH = Paths.get(System.getProperty("user.home"), ".audiovisualizer", "settings.json");

    public VisualizerSettings() {
        barColors.add(new ColorLevel("Start", "#00bf63"));
        barColors.add(new ColorLevel("Niski", "#00e676"));
        barColors.add(new ColorLevel("Średni", "#ffea00"));
        barColors.add(new ColorLevel("Wysoki", "#ff9100"));
        barColors.add(new ColorLevel("Ekstremalny", "#ff1744"));
        backgroundColors.add(new ColorLevel("Kolor A", "#201d40"));
        backgroundColors.add(new ColorLevel("Kolor B", "#080819"));
        presets.add(new BackgroundPreset("Preset 1", "#201d40", "#080819"));
        presets.add(new BackgroundPreset("Preset 2", "#7b2d9f", "#fdd82e"));
        presets.add(new BackgroundPreset("Preset 3", "#5de0e6", "#004aad"));
    }

    public static VisualizerSettings load() {
        if (Files.exists(SETTINGS_PATH)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(SETTINGS_PATH.toFile(), VisualizerSettings.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new VisualizerSettings();
    }

    public void save() {
        try {
            Files.createDirectories(SETTINGS_PATH.getParent());
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(SETTINGS_PATH.toFile(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncCurrentColorsToActivePreset() {
        if (activePresetIndex >= 0 && activePresetIndex < presets.size() && backgroundColors.size() >= 2) {
            BackgroundPreset p = presets.get(activePresetIndex);
            p.setColorTop(backgroundColors.get(0).getHexCode());
            p.setColorBottom(backgroundColors.get(1).getHexCode());
        }
    }

    public void applyBackgroundPreset(int index) {
        if (index < 0 || index >= presets.size()) return;
        BackgroundPreset p = presets.get(index);
        activePresetIndex = index;
        if (backgroundColors.size() < 2) {
            backgroundColors.clear();
            backgroundColors.add(new ColorLevel("Kolor A", p.getColorTop()));
            backgroundColors.add(new ColorLevel("Kolor B", p.getColorBottom()));
        } else {
            backgroundColors.get(0).setHexCode(p.getColorTop());
            backgroundColors.get(1).setHexCode(p.getColorBottom());
        }
    }

    public int getBarsCount() { return barsCount; }
    public void setBarsCount(int barsCount) { this.barsCount = barsCount; }
    public double getSensitivity() { return sensitivity; }
    public void setSensitivity(double sensitivity) { this.sensitivity = sensitivity; }
    public int getBarsGap() { return barsGap; }
    public void setBarsGap(int barsGap) { this.barsGap = barsGap; }
    public int getColorsCount() { return colorsCount; }
    public void setColorsCount(int colorsCount) { this.colorsCount = colorsCount; }
    public boolean isMirrorMode() { return isMirrorMode; }
    public void setMirrorMode(boolean mirrorMode) { this.isMirrorMode = mirrorMode; }
    public boolean isVerticalDirection() { return isVerticalDirection; }
    public void setVerticalDirection(boolean verticalDirection) { this.isVerticalDirection = verticalDirection; }
    public List<ColorLevel> getBarColors() { return barColors; }
    public void setBarColors(List<ColorLevel> barColors) { this.barColors = barColors; }
    public List<ColorLevel> getBackgroundColors() { return backgroundColors; }
    public void setBackgroundColors(List<ColorLevel> backgroundColors) { this.backgroundColors = backgroundColors; }
    public int getActivePresetIndex() { return activePresetIndex; }
    public void setActivePresetIndex(int activePresetIndex) { this.activePresetIndex = activePresetIndex; }
    public List<BackgroundPreset> getPresets() { return presets; }
    public void setPresets(List<BackgroundPreset> presets) { this.presets = presets; }

    public static class BackgroundPreset {
        public String name;
        public String colorTop;
        public String colorBottom;

        public BackgroundPreset() {}
        public BackgroundPreset(String name, String colorTop, String colorBottom) {
            this.name = name;
            this.colorTop = colorTop;
            this.colorBottom = colorBottom;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getColorTop() { return colorTop; }
        public void setColorTop(String colorTop) { this.colorTop = colorTop; }
        public String getColorBottom() { return colorBottom; }
        public void setColorBottom(String colorBottom) { this.colorBottom = colorBottom; }
    }
}