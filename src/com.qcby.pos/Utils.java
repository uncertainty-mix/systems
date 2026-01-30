package com.qcby.pos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Utils {


    private static boolean isValidPrice(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        int dotIndex = s.indexOf('.');
        boolean validFormat;
        if (dotIndex == -1) {
            // 没有小数点，检查是否全是数字
            validFormat = isAllDigits(s);
        } else {
            // 有小数点
            String intPart = s.substring(0, dotIndex);
            String decPart = s.substring(dotIndex + 1);
            // 整数部分不能为空且必须全是数字，小数部分长度1-2位且全是数字
            validFormat = !intPart.isEmpty() && isAllDigits(intPart)
                    && decPart.length() >= 1 && decPart.length() <= 2 && isAllDigits(decPart);
        }
        if (!validFormat) {
            return false;
        }
        // 检查价格必须大于0
        BigDecimal price = new BigDecimal(s);
        return price.compareTo(BigDecimal.ZERO) > 0;
    }

    private static boolean isAllDigits(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String scanNonEmptyName(Scanner sc, String prompt) {
        while (true) {
            System.out.println(prompt);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) {
                System.out.println("名称不能为空，请重新输入。");
                continue;
            }
            return s;
        }
    }

    public static BigDecimal scanPrice(Scanner sc, String prompt) {
        while (true) {
            System.out.println(prompt);
            String s = sc.next().trim();
            sc.nextLine(); // 消费换行符
            if (!isValidPrice(s)) {
                System.out.println("价格输入不合法，请重新输入（例：10 或 10.50）。");
                continue;
            }
            return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public static int scanPositiveInt(Scanner sc, String prompt) {
        while (true) {
            System.out.println(prompt);
            if (!sc.hasNextInt()) {
                String invalid = sc.next();
                System.out.println("输入不合法，请输入正整数。");
                continue;
            }
            int v = sc.nextInt();
            sc.nextLine();
            if (v <= 0) {
                System.out.println("必须大于0，请重新输入。");
                continue;
            }
            return v;
        }
    }

    public static boolean scanConfirm(Scanner sc, String prompt) {
        System.out.println(prompt);
        String confirm = sc.next().trim();
        sc.nextLine(); // 消费换行符
        return confirm.equals("y") || confirm.equals("Y");
    }


    public static String scanNameOptional(Scanner sc, String prompt, String currentValue) {
        System.out.println(prompt + "（当前：" + currentValue + "）");
        String s = sc.nextLine().trim();
        if (s.isEmpty()) {
            return currentValue;
        }
        return s;
    }


    public static BigDecimal scanPriceOptional(Scanner sc, String prompt, BigDecimal currentValue) {
        while (true) {
            System.out.println(prompt + "（当前：" + currentValue.toPlainString() + "）");
            String s = sc.nextLine().trim();
            if (s.isEmpty()) {
                return currentValue;
            }
            if (!isValidPrice(s)) {
                System.out.println("价格输入不合法，请重新输入（例：10 或 10.50）。");
                continue;
            }
            return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public static int scanPositiveIntOptional(Scanner sc, String prompt, int currentValue) {
        while (true) {
            System.out.println(prompt + "（当前：" + currentValue + "）");
            String s = sc.nextLine().trim();
            if (s.isEmpty()) {
                return currentValue;
            }
            if (!isAllDigits(s)) {
                System.out.println("输入不合法，请输入正整数。");
                continue;
            }
            int v = Integer.parseInt(s);
            if (v <= 0) {
                System.out.println("必须大于0，请重新输入。");
                continue;
            }
            return v;
        }
    }

    public static void consumeNewline(Scanner sc) {
        if (sc.hasNextLine()) {
            sc.nextLine();
        }
    }

    public static int scanPurchaseQuantity(Scanner sc, String prompt, int maxQuantity) {
        while (true) {
            System.out.println(prompt + "（最大可购买：" + maxQuantity + "，输入0退出购买）");
            if (!sc.hasNextInt()) {
                String invalid = sc.next();
                System.out.println("输入不合法，请输入数字。");
                continue;
            }
            int v = sc.nextInt();
            sc.nextLine();
            if (v < 0) {
                System.out.println("数量不能为负数，请重新输入。");
                continue;
            }
            if (v == 0) {
                System.out.println("取消购买该商品。");
                return 0;
            }
            if (v > maxQuantity) {
                System.out.println("购买数量不能超过库存数量（" + maxQuantity + "），请重新输入。");
                continue;
            }
            return v;
        }
    }

}
