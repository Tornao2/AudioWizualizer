package com.audiovisualizer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Optional;

public class AdvancedColorPicker {
    private String result;

    public Optional<String> show(Window owner, String initialHex) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);
        Color initColor;
        try { initColor = Color.web(initialHex); }
        catch (IllegalArgumentException e) { initColor = Color.web("#201d40"); }
        String bg = "#080819";
        String panel = "#201d40";
        String border = "#2D3540";
        String accent = "#4DA6FF";
        String text = "#E6E6E6";
        Label title = new Label("Edytor koloru");
        title.setTextFill(Color.web(text));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Button closeBtn = new Button("✕");
        closeBtn.setTextFill(Color.web("#888"));
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 4;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setTextFill(Color.web("#FFFFFF")));
        closeBtn.setOnMouseExited(e -> closeBtn.setTextFill(Color.web("#888")));
        closeBtn.setOnAction(e -> dialog.close());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(title, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(3, 6, 3, 6));
        header.setStyle("-fx-background-color: " + panel + "; -fx-border-color: " + border + "; -fx-border-width: 0 0 1px 0;");
        final double[] dragOffset = new double[2];
        header.setOnMousePressed(e -> {
            dragOffset[0] = e.getScreenX() - dialog.getX();
            dragOffset[1] = e.getScreenY() - dialog.getY();
        });
        header.setOnMouseDragged(e -> {
            dialog.setX(e.getScreenX() - dragOffset[0]);
            dialog.setY(e.getScreenY() - dragOffset[1]);
        });
        Region preview = new Region();
        preview.setPrefSize(260, 40);
        preview.setStyle("-fx-background-radius: 4; -fx-border-color: " + border + "; -fx-border-width: 1px; -fx-border-radius: 4;");
        HBox previewBox = new HBox(preview);
        previewBox.setAlignment(Pos.CENTER);
        previewBox.setPadding(new Insets(12, 12, 8, 12));
        Slider hue = new Slider(0, 360, initColor.getHue());
        Slider sat = new Slider(0, 100, initColor.getSaturation() * 100);
        Slider bright = new Slider(0, 100, initColor.getBrightness() * 100);
        String sliderCss = "-fx-background-color: transparent; -fx-pref-height: 18px; -fx-track: " + bg + "; -fx-thumb: " + accent + "; -fx-min-track-length: 200px;";
        hue.setStyle(sliderCss);
        sat.setStyle(sliderCss);
        bright.setStyle(sliderCss);
        GridPane sliders = new GridPane();
        sliders.setHgap(10);
        sliders.setVgap(8);
        sliders.setPadding(new Insets(4, 12, 4, 12));
        sliders.add(createLabel("Odcień", text), 0, 0);
        sliders.add(hue, 1, 0);
        sliders.add(createLabel("Nasycenie", text), 0, 1);
        sliders.add(sat, 1, 1);
        sliders.add(createLabel("Jasność", text), 0, 2);
        sliders.add(bright, 1, 2);
        Button confirmBtn = new Button("Zatwierdź");
        confirmBtn.setStyle(btnStyle(accent, text));
        confirmBtn.setPrefWidth(140);
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle(btnStyle("#3A80CC", text)));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle(btnStyle(accent, text)));
        HBox footer = new HBox(confirmBtn);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(8, 12, 12, 12));
        VBox root = new VBox(0, header, previewBox, sliders, footer);
        root.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-width: 1px; -fx-border-radius: 6;");
        Scene scene = new Scene(root, 280, 220);
        dialog.setScene(scene);
        String[] currentHex = {initialHex.toUpperCase()};
        Runnable syncColor = () -> {
            Color c = Color.hsb(hue.getValue(), sat.getValue() / 100.0, bright.getValue() / 100.0);
            currentHex[0] = String.format("#%02X%02X%02X",
                    (int) Math.round(c.getRed() * 255),
                    (int) Math.round(c.getGreen() * 255),
                    (int) Math.round(c.getBlue() * 255));
            preview.setStyle("-fx-background-color: " + currentHex[0] + "; -fx-background-radius: 4; -fx-border-color: " + border + "; -fx-border-width: 1px; -fx-border-radius: 4;");
        };
        hue.valueProperty().addListener((o, v1, v2) -> syncColor.run());
        sat.valueProperty().addListener((o, v1, v2) -> syncColor.run());
        bright.valueProperty().addListener((o, v1, v2) -> syncColor.run());
        confirmBtn.setOnAction(e -> { result = currentHex[0]; dialog.close(); });
        syncColor.run();
        dialog.showAndWait();
        return Optional.ofNullable(result);
    }

    private Label createLabel(String text, String color) {
        Label l = new Label(text);
        l.setTextFill(Color.web(color));
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 0 4px 0 0;");
        l.setPrefWidth(70);
        l.setAlignment(Pos.CENTER_RIGHT);
        return l;
    }

    private String btnStyle(String bg, String text) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + text + "; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 6 16; -fx-font-size: 14px;";
    }
}