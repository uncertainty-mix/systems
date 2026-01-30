package com.qcby.pos;

import com.qcby.grade.util.DataInitializer;
import java.util.Scanner;

public class Main {

    public static void start() {
        DataInitializer.initializeDataFiles();
        Scanner scanner = new Scanner(System.in);
        Ui ui = new Ui(scanner);
        ui.start();
    }

    public static void main(String[] args) {
        start();
    }
}