package com.example.todolist;

import com.example.todolist.models.User;
import config.UserDAO;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthenticationController {

    public Label titleLabel;
    public GridPane userDataGrid;
    public TextField userField;
    public PasswordField passwordField;
    public Button loginButton;
    public Button registerButton;

    //BOTÓN PARA INICIAR SESIÓN AUTOMATICAMENTE (DEBUG, BORRAR DESPUÉS)
    public Button debugButton;

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
        System.out.println("Sesion iniciada.");

        try {
            goToMainScene(actionEvent, u);
        } catch (IOException e) {
            System.out.println("Error al cargar la nueva escena: " + e);
            unableToLoadAccountAlert();
        }
    }

    public void createUser(ActionEvent actionEvent) {
        /*llama al evento para crear el usuario y termina. Ya que create recibe la instancia del controlador,
        * una vez termina el evento el DAO indica el resultado usando los métodos de alerta.*/
        User u = getUserFromFields();
        System.out.println(u + "\n");
        userDAO.create(u, this);
    }

    private void goToMainScene(ActionEvent event, User u) throws IOException {
        //cargar el fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent root = loader.load(); // carga la vista

        //obtiene el controlador de la vista desde el loader e inicializa los datos del usuario loggeado
        MainController controller = loader.getController();
        controller.setUser(u, userDAO);

        //obtiene el stage donde está el botón que creó el evento
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
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

    public void unableToLoadAccountAlert() {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setContentText("En este momento no es posible cargar su cuenta. Intente en otro momento.");
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

    /********************** DEBUG ***************************/

    public void quickLogin(ActionEvent actionEvent) {
        try {
            goToMainScene(actionEvent, new User("TristanLira", "password"));
        } catch (IOException e) {}
    }
}
