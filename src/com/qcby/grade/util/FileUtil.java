package com.qcby.grade.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;

public class FileUtil {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_DIR = "data";

    static {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static <T> void saveJson(String filename, T data) {
        try {
            String filepath = DATA_DIR + File.separator + filename;
            try (FileWriter writer = new FileWriter(filepath)) {
                gson.toJson(data, writer);
            }
        } catch (Exception e) {
            throw new RuntimeException("保存文件失败: " + filename, e);
        }
    }

    public static <T> T loadJson(String filename, Type type) {
        try {
            String filepath = DATA_DIR + File.separator + filename;
            File file = new File(filepath);
            
            if (!file.exists()) {
                return null;
            }

            try (FileReader reader = new FileReader(filepath)) {
                return gson.fromJson(reader, type);
            }
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败: " + filename, e);
        }
    }
}