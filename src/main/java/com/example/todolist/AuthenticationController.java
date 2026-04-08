package com.example.todolist;

import com.example.todolist.models.User;
import config.UserDAO;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class AuthenticationController {

    public Label titleLabel;
    public GridPane userDataGrid;
    public TextField userField;
    public PasswordField passwordField;
    public Button loginButton;
    public Button registerButton;

    //DAO de usuario
    UserDAO userDAO;

    @FXML
    public void initialize() {
        userDAO = new UserDAO(); //crea el dao
    }

    private void cleanFields() {
        userField.clear();
        passwordField.clear();
    }

    public void login(ActionEvent actionEvent) {
        ObservableList<User> users = userDAO.getUsers();
        User u = new User(userField.getText(), passwordField.getText());
        System.out.println(u + "\n");
        cleanFields();

        System.out.println("usuarios:");
        for (User i: users) System.out.println(i);
        System.out.println();

        //VALIDACIÓN DE USUARIO Y CONTRASEÑA
        if (!users.contains(u)) {
            nonExistentUserAlert();
            return;
        }

        System.out.println("Sesión iniciada");
    }

    private void nonExistentUserAlert() {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error de inicio de sesión!");
        a.setContentText("El usuario o la contraseña son incorrectos.");
        a.show();
    }

    public void createUser(ActionEvent actionEvent) {
    }
}
