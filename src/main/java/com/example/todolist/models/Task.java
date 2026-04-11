package com.example.todolist.models;

import com.google.firebase.database.Exclude;

import java.time.LocalDate;

public class Task {

    private String userId;
    private String categoryId;
    private String id;
    private String name;
    private String description;
    private String creation;
    private String deadline;
    private LocalDate creationObj;
    private LocalDate deadlineObj;

    private boolean completed;

    private TaskRow uiRow;

    public Task(String userId, String categoryId, String name, String description, LocalDate deadlineObj) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.deadlineObj = deadlineObj;
        this.deadline = deadlineObj.toString();

        //asigna la fecha actual como la fecha de creación
        this.creationObj = LocalDate.now();
        this.creation = creationObj.toString();

        completed = false;

        uiRow = null;
    }

    public Task() {}

    @Exclude
    public LocalDate obtainCreationObj() {
        return creationObj;
    }

    @Exclude
    public LocalDate obtainDeadlineObj() {
        return deadlineObj;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
        this.creationObj = LocalDate.parse(creation);
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
        this.deadlineObj = LocalDate.parse(deadline);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    //Excluye estos getters de la base de datos

    @Exclude
    public TaskRow getUiRow() {
        return uiRow;
    }

    @Exclude
    public void setUiRow(TaskRow uiRow) {
        this.uiRow = uiRow;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Task)) return false;

        Task t = (Task) o;

        //no se puede comparar una task a la que firebase no le ha asignado un id
        if (t.getId().isEmpty()) return false;

        //son iguales solo si el id es igual
        return t.getId().equals(this.id);
    }

    @Override
    public String toString() {
        return "Tarea: " + name + ", creacion: " + creation + ", limite: " + deadline +
                (completed ? "(completada)" : "");
    }
}
