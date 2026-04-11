package com.example.todolist.models;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class TaskRow extends HBox {

    //models
    private Task t;
    private Category c;

    private final Label nameLabel;
    private final Label categoryLabel;
    private final Label descriptionLabel;
    private final Label timeLeftTitleLabel;
    private final Label timeLeftLabel;

    private final Button completeButton;
    private final FontIcon buttonIcon;

    private final GridPane grid;


    public TaskRow(Task t, Category c) {
        super();

        this.t = t;
        this.c = c;

        nameLabel = new Label(t.getName());
        categoryLabel = new Label(c.getName());
        descriptionLabel = new Label(t.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxHeight(Double.MAX_VALUE);
        timeLeftTitleLabel = new Label("Tiempo restante");
        timeLeftLabel = new Label(getTimeLeft());
        timeLeftLabel.setWrapText(true);
        timeLeftLabel.setMaxHeight(Double.MAX_VALUE);
        completeButton = new Button();
        buttonIcon = new FontIcon("fas-check");
        completeButton.setGraphic(buttonIcon);
        grid = new GridPane();

        build();
        addCss();

        //permite que el hbox crezca
        this.setPrefHeight(USE_COMPUTED_SIZE);
        this.minHeight(USE_PREF_SIZE);
    }

    private String getTimeLeft() {
        Period p = Period.between(t.obtainCreationObj(), t.obtainDeadlineObj());

        ArrayList<String> dateList = new ArrayList<>();

        if (p.getYears() != 0) dateList.add(p.getYears() + " años");
        if (p.getMonths() != 0) dateList.add(p.getMonths() + " meses");
        if (p.getDays() != 0) dateList.add(p.getDays() + " días");

        if (p.getYears() == 0 && p.getMonths() == 0 & p.getDays() == 0) return "Hoy";

        String period = dateList.get(0);

        for (int i = 1; i < dateList.size(); i++) {
            period += ", " + dateList.get(i);
        }

        return period;
    }

    private void build() {
        Insets in = new Insets(10,10,10,10);

        completeButton.setPadding(in);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(65);
        c0.setFillWidth(true);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(35);
        c1.setFillWidth(true);

        grid.getColumnConstraints().addAll(c0, c1);
        grid.setPadding(in);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(grid, Priority.ALWAYS);

        HBox taskNameBox = new HBox();
        taskNameBox.getChildren().addAll(nameLabel, categoryLabel);
        taskNameBox.setAlignment(Pos.CENTER_LEFT);
        taskNameBox.setSpacing(15);

        grid.add(taskNameBox, 0, 0);
        grid.add(descriptionLabel, 0, 1);
        grid.add(timeLeftTitleLabel, 1, 0);
        grid.add(timeLeftLabel, 1, 1);

        GridPane.setValignment(descriptionLabel, VPos.TOP);
        GridPane.setValignment(timeLeftLabel, VPos.TOP);

        this.setAlignment(Pos.CENTER);
        this.setMaxWidth(Double.MAX_VALUE);
        this.getChildren().addAll(grid, completeButton);
    }

    private void addCss() {
        this.getStyleClass().add("task-row");
        grid.getStyleClass().add("task-row-grid");

        nameLabel.getStyleClass().add("task-row-title");
        categoryLabel.getStyleClass().add("task-category");
        descriptionLabel.getStyleClass().add("task-description");
        timeLeftTitleLabel.getStyleClass().add("task-row-subtitle");
        timeLeftLabel.getStyleClass().add("task-time");

        completeButton.getStyleClass().add("task-complete-button");
        buttonIcon.getStyleClass().add("icon-button");

        //css dinámico para respetar el color de la categoryLabel
        categoryLabel.setStyle("-fx-background-color: " + getHexColor(c.obtainColorObj()) + ";" +
                        "-fx-background-radius: 10;");
    }

    private String getHexColor(Color color) {
        return "#" + color.toString().substring(2);
    }

    public Button getCompleteButton() {
        return completeButton;
    }

    public Task getTask() {
        return t;
    }

    public Category getCategory() {
        return c;
    }

    public LocalDate getDeadline() {
        return t.obtainDeadlineObj();
    }
}
