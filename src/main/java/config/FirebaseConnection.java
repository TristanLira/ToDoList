package config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;

public class FirebaseConnection {

    private static FirebaseDatabase db;

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        try {

            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/com/example/todolist/firebase_connection.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://todolist-40a4f-default-rtdb.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Conexion a firebase inicializada.");
            db = FirebaseDatabase.getInstance();
            initialized = true;

        } catch (Exception e) {
            System.out.println("Error de conexion a firebase: " + e);
        }
    }

    public static FirebaseDatabase getDB() {
        if (db == null) init();
        return db;
    }
}
