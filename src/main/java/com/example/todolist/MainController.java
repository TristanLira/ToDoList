package com.example.todolist;

import com.example.todolist.models.User;
import config.UserDAO;
import javafx.scene.layout.VBox;

public class MainController {

    //DAOs
    UserDAO userDAO;

    //usuario con sesión iniciada
    User logged;

    public VBox todoVBox;

    public void initialize() {
        userDAO = null;
        logged = null;
    }

    //llamar cuando se haga el cambio de escena para recibir el dao
    public void setUser(User u, UserDAO dao) {
        if (userDAO != null || logged != null) {
            System.out.println("Usuario o DAO ya asignados");
            return;
        }
        this.userDAO = dao;
        this.logged = u;
    }
}

