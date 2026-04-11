package com.example.todolist.models;

import com.google.apps.card.v1.Grid;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public class CategoryRow extends GridPane {

    private final Category c;

    private final Label colorLabel;
    private final Label nameLabel;
    private final Button deleteButton;

    public CategoryRow(Category c) {
        super();
        this.c = c;
        colorLabel = new Label();
        nameLabel = new Label(c.getName());
        deleteButton = new Button("Eliminar");

        FontIcon icon = new FontIcon("fas-eraser");
        icon.setIconColor(Color.WHITE);
        deleteButton.setGraphic(icon);

        build();
    }

    private void build() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(10);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(60);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(30);
        this.getColumnConstraints().addAll(c0, c1, c2);

        colorLabel.setBackground(new Background(new BackgroundFill(c.obtainColorObj(), CornerRadii.EMPTY, Insets.EMPTY)));
        colorLabel.setPrefSize(20, 20);

        this.add(colorLabel, 0, 0);
        this.add(nameLabel, 1, 0);
        this.add(deleteButton, 2, 0);

        //centra el botón y el indicador del color

        GridPane.setHalignment(colorLabel, HPos.CENTER);
        GridPane.setHalignment(deleteButton, HPos.CENTER);

        GridPane.setValignment(colorLabel, VPos.CENTER);
        GridPane.setValignment(nameLabel, VPos.CENTER);
        GridPane.setValignment(deleteButton, VPos.CENTER);
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Category getCategory() {
        return c;
    }
}
