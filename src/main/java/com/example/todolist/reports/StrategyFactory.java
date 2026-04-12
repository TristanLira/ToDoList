package com.example.todolist.reports;

import java.util.HashMap;

public class StrategyFactory {

    private static final HashMap<String, ReportStrategy> strategies;

    static {
        strategies = new HashMap<>();
        strategies.put("PDF", new PDFReport());
        strategies.put("XLSX", new ExcelReport());
    }

    public static ReportStrategy getStrategy(String type) {
        return strategies.get(type);
    }

}
