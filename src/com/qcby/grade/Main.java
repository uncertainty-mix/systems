package com.qcby.grade;

import com.qcby.grade.ui.ConsoleUI;
import com.qcby.grade.util.DataInitializer;

public class Main {
    public static void main(String[] args) {
        try {
            DataInitializer.initializeDataFiles();
            ConsoleUI ui = new ConsoleUI();
            ui.start();
        } catch (Exception e) {
            System.err.println("系统运行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}