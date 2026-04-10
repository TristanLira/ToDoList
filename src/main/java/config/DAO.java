package config;

import javafx.collections.ObservableList;

public interface DAO<T> {

    ObservableList<T> getAll();

    void create(T t);

    void update(T t);

    void delete(T t);

}
