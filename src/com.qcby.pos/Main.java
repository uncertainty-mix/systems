package com.qcby.pos;

import java.util.Scanner;

public class Main {

    public static void start() {
        Scanner scanner = new Scanner(System.in);
        Ui ui = new Ui(scanner);
        ui.start();
    }

    public static void main(String[] args) {
        start();
    }
}