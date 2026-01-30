package com.qcby.grade.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataInitializer {
    
    public static void initializeDataFiles() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        copyResourceToFile("/users.json", "data/users.json");
        copyResourceToFile("/courses.json", "data/courses.json");
        copyResourceToFile("/grades_1.json", "data/grades_1.json");
        copyResourceToFile("/grades_MATH001.json", "data/grades_MATH001.json");
        copyResourceToFile("/grades_ENG001.json", "data/grades_ENG001.json");
        copyResourceToFile("/goods_data.json", "data/goods_data.json");
        copyResourceToFile("/purchase_records.json", "data/purchase_records.json");
    }
    
    private static void copyResourceToFile(String resourcePath, String targetPath) {
        File targetFile = new File(targetPath);
        if (targetFile.exists()) {
            return;
        }
        
        try (InputStream inputStream = DataInitializer.class.getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                Files.copy(inputStream, Paths.get(targetPath));
            }
        } catch (IOException e) {
            System.err.println("无法复制资源文件: " + resourcePath + " 到 " + targetPath);
        }
    }
}