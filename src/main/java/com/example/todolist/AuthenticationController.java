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

        //PRUEBA PARA VALIDACIÓN DE USUARIOS
        /*User u1 = new User("TristanLira", "12345678");  //VALIDO
        User u2 = new User("TristanLira", "holahola");  //INVALIDO
        User u3 = new User("usuario3", "password");     //VALIDO
        User u4 = new User("usuario_4", "password");    //VALIDO
        User u5 = new User("usuario 5", "password");    //INVALIDO
        User u6 = new User("u6", "password");           //INVALIDO

        userDAO.create(u1);
        userDAO.create(u2);
        userDAO.create(u3);
        userDAO.create(u4);
        userDAO.create(u5);
        userDAO.create(u6);*/
    }

    private void cleanFields() {
        userField.clear();
        passwordField.clear();
    }

    public void login(ActionEvent actionEvent) {
        ObservableList<User> users = userDAO.getUsers();
        User u = getUserFromFields();

        System.out.println(u + "\n"); //usuario recuperado

        //VALIDACIÓN DE USUARIO Y CONTRASEÑA
        if (!users.contains(u)) {
            nonExistentUserAlert();
            return;
        }

        //después de la validación se entra a la cuenta del usuario
        System.out.println("Sesion iniciada");
    }

    public void createUser(ActionEvent actionEvent) {
        /*llama al evento para crear el usuario y termina. Ya que create recibe la instancia del controlador,
        * una vez termina el evento el DAO indica el resultado usando los métodos de alerta.*/
        User u = getUserFromFields();
        System.out.println(u + "\n");
        userDAO.create(u, this);
    }

    private User getUserFromFields() {
        User u = new User(userField.getText(), passwordField.getText());
        cleanFields();
        return u;
    }

    /************************* ALERTAS PARA LA SECCIÓN DE INICIO DE SESIÓN *************************/

    //para el inicio de sesión

    private void nonExistentUserAlert() {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error de inicio de sesión!");
        a.setContentText("El usuario o la contraseña son incorrectos.");
        a.show();
    }

    //para el registro de usuario

    public void invalidUserAlert() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Usuario inválido");
        a.setContentText("El usuario ingresado no es válido. Asegurate que contenga como mínimo 6 carácteres y no incluya espacios ni caractéres especiales.");
        a.show();
    }

    public void invalidPasswordAlert() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Contraseña inválida");
        a.setContentText("La contraseña ingresada no es válida. Asegurate que contenga como mínimo 8 carácteres.");
        a.show();
    }

    public void userCreatedAlert() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Usuario creado");
        a.setContentText("El usuario fue creado exitosamente! Inicia sesión para continuar.");
        a.show();
    }

    public void userAlreadyExistAlert() {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Nombre de usuario invalido");
        a.setContentText("Este nombre de usuario ya está siendo utilizado, por favor escoja otro.");
        a.show();
    }
}
