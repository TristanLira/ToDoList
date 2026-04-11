package config;

import com.example.todolist.models.Task;
import com.example.todolist.models.User;
import com.google.firebase.database.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class TaskDAO implements DAO <Task>{

    private final User logged;

    private final FirebaseDatabase db;
    private final DatabaseReference ref;

    private final ObservableList<Task> tasks; //todas las tareas

    //listas para clasificar las tareas
    private final ObservableList<Task> completed;
    private final ObservableList<Task> due;
    private final ObservableList<Task> overdue;

    public TaskDAO(User logged) {
        db = FirebaseConnection.getDB();
        ref = db.getReference("tasks");
        this.logged = logged;

        tasks = FXCollections.observableArrayList();
        completed = FXCollections.observableArrayList();
        due = FXCollections.observableArrayList();
        overdue = FXCollections.observableArrayList();
        subscribe();
    }

    private void subscribe() {
        //suscribe el dao a los cambios de todas las tareas pertenecientes al usuario
        ref.orderByChild("userId").equalTo(logged.getUser()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Task t = snapshot.getValue(Task.class);
                tasks.add(t);

                //System.out.println(t);

                //después de agregar la tarea a la lista la clasifica
                if (t.isCompleted()) {
                    completed.add(t);
                }
                //si la fecha de vencimiento aún no pasa la clasifica como pendiente
                else if(t.obtainDeadlineObj().isAfter(LocalDate.now()) ||
                        t.obtainDeadlineObj().isEqual(LocalDate.now())) {
                    due.add(t);
                }
                //si no ninguna de las dos la clasifica como vencida
                else {
                    overdue.add(t);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                Task t = snapshot.getValue(Task.class);

                /*remove busca el elemento a eliminar con el metodo equals, por lo que al pasarle el objeto recuperado
                * elimina de la lista el elemento con el mismo id, sin importar ningún otro parámetro*/
                tasks.remove(t);
                tasks.add(t);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Task t = snapshot.getValue(Task.class);
                tasks.remove(t);
            }

            @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    /******************************** operaciones CRUD ********************************/

    @Override
    public ObservableList<Task> getAll() {
        //return FXCollections.observableArrayList(tasks); //copia la lista
        return tasks;
    }

    public Task get(String id) {
        for (Task i: tasks) {
            if (i.getId().equals(id)) {
                return i;
            }
        }
        return null;
    }

    @Override
    public void create(Task t) {
        if (t.getName().length() > 15 || t.getName().length() < 4 || t.getDescription().length() > 100) {
            System.out.println("No fue posible crear la tarea: " + t.getName() + "(" + t.getUserId() + ")");
            return;
        }

        //no crea la tarea si la fecha ya pasó
        if (t.obtainDeadlineObj().isBefore(LocalDate.now())) {
            System.out.println("No fue posible crear la tarea \"" + t.getName() + "\" porque la fecha ya paso.");
            return;
        }

        //agregar id
        DatabaseReference pushed = ref.push();
        t.setId(pushed.getKey());

        //agregar tarea a firebase
        pushed.setValueAsync(t);
    }

    @Override
    public void update(Task t) {
        if (!tasks.contains(t)){
            System.out.println("No se puede actualizar la tarea \"" + t.getName() + "\" porque no existe en firebase.");
            return;
        }
        ref.child(t.getId()).setValueAsync(t);
    }

    @Override
    public void delete(Task t) {
        ref.child(t.getId()).removeValueAsync();
    }

    /******************************** operaciones con callbacks ********************************/

    //create con callbacks
    public void create(Task t, Runnable success, Runnable fail) {
        if (t.getName().length() > 15 || t.getName().length() < 4 || t.getDescription().length() > 100) {
            System.out.println("No fue posible crear la tarea: " + t.getName() + " (" + t.getUserId() + ")");
            Platform.runLater(fail);
            return;
        }

        //no crea la tarea si la fecha ya pasó
        if (t.obtainDeadlineObj().isBefore(LocalDate.now())) {
            System.out.println("No fue posible crear la tarea \"" + t.getName() + "\" porque la fecha ya paso.");
            Platform.runLater(fail);
            return;
        }

        //agregar id
        DatabaseReference pushed = ref.push();
        t.setId(pushed.getKey());

        //agregar tarea a firebase
        pushed.setValue(t, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    System.out.println("Tarea \"" + t.getName() + "\" agregada correctamente a firebase.");
                    Platform.runLater(success);
                } else {
                    System.out.println("Error al crear la tarea: " + t.getName() + "(" + t.getUserId() + ")");
                }
            }
        });
    }

    //delete con callbacks
    public void delete(Task t, Runnable success) {
        ref.child(t.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    System.out.println("tarea eliminada correctamente. " + t);
                    Platform.runLater(success);
                }
            }
        });
    }

    /******************************** consultas específicas ********************************/

    public ObservableList<Task> getCompleted() {
        return completed;
    }

    public ObservableList<Task> getDue() {
        return due;
    }

    public ObservableList<Task> getOverdue() {
        return overdue;
    }
}
