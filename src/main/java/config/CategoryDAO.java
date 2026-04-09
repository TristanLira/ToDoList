package config;

import com.example.todolist.models.Category;
import com.example.todolist.models.User;
import com.google.firebase.database.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class CategoryDAO {

    //usuario al que se le van a registrar las categorías aquí creadas
    private final User user;

    private final FirebaseDatabase db;
    private final DatabaseReference ref;

    ObservableList<Category> categories;

    public CategoryDAO(User user) {
        this.user = user;
        db = FirebaseConnection.getDB();
        ref = db.getReference("categories");
        categories = FXCollections.observableArrayList();
        subscribe();
    }

    /*suscribir el DAO exclusivamente a los cambios de las categories que tengan el mismo
    * userId (para recuperar los datos de solo la cuenta con la que se inició sesión)*/
    private void subscribe() {
        ref.orderByChild("userId").equalTo(user.getUser()).addChildEventListener(new ChildEventListener() {
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

    public ObservableList<Category> getCategories() {
        return categories;
    }

    public void create(Category c) {
        //obtiene el id
        DatabaseReference pushed = ref.push();
        c.setId(pushed.getKey());

        //guarda la categoria
        pushed.setValueAsync(c);
    }

    //El objeto category que reciba puede ser diferente en todos los campos menos en el id generado por firebase
    public void update(Category updated) {
        if (!categories.contains(updated)) return;
        ref.child(updated.getId()).setValueAsync(updated);
    }

    public void delete(Category c) {
        ref.child(c.getId()).removeValueAsync();
    }
}
