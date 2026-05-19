package com.audiovisualizer.view;

import com.audiovisualizer.model.ColorLevel;
import com.audiovisualizer.model.VisualizerSettings;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

public class VisualizerRenderer {
    private Canvas canvas;
    private GraphicsContext gc;

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void clearCanvas(List<ColorLevel> bgColors) {
        if (gc == null || canvas == null) return;
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (bgColors == null || bgColors.isEmpty()) {
            gc.setFill(Color.BLACK);
        } else if (bgColors.size() == 1) {
            gc.setFill(Color.web(bgColors.getFirst().getHexCode()));
        } else {
            List<Stop> stops = new ArrayList<>();
            for (int i = 0; i < bgColors.size(); i++) {
                double offset = (double) i / (bgColors.size() - 1);
                stops.add(new Stop(offset, Color.web(bgColors.get(i).getHexCode())));
            }
            LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
            gc.setFill(grad);
        }
        gc.fillRect(0, 0, w, h);
    }

    public void drawSpectrum(float[] magnitudes, VisualizerSettings settings) {
        if (gc == null || canvas == null) return;
        clearCanvas(settings.getBackgroundColors());
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        int numBars = Math.min(settings.getBarsCount(), magnitudes.length);
        double gap = settings.getBarsGap();
        double barWidth = (w - (numBars - 1) * gap) / numBars;
        List<ColorLevel> colors = settings.getBarColors();
        for (int i = 0; i < numBars; i++) {
            double magnitude = magnitudes[i];
            double displayHeight = (magnitude + 60) * (h / 60) * settings.getSensitivity();
            if (displayHeight < 2) displayHeight = 2;
            double normalizedHeight = Math.min(1.0, displayHeight / h);
            int colorIndex = (int) (normalizedHeight * (colors.size() - 1));
            String hex = !colors.isEmpty() ? colors.get(colorIndex).getHexCode() : "#00FF00";
            gc.setFill(Color.web(hex));
            double x = i * (barWidth + gap);
            double y = settings.isVerticalDirection() ? (h - displayHeight) : 0;
            gc.fillRect(x, y, barWidth, displayHeight);
        }
    }
}