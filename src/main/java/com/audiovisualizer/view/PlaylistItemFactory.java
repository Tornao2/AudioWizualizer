package com.audiovisualizer.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import java.util.function.Consumer;

public class PlaylistItemFactory {
    public static VBox createComponent(String tytul, String dlugosc, String trashSvgContent, Consumer<VBox> onDeleteAction) {
        VBox itemBox = new VBox();
        itemBox.setStyle("-fx-background-color: transparent;");
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(70.0);
        row.setPadding(new Insets(0, 15, 0, 20));
        VBox textContainer = new VBox(2);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);
        Label titleLabel = new Label(tytul);
        titleLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 16px;");
        titleLabel.setWrapText(true);
        Label durationLabel = new Label(dlugosc);
        durationLabel.setId("durationLabel");
        durationLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        textContainer.getChildren().addAll(titleLabel, durationLabel);
        Button deleteBtn = new Button();
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        Tooltip.install(deleteBtn, new Tooltip("Usuń z playlisty"));
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent(trashSvgContent);
        trashIcon.setFill(Color.TRANSPARENT);
        trashIcon.setStrokeWidth(2.0);
        trashIcon.setStrokeLineCap(StrokeLineCap.ROUND);
        trashIcon.setStrokeLineJoin(StrokeLineJoin.ROUND);
        trashIcon.setStroke(Color.WHITE);
        trashIcon.setScaleX(1.2);
        trashIcon.setScaleY(1.2);
        deleteBtn.setGraphic(trashIcon);
        row.getChildren().addAll(textContainer, deleteBtn);
        Region separator = new Region();
        separator.setStyle("-fx-background-color: #3F444E; -fx-min-height: 1px; -fx-max-height: 1px; -fx-pref-height: 1px;");
        itemBox.getChildren().addAll(row, separator);
        deleteBtn.setOnAction(e -> onDeleteAction.accept(itemBox));
        return itemBox;
    }
}