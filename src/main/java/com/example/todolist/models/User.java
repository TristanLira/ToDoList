package com.example.todolist.models;

public class User {

    private String user;
    private String password;

    public User() {
        this.user = "";
        this.password = "";
    }

    public User(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "user: " + this.user + ", password: " + this.password;
    }

    //sobreescribe el metodo equals para comprobar si los usuarios recuperados de la base de datos coinciden con el usuario que se intenta iniciar sesión
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof User)) return false;

        User u = (User) o;

        return u.getUser().equals(this.user) && u.getPassword().equals(this.password);
    }

}