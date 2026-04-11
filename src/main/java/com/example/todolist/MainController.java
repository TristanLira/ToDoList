package com.example.todolist;

import com.example.todolist.models.*;
import config.CategoryDAO;
import config.TaskDAO;
import config.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.Comparator;

public class MainController {

    //menú
    public Label displayUserLabel;
    public ScrollPane sectionScrollPane;

    //dueSection
    public Button showDueSection;
    public VBox dueSection;

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


    //lista de categories
    private ObservableList<Category> categories;
    private ObservableList<CategoryRow> catRows;

    //lista de tareas, según su clasifición
    private ObservableList<Task> tasks; //todas las tareas del usuario
    private ObservableList<Task> completed; //tareas completadas
    private ObservableList<Task> due; //tareas pendientes
    private ObservableList<Task> overdue; //tareas vencidas

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
            System.out.println("Usuario o DAO ya asignados para mainController");
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
        initTasksLists();

        displayUserLabel.setText("Bienvenido, " + logged.getUser() + ".");

        //inicia la vista principal en la sección de tareas pendientes
        showDueSection();
    }

    private void initTasksLists() {
        this.tasks = taskDAO.getAll();

        this.completed = taskDAO.getCompleted();

        this.due = taskDAO.getDue();
        loadDueTasks();

        this.overdue = taskDAO.getOverdue();
    }


    /****************************** métodos para filtrar tareas ******************************/

    /*TODO: agregar combo box para seleccionar la categoria para filtrar, modificar filterByCategory y
       removeAllFilters para que filtren completedSection y overdueSection*/

    private void filterByCategory(Category c) {
        applyCategoryFilterToSection(c, dueSection);
    }

    public void removeAllFilters() {
        removeCategoryFilterToSection(dueSection);
    }

    private void applyCategoryFilterToSection(Category c, VBox section){
        for (Node i: section.getChildren()) {
            if (!(i instanceof TaskRow)) continue;

            TaskRow tr = (TaskRow) i;
            if (!tr.getCategory().equals(c)) {
                tr.setVisible(false);
                tr.setManaged(false);
            }
        }
    }

    private void removeCategoryFilterToSection(VBox section){
        for (Node i: section.getChildren()) {
            if (!(i instanceof TaskRow)) continue;
            i.setVisible(true);
            i.setManaged(true);
        }
    }

    /*PRUEBA, ELIMINAR DESPUÉS*/
    public void filter() {
        Category c = new Category();
        c.setId("-OptsB6UfTgiS3GPQqZt");
        filterByCategory(c);
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
                new ComboColor(Color.web("#FFEE8C"),"Amarillo"),
                new ComboColor(Color.web("#A9E9A4"), "Verde"),
                new ComboColor(Color.web("#84B6F4"), "Azul"),
                new ComboColor(Color.web("#C18FFF"), "Morado"),
                new ComboColor(Color.web("#F56574"), "Rojo"),
                new ComboColor(Color.web("#FFAE6A"), "Naranja")
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
        taskDAO.create(t, new Runnable() {
            @Override
            public void run() {
                System.out.println("tarea agregada a firebase exitosamente: " + t.getName() + "\n");
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

    /****************************** métodos para dueSection ******************************/

    /*En el metodo initTasks se inicializa la lista de tareas completadas (que se mostrarán en esta sección) obteniendo una
    * referencia a la lista original en el dao, y después se llama a este metodo que agrega un Listener para todos los cambios
    * que reciba la lista. De esta forma, cada que se agreguen nuevas tareas en la lista del dao, este listener crea el el
    * objeto taskRow que la representa dentro de la GUI, y lo agrega al vbox todoSection*/
    public void loadDueTasks() {
        System.out.println("Cargando las tareas a la gui");

        due.addListener((ListChangeListener<Task>) change ->{

            while (change.next()) {
                if (change.wasAdded()) {
                    for (Task i: change.getAddedSubList()) {
                        System.out.println("Nuevo cambio en due: " + i);
                        createTaskRow(i); //crea un nuevo taskRow con cada tarea agregada
                    }
                }
            }

        });

    }

    //crea el taskRow, le agrega los eventos al botón y lo muestra en la GUI
    private void createTaskRow(Task t) {
        Category c = catDAO.get(t.getCategoryId());

        if (t == null || c == null) {
            return;
        }

        TaskRow tr = new TaskRow(t, c);

        //le indica al hilo de javafx que agregue el taskRow
        Platform.runLater(() -> addTaskRowInOrder(tr, dueSection));
        //Platform.runLater(() -> dueSection.getChildren().add(tr));
    }

    //inserta la taskRow según la fecha
    private void addTaskRowInOrder(TaskRow tr, VBox section) {
        ObservableList<Node> children = section.getChildren();

        for (int i = 0; i < children.size(); i++) {
            if (!(children.get(i) instanceof TaskRow)) continue;

            TaskRow tr2 = (TaskRow) children.get(i);

            if (tr.getDeadline().isBefore(tr2.getDeadline())) {
                children.add(i, tr);
                return;
            }
        }
        //si no encuentra ninguna tarea con fecha mejor la agrega al final
        children.add(tr);
    }

    /****************************** métodos para mostrar cada sección ******************************/

    public void showDueSection() {
        //mostrar todoSection
        dueSection.setVisible(true);
        dueSection.setManaged(true);

        //ocultar las demás

        categorySection.setVisible(false);
        categorySection.setManaged(false);

        taskSection.setVisible(false);
        taskSection.setManaged(false);
    }

    public void showCategorySection(ActionEvent actionEvent) {
        //mostrar categorySection
        categorySection.setVisible(true);
        categorySection.setManaged(true);

        //ocultar las demás

        dueSection.setVisible(false);
        dueSection.setManaged(false);

        taskSection.setVisible(false);
        taskSection.setManaged(false);

        //-------------------------------

        cleanCategoryFields();
        initCategoryComboBox();
        loadCategories();
    }

    public void showTaskSection(ActionEvent actionEvent) {
        //mostrar taskSection
        taskSection.setVisible(true);
        taskSection.setManaged(true);

        //ocultar las demás

        dueSection.setVisible(false);
        dueSection.setManaged(false);

        categorySection.setVisible(false);
        categorySection.setManaged(false);

        //-------------------------------

        cleanTaskFields();
        initTaskCategoryComboBox();
        loadCategoriesList(); //carga solo la lista y no los CategoryRow
    }
}

