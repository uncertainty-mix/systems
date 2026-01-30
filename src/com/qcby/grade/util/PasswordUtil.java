package com.qcby.grade.util;

import java.io.Console;
import java.util.Scanner;

public class PasswordUtil {
    
    public static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword(prompt);
            return new String(password);
        } else {
            Scanner scanner = new Scanner(System.in);
            return scanner.nextLine().trim();
        }
    }
}