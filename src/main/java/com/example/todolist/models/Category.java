package com.example.todolist.models;

import com.google.firebase.database.Exclude;
import javafx.scene.paint.Color;

public class Category {

    private String userId;
    private String id;
    private String name;
    private String color;
    private Color colorObj;

    private CategoryRow uiRow;

    public Category(String userId, String name, Color colorObj) {
        this.userId = userId;
        this.id = "";
        this.name = name;
        this.colorObj = colorObj;
        this.color = colorObj.toString();
    }

    public Category() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        setColorObj();
    }

    public Color obtainColorObj() {
        return colorObj;
    }

    private void setColorObj() {
        colorObj = Color.web(color);
    }

    //excluye el row de escribirse o leerse por la base de datos

    @Exclude
    public void setUiRow(CategoryRow uiRow) {
        this.uiRow = uiRow;
    }

    @Exclude
    public CategoryRow getUiRow() {
        return uiRow;
    }

    @Override
    public String toString() {
        return name; //para el combobox al crear tareas
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Category)) return false;

        Category c = (Category) o;

        //no se puede comparar una category a la que firebase no le ha asignado un id
        if (c.getId().isEmpty()) return false;

        //son iguales solo si el id es igual
        return c.getId().equals(this.id);
    }
}
