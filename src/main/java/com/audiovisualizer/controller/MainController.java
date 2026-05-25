package com.audiovisualizer.controller;

import com.audiovisualizer.AdvancedColorPicker;
import com.audiovisualizer.model.*;
import com.audiovisualizer.view.PlaylistItemFactory;
import com.audiovisualizer.view.VisualizerRenderer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController implements AudioSpectrumListener {
    private MusicPlayerModel model;
    private VisualizerSettings settings;
    private VisualizerRenderer renderer;
    private MenuState currentMenuState = MenuState.NONE;
    private boolean isFullScreenMode = false;
    private boolean isPlaying = false;
    private boolean isStopped = true;
    private boolean isUserDraggingSlider = false;
    private boolean wasPlayingBeforeDrag = false;
    private boolean wasSideMenuVisible = false;
    private double lastKnownPosition = 0;
    private String playSvgPath = "", pauseSvgPath = "", trashSvgPath = "";
    @FXML private AnchorPane visualizerContainer;
    @FXML private Canvas visualizerCanvas;
    @FXML private Region backgroundLayer;
    @FXML private VBox mainControlPanel;
    @FXML private VBox topBar;
    @FXML private AnchorPane sideMenuContainer;
    @FXML private VBox playlistMenuPane;
    @FXML private VBox playlistContainer;
    @FXML private Label playlistCounterLabel;
    @FXML private VBox settingsMenuPane;
    @FXML private VBox colorRowsContainer;
    @FXML private VBox bgColorRowsContainer;
    @FXML private ToggleButton gradientDirectionToggle, presetBtn0, presetBtn2, presetBtn1;
    @FXML private Slider timeSlider, volumeSlider;
    @FXML private Label volumeLabel, currentTrackLabel, timeLabel, timePreviewLabel;
    @FXML private Button playPauseButton, stopButton, skipPrevButton, skipNextButton, fullScreenButton;
    @FXML private SVGPath playPauseGraphic, stopGraphic, skipPrevGraphic, skipNextGraphic, soundHighGraphic, maximizeGraphic;
    @FXML private Button menuToggleButton, settingsToggleButton, menuToggleButton2, settingsCloseButton;
    @FXML private SVGPath menuToggleGraphic, playlistSettings, menuToggleGraphic2, settingsCloseGraphic;
    @FXML private Slider barsCountSliderNew, sensitivitySliderNew, gapSliderNew, colorsCountSlider;
    @FXML private Label barsCountValueLabel, sensitivityValueLabel, gapValueLabel, colorsCountValueLabel;
    @FXML private ToggleButton mirrorToggleNew;
    private final List<TextField> colorTextFields = new ArrayList<>();
    private final List<TextField> bgColorTextFields = new ArrayList<>();
    private ToggleGroup presetToggleGroup;
    @FXML
    public void initialize() {
        loadResources();
        initTooltips();
        initModels();
        bindCanvasSize();
        initMenuBackground();
        initSettingsControls();
        initPlaybackControls();
        initKeyboardShortcuts();
        initFirstRender();
    }

    private void loadResources() {
        Platform.runLater(() -> {
            if (visualizerContainer.getScene() == null) return;
            String cssUrl = getClass().getResource("/style.css").toExternalForm();
            if (cssUrl != null && !visualizerContainer.getScene().getStylesheets().contains(cssUrl)) {
                visualizerContainer.getScene().getStylesheets().add(cssUrl);
            }
        });
        trashSvgPath = loadSvgPathFromResource("/com/audiovisualizer/trash.svg");
        playSvgPath = loadSvgPathFromResource("/com/audiovisualizer/play.svg");
        pauseSvgPath = loadSvgPathFromResource("/com/audiovisualizer/pause.svg");
        menuToggleGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/menu.svg"));
        skipPrevGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/skip-prev.svg"));
        stopGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/square.svg"));
        skipNextGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/skip-next.svg"));
        soundHighGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/sound-high.svg"));
        maximizeGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/maximize.svg"));
        playlistSettings.setContent(loadSvgPathFromResource("/com/audiovisualizer/settings.svg"));
        menuToggleGraphic2.setContent(loadSvgPathFromResource("/com/audiovisualizer/xmark.svg"));
        settingsCloseGraphic.setContent(loadSvgPathFromResource("/com/audiovisualizer/xmark.svg"));
        playPauseGraphic.setContent(playSvgPath);
    }

    private void initTooltips() {
        Tooltip.install(menuToggleButton, new Tooltip("Otwórz menu"));
        Tooltip.install(skipPrevButton, new Tooltip("Cofnij o 5s (←)"));
        Tooltip.install(playPauseButton, new Tooltip("Odtwórz / Pauza (Spacja)"));
        Tooltip.install(stopButton, new Tooltip("Zatrzymaj (S)"));
        Tooltip.install(skipNextButton, new Tooltip("Przewiń o 5s (→)"));
        Tooltip.install(fullScreenButton, new Tooltip("Pełny ekran (Esc/F11)"));
        Tooltip.install(settingsToggleButton, new Tooltip("Otwórz ustawienia"));
        Tooltip.install(menuToggleButton2, new Tooltip("Zamknij menu"));
        Tooltip.install(settingsCloseButton, new Tooltip("Zamknij ustawienia"));
    }

    private void initModels() {
        model = new MusicPlayerModel();
        settings = VisualizerSettings.load();
        renderer = new VisualizerRenderer();
        renderer.setCanvas(visualizerCanvas);
    }

    private void bindCanvasSize() {
        visualizerCanvas.widthProperty().bind(visualizerContainer.widthProperty());
        visualizerCanvas.heightProperty().bind(visualizerContainer.heightProperty());
        Runnable redrawOnResize = () -> {
            if (settings != null && renderer != null) renderer.redrawLast(settings);
        };
        visualizerContainer.widthProperty().addListener((o, oldV, newV) -> Platform.runLater(redrawOnResize));
        visualizerContainer.heightProperty().addListener((o, oldV, newV) -> Platform.runLater(redrawOnResize));
    }

    private void initMenuBackground() {
        Platform.runLater(() -> {
            applyDefaultMenuBackground();
            updateBackgroundGradient();
            updatePresetButtons();
            updateBgColorRows();
        });
    }

    private void initSettingsControls() {
        mirrorToggleNew.setSelected(settings.isMirrorMode());
        mirrorToggleNew.selectedProperty().addListener((o, oldV, newVal) -> {
            settings.setMirrorMode(newVal);
            forceVisualizerRedraw();
            settings.save();
        });
        sensitivitySliderNew.setValue(settings.getSensitivity());
        sensitivityValueLabel.setText(String.format("%.1f", settings.getSensitivity()));
        sensitivitySliderNew.valueProperty().addListener((o, oldV, newVal) -> {
            double v = newVal.doubleValue();
            settings.setSensitivity(v);
            sensitivityValueLabel.setText(String.format("%.1f", v));
            forceVisualizerRedraw();
            settings.save();
        });
        gapSliderNew.setValue(settings.getBarsGap());
        gapValueLabel.setText(String.valueOf(settings.getBarsGap()));
        gapSliderNew.valueProperty().addListener((o, oldV, newVal) -> {
            int v = newVal.intValue();
            settings.setBarsGap(v);
            gapValueLabel.setText(String.valueOf(v));
            forceVisualizerRedraw();
            settings.save();
        });
        barsCountSliderNew.setValue(settings.getBarsCount());
        barsCountValueLabel.setText(String.valueOf(settings.getBarsCount()));
        barsCountSliderNew.valueProperty().addListener((o, oldV, newVal) -> {
            int v = newVal.intValue();
            barsCountValueLabel.setText(String.valueOf(v));
            settings.setBarsCount(v);
            if (model.getMediaPlayer() == null || model.getMediaPlayer().getStatus() != MediaPlayer.Status.PLAYING) {
                float[] silence = new float[settings.getBarsCount()];
                Arrays.fill(silence, -60.0f);
                renderer.drawSpectrum(silence, settings);
            }
            settings.save();
        });
        colorsCountSlider.setValue(settings.getColorsCount());
        colorsCountValueLabel.setText(String.valueOf(settings.getColorsCount()));
        colorsCountSlider.valueProperty().addListener((o, oldV, newVal) -> {
            int v = newVal.intValue();
            settings.setColorsCount(v);
            colorsCountValueLabel.setText(String.valueOf(v));
            updateColorRows();
            forceVisualizerRedraw();
            settings.save();
        });
        updateColorRows();
        gradientDirectionToggle.setSelected(settings.isVerticalDirection());
        gradientDirectionToggle.selectedProperty().addListener((o, oldV, newVal) -> {
            settings.setVerticalDirection(newVal);
            updateBackgroundGradient();
            settings.save();
        });
    }

    private void initPlaybackControls() {
        volumeSlider.valueProperty().addListener((o, oldV, newVal) -> {
            model.setVolume(newVal.doubleValue());
            volumeLabel.setText((int) Math.round(newVal.doubleValue() * 100) + "%");
        });
        volumeLabel.setText((int) (volumeSlider.getValue() * 100) + "%");
        timeSlider.valueProperty().addListener((o, oldV, newVal) -> {
            if (isUserDraggingSlider) {
                updatePreviewTime();
                forceVisualizerRedraw();
            }
        });
        timeSlider.setOnMousePressed(e -> {
            isUserDraggingSlider = true;
            if (model.getMediaPlayer() != null && model.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                wasPlayingBeforeDrag = true;
                model.getMediaPlayer().pause();
            } else {
                wasPlayingBeforeDrag = false;
            }
            updatePreviewTime();
            if (timePreviewLabel != null) {
                timePreviewLabel.setVisible(true);
                timePreviewLabel.setManaged(true);
            }
        });
        timeSlider.setOnMouseReleased(e -> {
            if (model.getMediaPlayer() != null) {
                double target = timeSlider.getValue();
                model.getMediaPlayer().seek(Duration.seconds(target));
                lastKnownPosition = target;
                Platform.runLater(() -> {
                    updateTimeUI(target);
                    if (timePreviewLabel != null) {
                        timePreviewLabel.setVisible(false);
                        timePreviewLabel.setManaged(false);
                    }
                });
                if (wasPlayingBeforeDrag) {
                    Platform.runLater(() -> {
                        model.getMediaPlayer().play();
                        if (model.getMediaPlayer().getVolume() < 0.01) {
                            model.getMediaPlayer().setVolume(model.getMediaPlayer().getVolume());
                        }
                    });
                }
                if (timePreviewLabel != null) {
                    timePreviewLabel.setVisible(false);
                    timePreviewLabel.setManaged(false);
                }
            }
            isUserDraggingSlider = false;
            wasPlayingBeforeDrag = false;
        });
        updateControlsState(false);
        updatePlaylistCounter();
    }

    private void initKeyboardShortcuts() {
        Platform.runLater(() -> {
            Stage stage = getStage();
            if (stage == null) return;
            stage.fullScreenProperty().addListener((o, oldV, newVal) -> {
                isFullScreenMode = newVal;
                if (isFullScreenMode) hideUI(); else showUI();
            });
            visualizerContainer.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getTarget() instanceof TextInputControl) return;
                switch (e.getCode()) {
                    case F11 -> { stage.setFullScreen(!stage.isFullScreen()); e.consume(); }
                    case ESCAPE -> { if (isFullScreenMode) stage.setFullScreen(false); e.consume(); }
                    case SPACE -> { if (model.getMediaPlayer() != null) { handlePlayPause(); e.consume(); } }
                    case LEFT -> { if (model.getMediaPlayer() != null) { handleSkipBackward(); e.consume(); } }
                    case RIGHT -> { if (model.getMediaPlayer() != null) { handleSkipForward(); e.consume(); } }
                    case S -> { if (model.getMediaPlayer() != null) { handleStop(); e.consume(); } }
                    case UP -> { adjustVolume(0.05); e.consume(); }
                    case DOWN -> { adjustVolume(-0.05); e.consume(); }
                }
            });
        });
    }

    private void initFirstRender() {
        Platform.runLater(() -> {
            if (settings != null && renderer != null) {
                float[] silence = new float[settings.getBarsCount()];
                Arrays.fill(silence, -60.0f);
                renderer.drawSpectrum(silence, settings);
            }
        });
    }

    private void adjustVolume(double delta) {
        double newVol = Math.max(0, Math.min(1, volumeSlider.getValue() + delta));
        volumeSlider.setValue(newVol);
        model.setVolume(newVol);
        volumeLabel.setText((int) Math.round(newVol * 100) + "%");
    }

    private void hideUI() {
        wasSideMenuVisible = sideMenuContainer.isVisible();
        sideMenuContainer.setVisible(false); sideMenuContainer.setManaged(false);
        mainControlPanel.setVisible(false); mainControlPanel.setManaged(false);
        topBar.setVisible(false); topBar.setManaged(false);
        currentMenuState = MenuState.NONE;
    }

    private void showUI() {
        sideMenuContainer.setVisible(wasSideMenuVisible); sideMenuContainer.setManaged(wasSideMenuVisible);
        mainControlPanel.setVisible(true); mainControlPanel.setManaged(true);
        topBar.setVisible(true); topBar.setManaged(true);
    }

    private void updateControlsState(boolean enabled) {
        playPauseButton.setDisable(!enabled);
        stopButton.setDisable(!enabled);
        skipPrevButton.setDisable(!enabled);
        skipNextButton.setDisable(!enabled);
        timeSlider.setDisable(!enabled);
    }

    private void applyDefaultMenuBackground() {
        String gradientCss = "linear-gradient(to bottom, #201d40, #080819)";
        updateNodeBackgroundCss(sideMenuContainer, gradientCss);
        updateNodeBackgroundCss(playlistMenuPane, gradientCss);
        updateNodeBackgroundCss(settingsMenuPane, gradientCss);
        updateNodeBackgroundCss(topBar, gradientCss);
        updateNodeBackgroundCss(mainControlPanel, gradientCss);
    }

    private void updateNodeBackgroundCss(Region node, String gradientCss) {
        if (node != null) {
            node.setStyle("-fx-background-color: " + gradientCss + "; -fx-background-insets: 0;");
            node.applyCss();
            node.layout();
        }
    }

    private void updateBackgroundGradient() {
        List<ColorLevel> colors = settings.getBackgroundColors();
        if (colors == null || colors.size() < 2) return;
        String color1 = colors.get(0).getHexCode();
        String color2 = colors.get(1).getHexCode();
        boolean isVertical = settings.isVerticalDirection();
        String direction = isVertical ? "to bottom" : "to right";
        String gradientCss = "linear-gradient(" + direction + ", " + color1 + ", " + color2 + ")";
        backgroundLayer.setStyle("");
        backgroundLayer.applyCss();
        backgroundLayer.setStyle("-fx-background-color: " + gradientCss + "; -fx-background-insets: 0;");
        backgroundLayer.applyCss();
        backgroundLayer.layout();
        if (renderer != null && settings != null) renderer.redrawLast(settings);
    }

    private void updatePresetButtons() {
        if (presetToggleGroup == null) presetToggleGroup = new ToggleGroup();
        ToggleButton[] buttons = {presetBtn0, presetBtn1, presetBtn2};
        for (int i = 0; i < settings.getPresets().size(); i++) {
            VisualizerSettings.BackgroundPreset p = settings.getPresets().get(i);
            ToggleButton btn = buttons[i];
            btn.setText("");
            btn.setToggleGroup(presetToggleGroup);
            btn.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
                            "-fx-background-radius: 4; -fx-border-radius: 4;",
                    p.getColorTop(), p.getColorBottom()));
            btn.setSelected(i == settings.getActivePresetIndex());
            final int idx = i;
            btn.setOnAction(e -> {
                settings.applyBackgroundPreset(idx);
                updateBgColorRows();
                updateBackgroundGradient();
                updatePresetButtons();
                settings.save();
            });
        }
    }

    private void updateBgColorRows() {
        bgColorRowsContainer.getChildren().clear();
        bgColorTextFields.clear();
        List<ColorLevel> bgColors = settings.getBackgroundColors();
        String[] labels = {"Kolor A", "Kolor B"};
        for (int i = 0; i < 2; i++) {
            ColorLevel level = (i < bgColors.size()) ? bgColors.get(i) : new ColorLevel("Tło " + (i+1), "#201d40");
            final int index = i;
            Label label = new Label(labels[i]);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            label.setMinWidth(85);
            Region preview = new Region();
            preview.getStyleClass().add("color-preview");
            preview.setStyle("-fx-background-color: " + level.getHexCode());
            preview.setMinHeight(28);
            preview.setMinWidth(20);
            preview.setOnMouseClicked(e -> {
                AdvancedColorPicker picker = new AdvancedColorPicker();
                Optional<String> result = picker.show(visualizerContainer.getScene().getWindow(), level.getHexCode());
                result.ifPresent(hex -> {
                    level.setHexCode(hex);
                    preview.setStyle("-fx-background-color: " + hex);
                    if (index < bgColorTextFields.size()) bgColorTextFields.get(index).setText(hex);
                    updateBackgroundGradient();
                    settings.syncCurrentColorsToActivePreset();
                    updatePresetButtons();
                    settings.save();
                });
            });
            TextField tf = new TextField(level.getHexCode());
            tf.setPromptText("#RRGGBB");
            tf.setPrefWidth(95);
            tf.getStyleClass().add("color-text-field");
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.matches("#[0-9A-Fa-f]{6}")) {
                    level.setHexCode(newVal);
                    preview.setStyle("-fx-background-color: " + newVal);
                    updateBackgroundGradient();
                    settings.syncCurrentColorsToActivePreset();
                    updatePresetButtons();
                    settings.save();
                }
            });
            HBox row = new HBox(8, label, preview, tf);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, 0, 2, 0));
            row.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(preview, Priority.ALWAYS);
            bgColorRowsContainer.getChildren().add(row);
            bgColorTextFields.add(tf);
        }
    }

    private void updateColorRows() {
        colorRowsContainer.getChildren().clear();
        colorTextFields.clear();
        int count = (int) colorsCountSlider.getValue();
        List<ColorLevel> colors = settings.getBarColors();
        for (int i = 0; i < count; i++) {
            ColorLevel level = colors.get(i);
            final int index = i;
            Label label = new Label("Poziom " + (i + 1));
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            label.setMinWidth(85);
            Region preview = new Region();
            preview.getStyleClass().add("color-preview");
            preview.setStyle("-fx-background-color: " + level.getHexCode());
            preview.setMinHeight(28);
            preview.setMinWidth(20);
            preview.setOnMouseClicked(e -> {
                AdvancedColorPicker picker = new AdvancedColorPicker();
                Optional<String> result = picker.show(visualizerContainer.getScene().getWindow(), level.getHexCode());
                result.ifPresent(hex -> {
                    level.setHexCode(hex);
                    preview.setStyle("-fx-background-color: " + hex);
                    if (index < colorTextFields.size()) colorTextFields.get(index).setText(hex);
                    forceVisualizerRedraw();
                    settings.save();
                });
            });
            TextField tf = new TextField(level.getHexCode());
            tf.setPromptText("#RRGGBB");
            tf.setPrefWidth(95);
            tf.getStyleClass().add("color-text-field");
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.matches("#[0-9A-Fa-f]{6}")) {
                    level.setHexCode(newVal);
                    preview.setStyle("-fx-background-color: " + newVal);
                    forceVisualizerRedraw();
                    settings.save();
                }
            });
            HBox row = new HBox(8, label, preview, tf);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, 0, 2, 0));
            row.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(preview, Priority.ALWAYS);
            colorRowsContainer.getChildren().add(row);
            colorTextFields.add(tf);
        }
    }

    private void updatePlaylistCounter() {
        playlistCounterLabel.setText(model.getPlaylist().getCurrentIndexInfo());
    }

    private void activateTrack(VBox component, Track track, int index) {
        cleanupMediaPlayer();
        resetVisualsAndUI();
        model.getPlaylist().setCurrentIndex(index);
        model.loadTrack(track);
        lastKnownPosition = 0;
        playlistContainer.getChildren().forEach(n -> n.setStyle("-fx-background-color: transparent;"));
        component.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-background-radius: 5;");
        currentTrackLabel.setText(track.getTitle());
        if (model.getMediaPlayer() != null) {
            model.getMediaPlayer().setAudioSpectrumListener(this);
            model.getMediaPlayer().setOnReady(() -> {
                double total = model.getMediaPlayer().getTotalDuration().toSeconds();
                timeSlider.setMax(total);
                String formatted = formatTime(total);
                track.setDuration(formatted);
                timeLabel.setText("00:00 / " + formatted);
                updateLabelInHierarchy(component, formatted);
                updateControlsState(true);
            });
        }
        isPlaying = true;
        isStopped = false;
        handlePlayPause();
        updatePlaylistCounter();
    }

    private void cleanupMediaPlayer() {
        if (model.getMediaPlayer() != null) {
            MediaPlayer old = model.getMediaPlayer();
            old.setAudioSpectrumListener(null);
            if (old.getStatus() != MediaPlayer.Status.UNKNOWN) old.stop();
            old.dispose();
        }
    }

    private void addSongFile(File file) {
        if (file == null) return;
        String absolutePath = file.getAbsolutePath();
        for (Track t : model.getPlaylist().getTracks()) {
            if (t.getFilePath().equals(absolutePath)) return;
        }
        String cleanTitle = file.getName().replaceAll("\\.(mp3|wav|m4a|mp4)$", "");
        Track track = new Track(absolutePath, cleanTitle, "00:00");
        boolean wasEmpty = model.getPlaylist().getTracks().isEmpty();
        model.getPlaylist().addTrack(track);
        VBox readyComponent = PlaylistItemFactory.createComponent(
                track.getTitle(), "00:00", trashSvgPath,
                (itemBox) -> {
                    int index = playlistContainer.getChildren().indexOf(itemBox);
                    if (index != -1) {
                        Track removedTrack = model.getPlaylist().getTracks().get(index);
                        playlistContainer.getChildren().remove(index);
                        model.getPlaylist().removeTrack(index);
                        if (model.getCurrentTrack() != null && model.getCurrentTrack().equals(removedTrack)) {
                            handleStop();
                            currentTrackLabel.setText("Brak utworu");
                            timeLabel.setText("00:00 / 00:00");
                            model.loadTrack(null);
                            updateControlsState(false);
                        } else if (model.getPlaylist().getTracks().isEmpty()) {
                            currentTrackLabel.setText("Brak utworu");
                            timeLabel.setText("00:00 / 00:00");
                            updateControlsState(false);
                        }
                        updatePlaylistCounter();
                    }
                }
        );
        javafx.scene.media.Media media = new javafx.scene.media.Media(file.toURI().toString());
        javafx.scene.media.MediaPlayer tempPlayer = new javafx.scene.media.MediaPlayer(media);
        tempPlayer.setOnReady(() -> {
            String formatted = formatTime(media.getDuration().toSeconds());
            track.setDuration(formatted);
            Platform.runLater(() -> updateLabelInHierarchy(readyComponent, formatted));
            tempPlayer.dispose();
        });
        readyComponent.setOnMouseClicked(event -> {
            int clickedIndex = playlistContainer.getChildren().indexOf(readyComponent);
            if (clickedIndex != -1) activateTrack(readyComponent, track, clickedIndex);
        });
        playlistContainer.getChildren().add(readyComponent);
        updatePlaylistCounter();
        if (wasEmpty) activateTrack(readyComponent, track, 0);
    }

    @FXML public void handleAddTracks() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Wybierz pliki audio");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki Audio", "*.mp3", "*.wav", "*.m4a"));
        List<File> files = fc.showOpenMultipleDialog(getStage());
        if (files != null) files.forEach(this::addSongFile);
    }

    @FXML public void handlePlayPause() {
        if (model.getMediaPlayer() == null) return;
        MediaPlayer.Status status = model.getMediaPlayer().getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            model.pause();
            playPauseGraphic.setContent(playSvgPath);
            isPlaying = false;
        } else {
            model.play();
            playPauseGraphic.setContent(pauseSvgPath);
            isPlaying = true;
            isStopped = false;
        }
    }

    @FXML public void handleStop() {
        if (model.getMediaPlayer() == null) return;
        isPlaying = false;
        isStopped = true;
        lastKnownPosition = 0;
        playPauseGraphic.setContent(playSvgPath);
        timeSlider.setValue(0);
        timeLabel.setText("00:00 / " + (model.getCurrentTrack() != null ? model.getCurrentTrack().getDuration() : "00:00"));
        if (settings != null && renderer != null) {
            float[] silence = new float[settings.getBarsCount()];
            Arrays.fill(silence, -60.0f);
            renderer.drawSpectrum(silence, settings);
        }
        model.stopAndReset();
    }

    @FXML public void handleSkipForward() {
        if (model.getMediaPlayer() == null) return;
        MediaPlayer mp = model.getMediaPlayer();
        Duration total = mp.getTotalDuration();
        if (total == null || total.isUnknown()) return;
        double target = Math.min(lastKnownPosition + 5.0, total.toSeconds());
        mp.seek(Duration.seconds(target));
        lastKnownPosition = target;
        Platform.runLater(() -> { updateTimeUI(target); forceVisualizerRedraw(); });
    }

    @FXML public void handleSkipBackward() {
        if (model.getMediaPlayer() == null) return;
        MediaPlayer mp = model.getMediaPlayer();
        Duration total = mp.getTotalDuration();
        if (total == null || total.isUnknown()) return;
        double target = Math.max(0, lastKnownPosition - 5.0);
        mp.seek(Duration.seconds(target));
        lastKnownPosition = target;
        Platform.runLater(() -> { updateTimeUI(target); forceVisualizerRedraw(); });
    }

    @FXML public void handleToggleFullScreen() {
        Stage stage = getStage();
        if (stage != null) stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML public void handleOpenMenu() {
        if (sideMenuContainer.isVisible() && currentMenuState == MenuState.SETTINGS_MENU) {
            settingsMenuPane.setVisible(false);
            playlistMenuPane.setVisible(true);
            currentMenuState = MenuState.PLAYLIST_MENU;
        } else {
            boolean toVisible = !sideMenuContainer.isVisible();
            sideMenuContainer.setVisible(toVisible); sideMenuContainer.setManaged(toVisible);
            if (toVisible) {
                playlistMenuPane.setVisible(true); settingsMenuPane.setVisible(false);
                currentMenuState = MenuState.PLAYLIST_MENU;
            } else {
                playlistMenuPane.setVisible(false); settingsMenuPane.setVisible(false);
                currentMenuState = MenuState.NONE;
            }
        }
    }

    @FXML public void handleOpenSettings() {
        playlistMenuPane.setVisible(false);
        settingsMenuPane.setVisible(true);
        currentMenuState = MenuState.SETTINGS_MENU;
    }

    @FXML public void handleCloseSettings() {
        settingsMenuPane.setVisible(false);
        playlistMenuPane.setVisible(true);
        currentMenuState = MenuState.PLAYLIST_MENU;
    }

    @FXML public void handleDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.ANY);
        e.consume();
    }

    @FXML public void handleDragAndDrop(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.getDragboard().getFiles().forEach(f -> {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")) addSongFile(f);
            });
        }
        e.consume();
    }

    @Override
    public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
        if (isUserDraggingSlider) return;
        if (!isPlaying) {
            if (isStopped && settings != null && renderer != null) {
                float[] silence = new float[settings.getBarsCount()];
                Arrays.fill(silence, -60.0f);
                renderer.drawSpectrum(silence, settings);
            }
            return;
        }
        Platform.runLater(() -> {
            if (model.getMediaPlayer() != null && !isUserDraggingSlider) {
                double current = model.getMediaPlayer().getCurrentTime().toSeconds();
                lastKnownPosition = current;
                timeSlider.setValue(current);
                String total = (model.getCurrentTrack() != null) ? model.getCurrentTrack().getDuration() : "00:00";
                timeLabel.setText(formatTime(current) + " / " + total);
            }
            renderer.drawSpectrum(magnitudes, settings);
        });
    }

    private Stage getStage() {
        if (visualizerContainer != null && visualizerContainer.getScene() != null) {
            return (Stage) visualizerContainer.getScene().getWindow();
        }
        return null;
    }

    private void forceVisualizerRedraw() {
        if (renderer != null && settings != null) renderer.redrawLast(settings);
    }

    private void resetVisualsAndUI() {
        Platform.runLater(() -> {
            timeLabel.setText("00:00 / 00:00");
            if (timeSlider != null) timeSlider.setValue(0);
            if (settings != null && renderer != null) {
                float[] silence = new float[settings.getBarsCount()];
                Arrays.fill(silence, -60.0f);
                renderer.drawSpectrum(silence, settings);
            }
        });
    }

    private void updateTimeUI(double seconds) {
        timeSlider.setValue(seconds);
        timeLabel.setText(formatTime(seconds) + " / " +
                (model.getCurrentTrack() != null ? model.getCurrentTrack().getDuration() : "00:00"));
    }

    private void updatePreviewTime() {
        if (model.getCurrentTrack() == null) return;
        double previewSeconds = timeSlider.getValue();
        String preview = formatTime(previewSeconds);
        String total = model.getCurrentTrack().getDuration();
        timePreviewLabel.setText(preview + " / " + total);
    }

    private void drawSilence() {
        if (settings != null && renderer != null) {
            float[] silence = new float[settings.getBarsCount()];
            Arrays.fill(silence, -60.0f);
            renderer.drawSpectrum(silence, settings);
        }
    }

    private String formatTime(double seconds) {
        int min = (int) seconds / 60;
        int sec = (int) seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void updateLabelInHierarchy(javafx.scene.Parent parent, String newDuration) {
        javafx.scene.Node labelNode = parent.lookup("#durationLabel");
        if (labelNode instanceof Label) ((Label) labelNode).setText(newDuration);
    }

    private String loadSvgPathFromResource(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) return "";
            String svgContent = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining(" "));
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("d=\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(svgContent);
            StringBuilder combinedPath = new StringBuilder();
            while (matcher.find()) combinedPath.append(matcher.group(1)).append(" ");
            return combinedPath.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}