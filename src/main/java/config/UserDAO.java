package config;

import com.example.todolist.models.User;
import com.google.firebase.database.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserDAO {

    DatabaseReference ref; //referencia a la tabla de usuarios
    ObservableList<User> users;

    public UserDAO(){
        ref = FirebaseConnection.getDB().getReference("users");
        users = FXCollections.observableArrayList();
        subscribe();
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    //suscribe el dao a los eventos
    private void subscribe() {
        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User u = dataSnapshot.getValue(User.class);
                users.add(u);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                users.remove(u);
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void create(User u) {
        //comprueba que el usuario recibido no existe en la base de datos
        ref.child(u.getUser()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ref.child(u.getUser()).setValueAsync(u); //guarda el usuario
                    System.out.println("Usuario creado: " + u.getUser());
                } else {
                    System.out.println("El usuario " + u.getUser() + " ya existe.");
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void delete(User u) {
        ref.child(u.getUser()).removeValueAsync();
    }

}
