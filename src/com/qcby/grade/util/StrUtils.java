package com.qcby.grade.util;

public class StrUtils {
    public static String repeat(char ch, int count) {
        if (count <= 0) return "";
        char[] arr = new char[count];
        java.util.Arrays.fill(arr, ch);
        return new String(arr);
    }
}
