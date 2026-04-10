package config;

import com.example.todolist.models.Category;
import com.example.todolist.models.User;
import com.google.firebase.database.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class CategoryDAO implements DAO<Category> {

    //usuario al que se le van a registrar las categorías aquí creadas
    private final User logged;

    private final FirebaseDatabase db;
    private final DatabaseReference ref;

    private final ObservableList<Category> categories;

    public CategoryDAO(User logged) {
        this.logged = logged;
        db = FirebaseConnection.getDB();
        ref = db.getReference("categories");
        categories = FXCollections.observableArrayList();
        subscribe();
    }

    /*suscribir el DAO exclusivamente a los cambios de las categories que tengan el mismo
    * userId (para recuperar los datos de solo la cuenta con la que se inició sesión)*/
    private void subscribe() {
        ref.orderByChild("userId").equalTo(logged.getUser()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Category c = dataSnapshot.getValue(Category.class);
                categories.add(c);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Category c = dataSnapshot.getValue(Category.class);

                /*ya que en el metodo equals de category son iguales solo si tienen el mismo id, se remueve de la lista
                la category que tenga el id recuperado y y se agrega de nuevo modificada*/
                categories.remove(c);
                categories.add(c);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Category c = dataSnapshot.getValue(Category.class);
                categories.remove(c);
            }

            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /******************************** operaciones CRUD ********************************/

    @Override
    public ObservableList<Category> getAll() {
        //return FXCollections.observableArrayList(categories); //clona la lista
        return categories;
    }

    @Override
    public void create(Category c) {
        //obtiene el id
        if (c.getName().length() > 15 || c.getName().length() < 4) {
            System.out.println("No se pudo crear la categoría \"" + c.getName() + "\" (" + c.getUserId() + ")");
            return;
        }
        DatabaseReference pushed = ref.push();
        c.setId(pushed.getKey());
        //guarda la categoria
        pushed.setValueAsync(c);
    }

    //El objeto category que reciba puede ser diferente en todos los campos menos en el id generado por firebase
    @Override
    public void update(Category updated) {
        if (!categories.contains(updated)) return;
        ref.child(updated.getId()).setValueAsync(updated);
    }

    @Override
    public void delete(Category c) {
        ref.child(c.getId()).removeValueAsync();
    }

    /******************************** operaciones con callbacks ********************************/

    public void create(Category c, Runnable success, Runnable fail) {
        //obtiene el id
        if (c.getName().length() > 15 || c.getName().length() < 4) {
            System.out.println("No se pudo crear la categoría \"" + c.getName() + "\" (" + c.getUserId() + ")");
            Platform.runLater(fail);
            return;
        }

        //genera el id
        DatabaseReference pushed = ref.push();
        c.setId(pushed.getKey());

        //guarda la categoria, cuando se completa el guardado ejecuta el runnable de success
        pushed.setValue(c, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    Platform.runLater(success);
                } else {
                    System.out.println("Error al registrar la categoria " + c.getName() + "(" + c.getUserId() + ")");
                }
            }
        });
    }

    public void delete(Category c, Runnable success) {
        ref.child(c.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    System.out.println("Categoria \"" + c.getName() + "\" eliminada correctamente.");
                    Platform.runLater(success);
                }
            }
        });
    }
}

//TODO eliminar también todas las tareas que contenían la categoria eliminada de la base de datos.
