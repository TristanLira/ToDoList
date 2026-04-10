package config;

import com.example.todolist.AuthenticationController;
import com.example.todolist.models.User;
import com.google.firebase.database.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.regex.*;

public class UserDAO {

    //regex para validar usuarios y contraseñas
    public static final String USER_PASSWORD_REGEX = "[a-zA-Z0-9_-]*";
    private final Pattern pattern;

    DatabaseReference ref; //referencia a la tabla de usuarios
    ObservableList<User> users;

    public UserDAO(){
        ref = FirebaseConnection.getDB().getReference("users");
        users = FXCollections.observableArrayList();
        subscribe();

        pattern = Pattern.compile(USER_PASSWORD_REGEX);
    }

    public ObservableList<User> getAll() {
        return FXCollections.observableArrayList(users); //clona la lista
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

    //create simple
    public void create(User u) {

        if (invalidUser(u)) {
            System.out.println("Usuario invalido: " + u.getUser());
            return;
        }

        if (invalidPassword(u)) {
            System.out.println("Contraseña invalida: " + u.getUser() + ", " + u.getPassword());
            return;
        }

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

    /*En esta otra versión del metodo se recibe el controlador específico de donde fue llamada, para poder notificar
    * dentro de la UI de creación de usuario el resultado (ya sea si se creó exitosamente o si hubo algún error)*/
    public void create(User u, AuthenticationController ac) {

        if (invalidUser(u)) {
            System.out.println("Usuario invalido: " + u.getUser());
            ac.invalidUserAlert();
            return;
        }

        if (invalidPassword(u)) {
            System.out.println("Contraseña invalida: " + u.getUser() + ", " + u.getPassword());
            ac.invalidPasswordAlert();
            return;
        }

        //comprueba que el usuario recibido no existe en la base de datos
        ref.child(u.getUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    ref.child(u.getUser()).setValueAsync(u); //guarda el usuario

                    Platform.runLater(() -> {
                        System.out.println("Usuario creado: " + u.getUser());
                        ac.userCreatedAlert();
                    });
                } else {
                    Platform.runLater(() -> {
                        System.out.println("El usuario " + u.getUser() + " ya existe.");
                        ac.userAlreadyExistAlert();
                    });
                }

            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });

    }

    //validación para el usuario y contraseña, antes de registrar el usuario a la base de datos
    private boolean invalidUser(User u) {
        Matcher matcher = pattern.matcher(u.getUser());
        return !matcher.matches() || u.getUser().length() < 4 || u.getUser().length() > 15;
    }
    private boolean invalidPassword(User u) {
        Matcher matcher = pattern.matcher(u.getPassword());
        return !matcher.matches() || u.getPassword().length() < 8 || u.getPassword().length() > 20;
    }

    //los usuarios son inmutables por lo que no agrega ninguna funcionalidad
    public void update() {}

    public void delete(User u) {
        ref.child(u.getUser()).removeValueAsync();
    }

}
