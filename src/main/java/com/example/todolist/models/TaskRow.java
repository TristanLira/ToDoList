package com.example.todolist.models;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
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


    public TaskRow(Task t, Category c) {
        super();

        this.t = t;
        this.c = c;

        nameLabel = new Label(t.getName());
        categoryLabel = new Label(c.getName());
        categoryLabel.setBackground(new Background(new BackgroundFill(c.obtainColorObj(), CornerRadii.EMPTY, Insets.EMPTY)));
        descriptionLabel = new Label(t.getDescription());
        descriptionLabel.setWrapText(true);
        timeLeftTitleLabel = new Label("Tiempo restante");
        timeLeftLabel = new Label(getTimeLeft());
        timeLeftLabel.setWrapText(true);

        completeButton = new Button();
        FontIcon icon = new FontIcon("fas-check-square");
        completeButton.setGraphic(icon);

        this.setAlignment(Pos.CENTER_LEFT);

        build();

        //TODO agregar style class a los componentes
    }

    private String getTimeLeft() {
        System.out.println(t.obtainCreationObj());
        System.out.println(t.obtainDeadlineObj());
        Period p = Period.between(t.obtainCreationObj(), t.obtainDeadlineObj());

        ArrayList<String> dateList = new ArrayList<>();

        System.out.println(p.getDays() + " " + p.getMonths() + " " + p.getYears());

        if (p.getYears() != 0) dateList.add(p.getYears() + " años");
        if (p.getMonths() != 0) dateList.add(p.getMonths() + " meses");
        if (p.getDays() != 0) dateList.add(p.getDays() + " días");

        String period = dateList.get(0);

        for (int i = 1; i < dateList.size(); i++) {
            period += ", " + dateList.get(i);
        }

        return period;
    }

    private void build() {
        GridPane grid = new GridPane();

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(20);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(30);

        grid.getColumnConstraints().addAll(c0, c1, c2);

        grid.add(categoryLabel, 0, 0);
        grid.add(nameLabel, 1, 0);
        grid.add(timeLeftTitleLabel, 2, 0);
        grid.add(descriptionLabel, 1, 1);
        grid.add(timeLeftLabel, 2, 1);

        this.getChildren().addAll(grid, completeButton);
    }

    public Button getCompleteButton() {
        return completeButton;
    }
}
