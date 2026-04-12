package com.example.todolist.reports;

import com.example.todolist.models.Task;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ReportStrategy {

    void createReport(List<Task> tasks) throws IOException;

    static void openFile(String filename) {
        if (Desktop.isDesktopSupported()) {
            try {
                File f = new File(filename);
                Desktop.getDesktop().open(f);
            } catch (IOException ex) {}
        }
    }

}