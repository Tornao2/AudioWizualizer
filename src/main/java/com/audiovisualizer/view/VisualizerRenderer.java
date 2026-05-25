package com.audiovisualizer.view;

import com.audiovisualizer.model.ColorLevel;
import com.audiovisualizer.model.VisualizerSettings;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

public class VisualizerRenderer {
    private Canvas canvas;
    private GraphicsContext gc;
    private float[] lastMagnitudes = null;
    private double[] smoothedHeights = new double[256];
    private int lastNumBars = 0;

    private static final double SMOOTHING_FACTOR = 1;

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void clearCanvas() {
        if (gc == null || canvas == null) return;
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        gc.clearRect(0, 0, w, h);
    }

    public void drawSpectrum(float[] magnitudes, VisualizerSettings settings) {
        if (gc == null || canvas == null || magnitudes == null) return;
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        lastMagnitudes = magnitudes.clone();
        clearCanvas();
        int availableBands = magnitudes.length;
        int numBars = Math.min(settings.getBarsCount(), availableBands);
        if (numBars <= 0) return;
        if (numBars != lastNumBars) {
            int resetLen = Math.max(numBars, lastNumBars);
            if (smoothedHeights.length < resetLen) {
                smoothedHeights = new double[resetLen];
            }
            Arrays.fill(smoothedHeights, 0, resetLen, 0.0);
            lastNumBars = numBars;
        }
        double gap = settings.getBarsGap();
        double barWidth = Math.max(1.0, (w - (numBars - 1) * gap) / numBars);
        List<ColorLevel> colors = settings.getBarColors();
        if (colors == null || colors.isEmpty()) {
            colors = List.of(new ColorLevel("default", "#00FF00"));
        }
        int usableColors = Math.min(settings.getColorsCount(), colors.size());
        Color[] colorCache = new Color[usableColors];
        for (int i = 0; i < usableColors; i++) {
            colorCache[i] = Color.web(colors.get(i).getHexCode());
        }
        double maxBarHeight = settings.isMirrorMode() ? h / 2.0 : h;
        int bandSize = Math.max(1, availableBands / numBars);
        int currentColorIdx = -1;
        for (int i = 0; i < numBars; i++) {
            float sum = 0;
            int start = i * bandSize;
            int end = Math.min(start + bandSize, availableBands);
            for (int j = start; j < end; j++) sum += magnitudes[j];
            double magnitude = (end > start) ? (sum / (end - start)) : -60.0;
            double normalized = Math.max(0, Math.min(1, (magnitude + 60) / 60.0));
            double rawHeight = Math.pow(normalized, 0.7) * h * settings.getSensitivity();
            rawHeight = Math.max(0, Math.min(rawHeight/ (settings.isMirrorMode() ? 2 : 1), maxBarHeight));
            smoothedHeights[i] += (rawHeight - smoothedHeights[i]) * SMOOTHING_FACTOR;
            double barH = smoothedHeights[i];
            double colorProgress = Math.max(0, Math.min(1.0, barH / maxBarHeight));
            int colorIndex = Math.min(usableColors - 1, (int)(colorProgress * usableColors));
            if (colorIndex != currentColorIdx) {
                gc.setFill(colorCache[colorIndex]);
                currentColorIdx = colorIndex;
            }
            double x = i * (barWidth + gap);
            if (settings.isMirrorMode()) {
                double centerY = h / 2.0;
                gc.fillRect(x, centerY - barH, barWidth, barH);
                gc.fillRect(x, centerY, barWidth, barH);
            } else {
                gc.fillRect(x, h - barH, barWidth, barH);
            }
        }
    }

    public void redrawLast(VisualizerSettings settings) {
        if (lastMagnitudes == null) {
            lastMagnitudes = new float[128];
            Arrays.fill(lastMagnitudes, -60.0f);
        }
        drawSpectrum(lastMagnitudes, settings);
    }
}