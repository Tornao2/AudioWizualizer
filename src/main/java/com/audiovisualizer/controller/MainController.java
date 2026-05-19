package com.audiovisualizer.controller;

import com.audiovisualizer.model.*;
import com.audiovisualizer.view.VisualizerRenderer;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainController implements AudioSpectrumListener {
    private MusicPlayerModel model;
    private VisualizerSettings settings;
    private VisualizerRenderer renderer;
    private MenuState currentMenuState = MenuState.NONE;
    private String playSvgPath = "";
    private String pauseSvgPath = "";
    @FXML private Separator verticalSeparator;
    @FXML private AnchorPane sideMenuContainer;
    private boolean isFullScreenMode = false;
    @FXML private AnchorPane visualizerContainer;
    @FXML private Canvas visualizerCanvas;
    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label songTitleLabel;
    @FXML private Label durationLabel;
    @FXML private VBox playlistMenuPane;
    @FXML private VBox settingsMenuPane;
    @FXML private ListView<Track> playlistView;
    @FXML private Label playlistCounterLabel;
    @FXML private Slider barsCountSlider;
    @FXML private Slider sensitivitySlider;
    @FXML private Slider gapSlider;
    @FXML private ToggleButton mirrorToggle;
    @FXML private ToggleButton verticalToggle;
    @FXML private Label volumeLabel;
    @FXML private SVGPath skipPrevGraphic;
    @FXML private SVGPath playPauseGraphic;
    @FXML private SVGPath stopGraphic;
    @FXML private SVGPath skipNextGraphic;
    @FXML private SVGPath soundHighGraphic;
    @FXML private SVGPath maximizeGraphic;
    @FXML private SVGPath  menuToggleGraphic;
    @FXML private Button menuToggleButton;
    @FXML private Button skipPrevButton;
    @FXML private Button playPauseButton;
    @FXML private Button stopButton;
    @FXML private Button skipNextButton;
    @FXML private Button fullScreenButton;
    @FXML private HBox topBar;
    @FXML private VBox bottomBar;
    @FXML private Region backgroundLayer;
    private boolean isPlaying = false;
    private void updateControlsState(boolean enabled) {
        playPauseButton.setDisable(!enabled);
        stopButton.setDisable(!enabled);
        skipPrevButton.setDisable(!enabled);
        skipNextButton.setDisable(!enabled);
        timeSlider.setDisable(!enabled);
    }
    private void updateBackgroundGradient() {
        List<ColorLevel> colors = settings.getBackgroundColors();
        if (colors == null || colors.isEmpty()) return;
        List<Stop> stops = new ArrayList<>();
        int n = colors.size();
        for (int i = 0; i < n; i++) {
            double offset = (double) i / (n - 1);
            stops.add(new Stop(offset * 0.5, Color.web(colors.get(i).getHexCode())));
        }
        for (int i = n - 2; i >= 0; i--) {
            double offset = (double) (n - 1 - i) / (n - 1);
            stops.add(new Stop(0.5 + (offset * 0.5), Color.web(colors.get(i).getHexCode())));
        }
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops
        );
        backgroundLayer.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
    }
    private String loadSvgPathFromResource(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) return "";
            String svgContent = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining(" "));
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("d=\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(svgContent);
            StringBuilder combinedPath = new StringBuilder();
            while (matcher.find()) {
                combinedPath.append(matcher.group(1)).append(" ");
            }
            return combinedPath.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @FXML
    public void initialize() {
        Tooltip.install(menuToggleButton, new Tooltip("Otwórz menu"));
        Tooltip.install(skipPrevButton, new Tooltip("Cofnij o 5s"));
        Tooltip.install(playPauseButton, new Tooltip("Odtwórz / Pauza"));
        Tooltip.install(stopButton, new Tooltip("Zatrzymaj"));
        Tooltip.install(skipNextButton, new Tooltip("Przewiń o 5s"));
        Tooltip.install(fullScreenButton, new Tooltip("Pełny ekran"));
        menuToggleGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/menu.svg"));
        skipPrevGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/skip-prev.svg"));
        stopGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/square.svg"));
        skipNextGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/skip-next.svg"));
        soundHighGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/sound-high.svg"));
        maximizeGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/maximize.svg"));
        playSvgPath = loadSvgPathFromResource("/com/audiovisualizer/play.svg");
        pauseSvgPath = loadSvgPathFromResource("/com/audiovisualizer/pause.svg");
        playPauseGraphic.setContent(playSvgPath);
        model = new MusicPlayerModel();
        settings = new VisualizerSettings();
        renderer = new VisualizerRenderer();
        renderer.setCanvas(visualizerCanvas);
        updateBackgroundGradient();
        String menuColor = settings.getBackgroundColors().getFirst().getHexCode();
        playlistMenuPane.setStyle("-fx-background-color: " + menuColor + ";");
        settingsMenuPane.setStyle("-fx-background-color: " + menuColor + ";");
        playlistMenuPane.setVisible(false);
        settingsMenuPane.setVisible(false);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            model.setVolume(newVal.doubleValue());
            int percentage = (int) Math.round(newVal.doubleValue() * 100);
            volumeLabel.setText(percentage + "%");
        });
        volumeLabel.setText((int) (volumeSlider.getValue() * 100) + "%");
        barsCountSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            settings.setBarsCount(newVal.intValue());
        });
        sensitivitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            settings.setSensitivity(newVal.doubleValue());
        });
        gapSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            settings.setBarsGap(newVal.intValue());
        });
        mirrorToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            settings.setMirrorMode(newVal);
        });
        verticalToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            settings.setVerticalDirection(newVal);
        });
        visualizerContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            visualizerCanvas.setWidth(newVal.doubleValue());
        });
        visualizerContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            visualizerCanvas.setHeight(newVal.doubleValue());
        });
        updateControlsState(false);
    }
    public void onTrackSelected() {
        updateControlsState(true);
    }
    @FXML
    public void handlePlayPause() {
        if (isPlaying) {
            model.pause();
            playPauseGraphic.setContent(playSvgPath);
            isPlaying = false;
        } else {
            model.play();
            playPauseGraphic.setContent(pauseSvgPath);
            isPlaying = true;
        }
    }

    @FXML
    public void handleStop() {
        model.stopAndReset();
        timeSlider.setValue(0);
        playPauseGraphic.setContent(playSvgPath);
        isPlaying = false;
    }

    @FXML
    public void handleSkipForward() {
        model.skipTime(5.0);
    }

    @FXML
    public void handleSkipBackward() {
        model.skipTime(-5.0);
    }

    @FXML
    public void handleToggleFullScreen() {
        isFullScreenMode = !isFullScreenMode;
        sideMenuContainer.setVisible(false);
        sideMenuContainer.setManaged(false);
        verticalSeparator.setVisible(false);
        verticalSeparator.setManaged(false);
        playlistMenuPane.setVisible(false);
        settingsMenuPane.setVisible(false);
        currentMenuState = MenuState.NONE;
    }

    @FXML
    private void handleOpenMenu() {
        if (sideMenuContainer.isVisible() && currentMenuState == MenuState.SETTINGS_MENU) {
            settingsMenuPane.setVisible(false);
            playlistMenuPane.setVisible(true);
            currentMenuState = MenuState.PLAYLIST_MENU;
        } else {
            boolean toVisible = !sideMenuContainer.isVisible();
            sideMenuContainer.setVisible(toVisible);
            sideMenuContainer.setManaged(toVisible);
            if (toVisible) {
                playlistMenuPane.setVisible(true);
                settingsMenuPane.setVisible(false);
                currentMenuState = MenuState.PLAYLIST_MENU;
            } else {
                playlistMenuPane.setVisible(false);
                settingsMenuPane.setVisible(false);
                currentMenuState = MenuState.NONE;
            }
        }
    }

    @FXML
    public void handleOpenSettings() {
        playlistMenuPane.setVisible(false);
        settingsMenuPane.setVisible(true);
        currentMenuState = MenuState.SETTINGS_MENU;
    }

    @FXML
    public void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
        event.consume();
    }

    @FXML
    public void handleDragAndDrop(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            List<File> files = event.getDragboard().getFiles();
        }
        event.consume();
    }
    @FXML
    public void handleApplyBackgroundPreset(int presetId) {
        settings.applyBackgroundPreset(presetId);
        updateBackgroundGradient();
    }
    @Override
    public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
        javafx.application.Platform.runLater(() -> {
            timeSlider.setValue(timestamp);
            renderer.drawSpectrum(magnitudes, settings);
        });
    }
}