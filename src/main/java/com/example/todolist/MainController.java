package com.example.todolist;

import com.example.todolist.models.*;
import config.CategoryDAO;
import config.TaskDAO;
import config.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;

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

    //taskSection
    public Button showTaskSection;
    public VBox taskSection;
    public TextField taskNameField;
    public ComboBox <Category> taskCategoryComboBox;
    public DatePicker taskDatePicker;
    public Button taskSaveButton;
    public TextArea taskDescriptionTextArea;
    public Button printTasksButton;


    //lista de categories
    private ObservableList<Category> categories;
    private ObservableList<CategoryRow> catRows;

    //lista de tareas
    private ObservableList<Task> tasks;

    //DAOs
    UserDAO userDAO;
    CategoryDAO catDAO;
    TaskDAO taskDAO;

    //usuario con sesión iniciada
    User logged;


    public void initialize() {
        //recibe estos datos de la escena de autenticación por lo que se inicializan como null
        userDAO = null;
        logged = null;
        catDAO = null;
        taskDAO = null;
    }

    //llamar cuando se haga el cambio de escena para inicializar lo que el controlador necesita
    public void InitControllerData(User u, UserDAO dao) {
        if (userDAO != null || logged != null) {
            System.out.println("Usuario o DAO ya asignados");
            return;
        }

        this.logged = u;

        /*ya que los DAOs de category y task necesitan el usuario loggeado se
        * inicializan aquí, hasta haber recibido al usuario en esta función*/
        this.userDAO = dao;
        this.catDAO = new CategoryDAO(logged);
        this.taskDAO = new TaskDAO(logged);

        //también se inicializan las listas de los models
        this.categories = FXCollections.observableArrayList();
        this.tasks = FXCollections.observableArrayList();

        displayUserLabel.setText("Bienvenido, " + logged.getUser() + ".");

        //inicia la vista principal en la sección de tareas pendientes
        showTodoSection();
    }

    /****************************** métodos para categorySection ******************************/

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
        loadCategoriesList();

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

        /*System.out.println("Recargando sección de categorías, categorías obtenidas: ");
        for (Category i: categories) System.out.println(i);
        System.out.println();*/
    }

    private void loadCategoriesList() {
        //limpia las categories obtenidas del dao
        categories.setAll(catDAO.getAll());
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
        a.setContentText("El nombre de la categoría no es válido. Asegurate que tenga entre 4 y 15 carácteres.");
        a.show();
    }


    /****************************** métodos para taskSection ******************************/

    private void cleanTaskFields() {
        taskNameField.clear();
        taskDescriptionTextArea.clear();
        taskCategoryComboBox.getEditor().clear();
        taskDatePicker.getEditor().clear();
    }

    private void initTaskCategoryComboBox() {
        taskCategoryComboBox.setItems(categories);
    }

    public void saveTask(ActionEvent actionEvent) {
        Task t = getTaskFromFields();

        //maneja el null si no se selecciono categoria
        if (t == null) {
            categoryNotSelectedAlert();
            return;
        }

        //escribir en la base de datos
        System.out.println(t);
        taskDAO.create(t, new Runnable() {
            @Override
            public void run() {
                System.out.println("tarea creada: " + t.getName());
            }
        }, () -> invalidTaskAlert());
    }

    private Task getTaskFromFields() {
        Category c = taskCategoryComboBox.getValue();
        LocalDate d = taskDatePicker.getValue();
        if (c == null || d == null) return null; //regresa null cuando no se selecciona categoria

        Task t = new Task(
                logged.getUser(), c.getId(),
                taskNameField.getText(),
                taskDescriptionTextArea.getText(),
                taskDatePicker.getValue());

        cleanTaskFields();
        return t;
    }

    public void printTasks(ActionEvent actionEvent) {
        loadTasksList();
        System.out.println("Tareas de " + logged.getUser());
        for (Task i: tasks) System.out.println(i);
        System.out.println();
    }

    public void categoryNotSelectedAlert() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Categoría no seleccionada");
        a.setContentText("No fue posible crear la tarea, por favor indique la categoria y la fecha.");
        a.show();
    }

    private void invalidTaskAlert() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Tarea no crea");
        a.setContentText("No fue posible crear la tarea, asegúrese que el nombre tenga entre 4 y" +
                "20 carácteres y la descripción menos de 100 carácteres, y que la fecha aún no haya pasado.");
        a.show();
    }

    /****************************** métodos para todoSection ******************************/

    //carga la lista de tasks y los objetos TasksRow que las representan
    private void loadTasks() {}

    private void loadTasksList() {
        tasks.setAll(taskDAO.getAll());
    }

    /****************************** métodos para mostrar cada sección ******************************/

    public void showTodoSection() {
        todoSection.toFront();
        todoSection.setVisible(true);

        categorySection.setVisible(false);
        taskSection.setVisible(false);
    }

    public void showCategorySection(ActionEvent actionEvent) {
        categorySection.toFront();
        categorySection.setVisible(true);

        todoSection.setVisible(false);
        taskSection.setVisible(false);

        //-------------------------------

        cleanCategoryFields();
        initCategoryComboBox();
        loadCategories();
    }

    public void showTaskSection(ActionEvent actionEvent) {
        taskSection.toFront();
        taskSection.setVisible(true);

        todoSection.setVisible(false);
        categorySection.setVisible(false);

        //-------------------------------

        cleanTaskFields();
        initTaskCategoryComboBox();
        loadCategoriesList(); //carga solo la lista y no los CategoryRow
        loadTasksList();
    }
}

