package com.example.todolist.reports;

import com.example.todolist.models.Task;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelReport implements ReportStrategy {

    private final static String dest = "reports/excelReport.xlsx";

    @Override
    public void createReport(List<Task> tasks) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Java Books");

        //crea tantas filas como tareas, además de otra para el header
        Object[][] cells = new Object[tasks.size() + 1][6];

        //crea las filas
        cells[0] = getHeader();
        for (int i = 1; i <= cells.length; i++) {
            if (!tasks.isEmpty()) cells[i] = getRow(tasks.remove(0));
        }

        for (int i = 0; i < cells.length; i++) {
            Row row = sheet.createRow(i);

            for (int j = 0; j < cells[0].length; j++) {
                Cell cell = row.createCell(j);

                if (cells[i][j] instanceof String) {
                    cell.setCellValue((String) cells[i][j]);
                } else if(cells[i][j] instanceof Boolean) {
                    cell.setCellValue((Boolean) cells[i][j]);
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(dest)) {
            workbook.write(outputStream);
        }

        ReportStrategy.openFile(dest);
    }

    private Object[] getHeader() {
        return new Object[] {
                "Nombre",
                "Descripción",
                "Fecha de creación",
                "Fecha límite",
                "Categoría",
                "Completada"};
    }

    private Object[] getRow(Task t) {
        return new Object[] {
                t.getName(),
                t.getDescription(),
                t.getCreation(),
                t.getDeadline(),
                t.getCategoryName(),
                t.isCompleted()
        };
    }

    private void printMatrix(Object[][] cells) {
        for (int i = 0; i < cells.length; i++) {

            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j] instanceof String) {
                    String s = (String) cells[i][j];
                    System.out.print(s + "\t");
                } else if(cells[i][j] instanceof Boolean) {
                    Boolean b = (Boolean) cells[i][j];
                    System.out.print(b.toString() + "\t");
                }
                System.out.print(" | ");
            }

            System.out.println();

        }
    }
}
