package com.example.todolist;

import com.example.todolist.controls.CategoryRow;
import com.example.todolist.controls.ComboColor;
import com.example.todolist.controls.TaskRow;
import com.example.todolist.models.*;
import com.example.todolist.reports.ReportStrategy;
import com.example.todolist.reports.StrategyFactory;
import config.CategoryDAO;
import config.TaskDAO;
import config.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

public class MainController {

    //menú
    public Label displayUserLabel;
    public ScrollPane sectionScrollPane;
    public ComboBox<Category> categoryFilterComboBox;
    public TextField textFilterField;

    public StackPane sectionSpace;

    //dueSection
    public Button showDueSection;
    public VBox dueSection;

    //completedSection
    public Button showCompletedSection;
    public VBox completedSection;

    //overdueSection
    public VBox overdueSection;
    public Button showOverdueSection;

    //taskSection
    public Button showTaskSection;
    public VBox taskSection;
    public TextField taskNameField;
    public ComboBox <Category> taskCategoryComboBox;
    public DatePicker taskDatePicker;
    public Button taskSaveButton;
    public TextArea taskDescriptionTextArea;

    //categorySection
    public Button showCategorySection;
    public VBox categorySection;
    public TextField categoryNameField;
    public ComboBox <ComboColor> categoryColorComboBox;
    public Button categorySaveButton;

    //reportSection
    public Button showReportSection;
    public VBox reportSection;
    public ComboBox <Category> reportCategoryComboBox;
    public ComboBox <String> reportStatusComboBox;
    public ComboBox <String> reportFileComboBox;
    public PieChart compareChart;
    public VBox compareChartContainer;
    public VBox barChartContainer;
    public BarChart<String, Number> barChart;


    //lista de categories
    private ObservableList<Category> categories;

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
        initTasksLists();

        //inicializar las combobox de diferentes secciones
        initTaskCategoryComboBox();
        initReportComboBox();
        initCategoryComboBox();


        //inicializar cosillas del menú
        displayUserLabel.setText("Bienvenido, " + logged.getUser() + ".");
        this.categoryFilterComboBox.setItems(categories);

