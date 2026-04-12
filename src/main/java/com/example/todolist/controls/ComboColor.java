package com.example.todolist.controls;

import javafx.scene.paint.Color;

public class ComboColor {

    public final Color color;
    public final String name;

    public ComboColor(Color color, String name) {
        this.color = color;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
