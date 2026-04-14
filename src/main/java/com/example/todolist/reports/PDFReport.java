package com.example.todolist.reports;

import com.example.todolist.models.Category;
import com.example.todolist.models.Task;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.IOException;
import java.security.spec.ECField;
import java.time.LocalDate;
import java.util.List;

public class PDFReport implements ReportStrategy {

    private final String dest = "reports/pdfReport.pdf";

    @Override
    public void createReport(List<Task> tasks) throws IOException {
        createFile(); //crea el archivo
        Document document = createPdf(); //crea el pdf

        //crear las fuentes a usar
        PdfFont title = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont text = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        //escribir en el documento

        document.add(getParagraph("Reporte de tareas (generado el " + LocalDate.now() + ")", title, 18));
        document.add(getParagraph("", title, 18));

        for (Task i: tasks) {
            String taskReport = "Descripción: " + i.getDescription() +
                    "\nFecha de creación:" + i.getCreation() +
                    "\nFecha límite: " + i.getDeadline() +
                    "\nCategoría: " + i.getCategoryName() +
                    (i.isCompleted() ? "\ncompletada" : "\nsin completar");

            document.add(getParagraph(i.getName(), title, 12));
            document.add(getParagraph(taskReport, text, 12));
            document.add(new Paragraph("")); //salto de linea

        }

        document.close();

        ReportStrategy.openFile(dest);
    }

    private Paragraph getParagraph(String s, PdfFont font, int size) {
        Paragraph p = new Paragraph(s);
        p.setFont(font);
        p.setFontSize(size);
        return p;
    }

    private void createFile() {
        File file = new File(dest);
        file.getParentFile().mkdirs();
    }

    private Document createPdf() throws IOException {
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        return new Document(pdf);
    }

}