        //inicia la vista principal en la sección de tareas pendientes
        showDueSection();
    }

    private void initTasksLists() {
        this.categories = catDAO.getAll();
        loadCategories();

        this.tasks = taskDAO.getAll();

        this.due = taskDAO.getDue();
        loadDueTasks();

        this.completed = taskDAO.getCompleted();
        loadCompletedTasks();

        this.overdue = taskDAO.getOverdue();
        loadOverdueTasks();
    }


    /****************************** métodos para filtrar tareas ******************************/

    private void cleanFilterFields() {
        categoryFilterComboBox.getEditor().clear();
        categoryFilterComboBox.setValue(null);
        textFilterField.clear();
    }

    private void applyCategoryFilterToSection(Category c, VBox section) {
        for (Node i: section.getChildren()) {
            if (!(i instanceof TaskRow)) continue;

            TaskRow tr = (TaskRow) i;
            if (!tr.getCategory().equals(c)) {
                tr.setVisible(false);
                tr.setManaged(false);
            }
        }
    }

    private void applyTextFilterToSection(String s, VBox section) {
        for (Node i: section.getChildren()) {
            if (!(i instanceof  TaskRow)) continue;

            TaskRow tr = (TaskRow) i;

            //oculta los taskRow que no contengan el string dentro del nombre o la descripción
            if ( !(tr.getTask().getName().contains(s) ||
                    tr.getTask().getDescription().contains(s)) ) {
                tr.setVisible(false);
                tr.setManaged(false);
            }
        }
    }

    private void removeFiltersToSection(VBox section){
        for (Node i: section.getChildren()) {
            if (!(i instanceof TaskRow)) continue;
            i.setVisible(true);
            i.setManaged(true);
        }
    }

    public void filterTasksByCategory(Category c) {
        applyCategoryFilterToSection(c, dueSection);
        applyCategoryFilterToSection(c, completedSection);
        applyCategoryFilterToSection(c, overdueSection);
    }

    public void filterTasksByText(String text) {
        applyTextFilterToSection(text, dueSection);
        applyTextFilterToSection(text, completedSection);
        applyTextFilterToSection(text, overdueSection);
    }

    public void applyAllFilters() {
        Category c = categoryFilterComboBox.getValue();
        String text = textFilterField.getText();

        removeAllFilters();

        if (c != null) filterTasksByCategory(c);
        if (text != null && !text.isEmpty()) filterTasksByText(text);
    }

    public void removeAllFilters() {
        removeFiltersToSection(dueSection);
        removeFiltersToSection(completedSection);
        removeFiltersToSection(overdueSection);
        cleanFilterFields();
    }

    /****************************** métodos para categorySection ******************************/

    private void cleanCategoryFields() {
        categoryNameField.clear();
        categoryColorComboBox.setValue(null);
    }

    public void saveCategory(ActionEvent actionEvent) {
        Category c = getCategoryFromFields();

        /*si no se escribe correctamente llama a la alerta para indicar al usuario del error.*/
        catDAO.create(c,
                () -> System.out.println("Categoria guardada en firebase: " + c),
                () -> invalidCategoryNameAlert());
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
    private void loadCategories() {
        /*crea un listener que escucha cada que se agrega o elimina una categoria a la lista y actualiza dinámicamente las categoryRows*/
        categories.addListener((ListChangeListener<Category>) change -> {
            while (change.next()) {

                if (change.wasAdded()) {

                    for (Category i: change.getAddedSubList()) {
                        Platform.runLater(() -> {
                            categorySection.getChildren().add(createCatRow(i));
                        });
                    }

                } else if (change.wasRemoved()) {

                    for (Category i: change.getRemoved()) {
                        if (i.getUiRow() != null) {
                            Platform.runLater(() -> {
                                categorySection.getChildren().remove(i.getUiRow());
                            });
                        }
                    }

                }

            }
        });

    }

    private CategoryRow createCatRow(Category c) {
        CategoryRow cr = new CategoryRow(c);
        c.setUiRow(cr);

        cr.getDeleteButton().setOnAction(ActionEvent -> {
            catDAO.delete(cr.getCategory(), () -> {
                System.out.println("Categoria eliminada permanentemente: " + cr.getCategory());
            });
        });

        return cr;
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
        taskCategoryComboBox.setValue(null);
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
        taskDAO.create(t,
                () -> System.out.println("tarea agregada a firebase exitosamente: " + t.getName() + "\n"),
                () -> invalidTaskAlert());
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

        t.setCategoryName(c.getName());

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
        a.setContentText("No fue posible crear la tarea, asegúrese que el nombre tenga entre uno y " +
                "30 carácteres y la descripción menos de 200 carácteres, y que la fecha aún no haya pasado.");
        a.show();
    }

    /****************************** métodos para dueSection ******************************/

    /*En el metodo initTasks se inicializa la lista de tareas completadas (que se mostrarán en esta sección) obteniendo una
    * referencia a la lista original en el dao, y después se llama a este metodo que agrega un Listener para todos los cambios
    * que reciba la lista. De esta forma, cada que se agreguen nuevas tareas en la lista del dao, este listener crea el el
    * objeto taskRow que la representa dentro de la GUI, y lo agrega al vbox todoSection*/
    public void loadDueTasks() {
        System.out.println("Cargando las tareas pendientes a la gui");

        due.addListener((ListChangeListener<Task>) change ->{

            while (change.next()) {
                /*este listener se encarga de agregar el taskRow, pero el evento del botón del taskRow se encargará
                de eliminarlo de la sección en el momento que se presione*/
                if (change.wasAdded()) {
                    for (Task i: change.getAddedSubList()) {
                        System.out.println("Nueva tarea en due: " + i);
                        createTaskRow(i); //crea un nuevo taskRow con cada tarea agregada
                    }
                } else if (change.wasRemoved()) {
                    for (Task i: change.getRemoved()) {
                        deleteRowFromSection(i, dueSection);
                        System.out.println("Tarea eliminada de due: " + i);
                    }
                }
            }

        });

    }

    private void deleteRowFromSection(Task t, VBox section) {
        if (t.getUiRow() == null) return;
        Platform.runLater(() -> {
            section.getChildren().remove(t.getUiRow());
        });
    }

    //crea el taskRow, le agrega los eventos al botón y lo muestra en la GUI
    private void createTaskRow(Task t) {
        Category c = catDAO.get(t.getCategoryId());

        if (t == null || c == null) return;

        TaskRow tr = new TaskRow(t, c);
        t.setUiRow(tr); //le asigna al model su representación en la gui

        //agrega el evento al botón para actualizar la tarea y marcarla como completada
        tr.getCompleteButton().setOnAction(ActionEvent -> {
            tr.getTask().setCompleted(true);
            taskDAO.update(tr.getTask());
            dueSection.getChildren().remove(tr); //elimina el row
        });

        //le indica al hilo de javafx que agregue el taskRow
        Platform.runLater(() -> addTaskRowInOrder(tr, dueSection));
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

    /****************************** métodos para completedSection ******************************/

    /*Funciona de la misma manera que loadDueTasks, escuchando los cambios a la lista y cargando los objetos
    * taskRow a la sección de completedSection*/
    public void loadCompletedTasks() {
        System.out.println("Cargando las tareas completadas a la gui");

        completed.addListener((ListChangeListener<Task>) change ->{

            while (change.next()) {
                if (change.wasAdded()) {
                    for (Task i: change.getAddedSubList()) {
                        System.out.println("Nueva tarea en completed: " + i);
                        createDisabledTaskRow(i); //crea un nuevo taskRow con cada tarea agregada
                    }
                } else if (change.wasRemoved()) {
                    for (Task i: change.getRemoved()) {
                        deleteRowFromSection(i, completedSection);
                        System.out.println("Tarea eliminada de completed: " + i);
                    }
                }
            }

        });

    }

    //crea un taskRow con la opción de marcar como completada desactivada y lo agrega a la sección recibida
    private void createDisabledTaskRow(Task t) {
        Category c = catDAO.get(t.getCategoryId());

        if (t == null || c == null) {
            return;
        }

        TaskRow tr = new TaskRow(t, c);
        tr.disableCompletion(); //desactiva el marcar como completada
        t.setUiRow(tr);

        //le indica al hilo de javafx que agregue el taskRow
        //Platform.runLater(() -> addTaskRowInOrder(tr, completedSection));
        Platform.runLater(() -> addTaskRowInCustomOrder(tr));
    }

    /*Agrega primero las tareas no vencidas de mas cercana a más lejana
    * y luego las tareas vencidas de más lejana a más cercana*/
    private void addTaskRowInCustomOrder(TaskRow tr) {
        ObservableList<Node> children = completedSection.getChildren();

        LocalDate today = LocalDate.now();
        boolean isOverdue = tr.getDeadline().isBefore(today);

        for (int i = 0; i < children.size(); i++) {
            if (!(children.get(i) instanceof TaskRow)) continue;

            TaskRow tr2 = (TaskRow) children.get(i);

            boolean isOverdue2 = tr2.getDeadline().isBefore(today);

            //CASO 1, ambas no vencidas: más cercana primero
            if (!isOverdue && !isOverdue2) {
                if (tr.getDeadline().isBefore(tr2.getDeadline())) {
                    children.add(i, tr);
                    return;
                }
            }

            //CASO 2, ambas vencidas: más reciente primero
            else if (isOverdue && isOverdue2) {
                if (tr.getDeadline().isAfter(tr2.getDeadline())) {
                    children.add(i, tr);
                    return;
                }
            }

            //CASO 3: tr NO vencida y tr2 SÍ vencida: tr va antes
            else if (!isOverdue && isOverdue2) {
                children.add(i, tr);
                return;
            }
        }

        // si no encontró posición, va al final
        children.add(tr);
    }

    /****************************** métodos para overdueSection ******************************/

    /*Funciona igual que las dos secciones anteriores*/
    public void loadOverdueTasks() {
        System.out.println("Cargando las tareas vencidas a la gui");

        overdue.addListener((ListChangeListener<Task>) change ->{

            while (change.next()) {
                if (change.wasAdded()) {
                    for (Task i: change.getAddedSubList()) {
                        System.out.println("Nuevo cambio en overdue: " + i);
                        createOverdueTaskRow(i); //crea un nuevo taskRow con cada tarea agregada
                    }
                } else if (change.wasRemoved()) {
                    for (Task i: change.getRemoved()) {
                        deleteRowFromSection(i, overdueSection);
                        System.out.println("Tarea eliminada de completed: " + i);
                    }
                }
            }

        });

    }

    //crea un taskRow con la opción de marcar como completada desactivada y lo agrega a la sección recibida
    private void createOverdueTaskRow(Task t) {
        Category c = catDAO.get(t.getCategoryId());

        if (t == null || c == null) {
            return;
        }

        TaskRow tr = new TaskRow(t, c);
        tr.disableCompletion(); //desactiva el marcar como completada
        tr.highlightOverdue();
        t.setUiRow(tr);

        //le indica al hilo de javafx que agregue el taskRow
        Platform.runLater(() -> addTaskRowInInvertedOrder(tr, overdueSection));
    }

    //inserta la taskRow por fecha, de manera invertida si la fecha ya pasó, y de manera normal si aún no pasa
    private void addTaskRowInInvertedOrder(TaskRow tr, VBox section) {
        ObservableList<Node> children = section.getChildren();

        for (int i = 0; i < children.size(); i++) {
            if (!(children.get(i) instanceof TaskRow)) continue;

            TaskRow tr2 = (TaskRow) children.get(i);

            if (tr.getDeadline().isAfter(tr2.getDeadline())) {
                children.add(i, tr);
                return;
            }
        }
        children.add(tr);
    }

    /****************************** métodos para reportSection (generación de reportes) ******************************/

    public void generateReport() {
        //revisa que se haya seleccionado algo en los filtros
        if (reportStatusComboBox.getValue() == null || reportStatusComboBox.getValue().isEmpty()
                || reportFileComboBox.getValue() == null || reportFileComboBox.getValue().isEmpty()) {
            System.out.println("El reporte no fue creado");
            cleanReportComboBox();
            invalidReportFiltersAlert();
            return;
        }

        //obtiene la lista según el estatus
        ObservableList<Task> tasksToReport = getTasksToReport();

        //si se selecciona un filtro de categoria elimina todas las tareas que no pertenezcan a esa categoria
        if (reportCategoryComboBox.getValue() != null) {
            Category c = reportCategoryComboBox.getValue();
            tasksToReport.removeIf(i -> !i.getCategoryId().equals(c.getId()));
        }

        //ahora que la lista está correctamente filtrada genera la estrategia correspondiente
        ReportStrategy report = StrategyFactory.getStrategy(reportFileComboBox.getValue());

        cleanReportComboBox();

        //crea el reporte dentro de un nuevo hilo para no bloquear el hilo de java fx
        Thread reportThread = new Thread(() -> {
            try {
                report.createReport(tasksToReport);
            } catch (IOException e) {
                System.out.println("Error al crear el reporte: " + e);
            }
        });

        reportThread.start();
    }

    //selecciona una lista con la que escribirá el reporte según el filtro
    private ObservableList<Task> getTasksToReport () {
        ObservableList<Task> list = FXCollections.observableArrayList();

        switch (reportStatusComboBox.getValue()) {
            case "Todas las tareas":
                list.addAll(tasks);
                break;

            case "Completada":
                list.addAll(completed);
                break;

            case "Sin completar":
                list.addAll(due);
                break;

            case "Vencida":
                list.addAll(overdue);
                break;
        }

        return list;
    }

    private void cleanReportComboBox() {
        reportCategoryComboBox.setValue(null);
        reportStatusComboBox.setValue(null);
        reportFileComboBox.setValue(null);
    }

    private void initReportComboBox() {
        reportCategoryComboBox.setItems(categories);

        ObservableList<String> status = FXCollections.observableArrayList(
                "Todas las tareas",
                "Completada",
                "Sin completar",
                "Vencida");
        reportStatusComboBox.setItems(status);

        ObservableList<String> file = FXCollections.observableArrayList(
                "PDF",
                "XLSX");
        reportFileComboBox.setItems(file);

    }

    private void setCompareChart() {
        //crea el gráfico en otro hilo para no congelar el hilo de javafx
        Thread t = new Thread(() -> {
            if (compareChart != null) {
                Platform.runLater(() -> compareChartContainer.getChildren().clear());
            }

            ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList(
                    new PieChart.Data("Tareas completadas", completed.size()),
                    new PieChart.Data("Tareas sin completar", overdue.size())
            );

            compareChart = new PieChart(chartData);
            compareChart.getStyleClass().add("pie-chart");

            //le indica al hilo de javafx que lo agregue al contenedor
            Platform.runLater(() -> compareChartContainer.getChildren().add(compareChart));
        });

        t.start();
    }

    private void setBarChart() {
        //crea el gráfico en otro hilo para no congelar el hilo de javafx
        Thread t = new Thread(() -> {
            if (barChart != null) {
                Platform.runLater(() -> barChartContainer.getChildren().clear());
            }

            HashMap<String, Integer> frequencies = new HashMap<>();

            for (Category i: categories) {
                frequencies.put(i.getId(), 0);
            }

            for (Task i: tasks) {
                String id = i.getCategoryId();
                int n = frequencies.get(id);
                frequencies.put(id, n + 1);
            }

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Categorías");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Tareas");
            yAxis.setTickUnit(1);
            yAxis.setMinorTickCount(0);

            barChart = new BarChart<>(xAxis, yAxis);

            XYChart.Series<String, Number> series = new XYChart.Series<>();


            for (Category i: categories) {
                String name = i.getName();
                int frequency = frequencies.get(i.getId());
                series.getData().add(new XYChart.Data<>(name, frequency));
            }

            barChart.getData().add(series);
            barChart.getStyleClass().add("bar-chart");

            //le indica al hilo de javafx que lo agregue al contenedor
            Platform.runLater(() -> barChartContainer.getChildren().add(barChart));
        });

        t.start();
    }


    private void invalidReportFiltersAlert() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Error al crear el reporte");
        a.setContentText("No se pudo crear el reporte. Por favor seleccione el estatus y archivo.");
        a.show();
    }

    /****************************** métodos para mostrar cada sección ******************************/

    public void showDueSection() {
        showSection(dueSection);
    }

    public void showCategorySection(ActionEvent actionEvent) {
        showSection(categorySection);
    }

    public void showTaskSection(ActionEvent actionEvent) {
        showSection(taskSection);
    }

    public void showCompletedSection(ActionEvent actionEvent) {
        showSection(completedSection);
    }

    public void showOverdueSection(ActionEvent actionEvent) {
        showSection(overdueSection);
    }

    public void showReportSection(ActionEvent actionEvent) {
        showSection(reportSection);

        //inicializa los gráficos
        setCompareChart();
        setBarChart();
    }

    private void showSection(VBox section) {
        for (Node i: sectionSpace.getChildren()) {
            i.setVisible(false);
            i.setManaged(false);
        }

        section.setVisible(true);
        section.setManaged(true);
    }
}

