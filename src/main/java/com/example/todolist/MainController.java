package com.example.todolist;

import config.UserDAO;
import javafx.scene.layout.VBox;

public class MainController {

    //DAOs
    UserDAO userDAO;

    public VBox todoVBox;

    //llamar cuando se haga el cambio de escena para recibir el dao
    public void setUserDAO(UserDAO dao) {
        userDAO = dao;
    }
}

