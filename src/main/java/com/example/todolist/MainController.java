package com.example.todolist;

import com.example.todolist.models.Category;
import com.example.todolist.models.ComboColor;
import com.example.todolist.models.User;
import config.CategoryDAO;
import config.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MainController {

    //todoSection
    public Button showTodoSection;
    public VBox todoSection;

    //categorySection
    public Button showCategorySection;
    public VBox categorySection;
    public TextField categoryNameField;
    public ComboBox <ComboColor> categoryColorComboBox;
    public Button categorySaveButton;
    public Button printCategoriesButton;


    //DAOs
    UserDAO userDAO;
    CategoryDAO catDAO;

    //usuario con sesión iniciada
    User logged;


    public void initialize() {
        //recibe estos datos de la escena de autenticación por lo que se inicializan como null
        userDAO = null;
        logged = null;
        catDAO = null;

        //mostrar la sección principal
        todoSection.toFront();
        todoSection.setVisible(true);
        categorySection.setVisible(false);
    }

    //llamar cuando se haga el cambio de escena para recibir el dao
    public void setUser(User u, UserDAO dao) {
        if (userDAO != null || logged != null) {
            System.out.println("Usuario o DAO ya asignados");
            return;
        }
        this.userDAO = dao;
        this.logged = u;

        /*ya que los DAOs de category y task necesitan el usuario loggeado,
        * se inicializan aquí, hasta haberlo recibido en esta función*/
        this.catDAO = new CategoryDAO(logged);
    }

    /***************** METODOS PARA LA SECCIÓN DE CATEGORÍAS ***********************/

    private void cleanCategoryFields() {
        categoryNameField.clear();
        categoryColorComboBox.setPromptText("Seleccione su color");
    }

    public void saveCategory(ActionEvent actionEvent) {
        Category c = getCategoryFromFields();
        catDAO.create(c);
    }

    private Category getCategoryFromFields() {
        Category c = new Category(logged.getUser(), categoryNameField.getText(), categoryColorComboBox.getValue().color);
        cleanCategoryFields();
        return c;
    }

    public void printCategories(ActionEvent actionEvent) {
        ObservableList<Category> categories = catDAO.getCategories();

        System.out.println("Categorías de " + logged.getUser() + ": ");
        for (Category i: categories) System.out.println(i);
    }

    private void initCategoryComboBox() {
        ObservableList<ComboColor> items = FXCollections.observableArrayList(
                new ComboColor(Color.web("#FAEDCB"),"Amarillo"),
                new ComboColor(Color.web("#C9E4DE"), "Verde"),
                new ComboColor(Color.web("#C6DEF1"), "Azul"),
                new ComboColor(Color.web("#DBCDF0"), "Morado"),
                new ComboColor(Color.web("#F2C6DE"), "Rosa"),
                new ComboColor(Color.web("#F7D9C4"), "Naranja")
        );
        categoryColorComboBox.setItems(items);
    }


    /*************** metodos para mostrar cada sección ******************/

    public void showTodoSection(ActionEvent actionEvent) {
        todoSection.toFront();
        todoSection.setVisible(true);

        categorySection.setVisible(false);
    }

    public void showCategorySection(ActionEvent actionEvent) {
        categorySection.toFront();
        categorySection.setVisible(true);

        todoSection.setVisible(false);

        //-------------------------------

        cleanCategoryFields();
        initCategoryComboBox();
    }

}

