package com.example.todolist;

import com.example.todolist.models.Category;
import com.example.todolist.models.CategoryRow;
import com.example.todolist.models.ComboColor;
import com.example.todolist.models.User;
import config.CategoryDAO;
import config.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class MainController {

    //menú
    public Label displayUserLabel;

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
    private ObservableList<Category> categories;
    private ObservableList<CategoryRow> catRows;

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

        displayUserLabel.setText("Bienvenido, " + logged.getUser() + ".");
    }

    /****************************** METODOS PARA LA SECCIÓN DE CATEGORÍAS ******************************/

    private void cleanCategoryFields() {
        categoryNameField.clear();
        categoryColorComboBox.setPromptText("Seleccione su color");
    }

    public void saveCategory(ActionEvent actionEvent) {
        Category c = getCategoryFromFields();

        /*escribe la entidad en la base de datos, pasando dos metodos. Si la escritura es exitosa, llama a loadCategories,
        * que recarga las CategoryRows dentro de la GUI, pero si no llama a la alerta para indicar al usuario del error.*/
        catDAO.create(c, () -> loadCategories(), () -> invalidCategoryNameAlert());
    }

    private Category getCategoryFromFields() {
        //revisa si el color no es null, y si lo es le da el azul como default
        Color catColor;
        if (categoryColorComboBox.getValue() == null) {
            catColor = categoryColorComboBox.getItems().get(2).color;
        } else {
            catColor = categoryColorComboBox.getValue().color;
        }

        Category c = new Category(logged.getUser(), categoryNameField.getText(), catColor);
        cleanCategoryFields();
        return c;
    }

    //Carga las categorías a la lista y crea los CategoryRow
    public void loadCategories() {
        //limpia las categories obtenidas del dao
        categories = null;
        categories = catDAO.getCategories();

        //limpia la lista de rows
        removeRowsFromGUI();
        catRows = FXCollections.observableArrayList();


        /*los objetos CategoryRow son nodos que extienden de GridPane, muestran una
        * category dentro de la GUI como una columna de una tabla.*/
        for (Category i: categories) {
            CategoryRow cr = new CategoryRow(i);
            catRows.add(cr);

            //agrega un evento para eliminar tanto Category de la bse de datos como su row
            cr.getDeleteButton().setOnAction(actionEvent -> {
                catDAO.delete(i, () -> loadCategories()); //el delete recarga las categorías cargadas para actualizar la lista
                categorySection.getChildren().remove(cr);
            });

            categorySection.getChildren().add(cr);
        }
    }

    private void removeRowsFromGUI() {
        if (catRows == null) return;

        for (CategoryRow i: catRows) {
            categorySection.getChildren().remove(i);
        }

        catRows = null;
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

    public void invalidCategoryNameAlert() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Nombre de categoría invalido");
        a.setContentText("El nombre de la categoría no es válido. Asegurate que tenga entre 4 y 30 carácteres.");
        a.show();
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
        loadCategories();
    }

}

