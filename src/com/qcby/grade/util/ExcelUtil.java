package com.qcby.grade.util;

import com.qcby.grade.model.Course;
import com.qcby.grade.model.Grade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.Level;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelUtil {
    private static final String EXPORT_DIR = "exports";
    
    static {
        try {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.OFF);
            ctx.updateLoggers();
        } catch (Exception e) {
        }
        
        File exportDir = new File(EXPORT_DIR);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
    }
    
    public static void exportGradesToExcel(List<Grade> grades, String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("成绩查询结果");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("成绩ID");
            headerRow.createCell(1).setCellValue("课程名称");
            headerRow.createCell(2).setCellValue("课程ID");
            headerRow.createCell(3).setCellValue("学生姓名");
            headerRow.createCell(4).setCellValue("成绩");
            
            for (int i = 0; i < 5; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            for (int i = 0; i < grades.size(); i++) {
                Grade grade = grades.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(grade.getId());
                row.createCell(1).setCellValue(grade.getCourseName());
                row.createCell(2).setCellValue(grade.getCourseId());
                row.createCell(3).setCellValue(grade.getStudentName());
                row.createCell(4).setCellValue(grade.getScore());
            }
            
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            String filepath = EXPORT_DIR + File.separator + filename;
            try (FileOutputStream fileOut = new FileOutputStream(filepath)) {
                workbook.write(fileOut);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
    
    public static void exportCoursesToExcel(List<Course> courses, String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("课程查询结果");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("课程ID");
            headerRow.createCell(1).setCellValue("课程名称");
            
            for (int i = 0; i < 2; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            for (int i = 0; i < courses.size(); i++) {
                Course course = courses.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(course.getCourseId());
                row.createCell(1).setCellValue(course.getCourseName());
            }
            
            for (int i = 0; i < 2; i++) {
                sheet.autoSizeColumn(i);
            }
            
            String filepath = EXPORT_DIR + File.separator + filename;
            try (FileOutputStream fileOut = new FileOutputStream(filepath)) {
                workbook.write(fileOut);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
    
    public static String generateFilename(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + timestamp + ".xlsx";
    }
}