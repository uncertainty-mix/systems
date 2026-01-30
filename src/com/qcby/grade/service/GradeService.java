package com.qcby.grade.service;

import com.google.gson.reflect.TypeToken;
import com.qcby.grade.model.Course;
import com.qcby.grade.model.Grade;
import com.qcby.grade.util.FileUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GradeService {
    private Map<String, List<Grade>> gradesByCourse;
    private int nextGradeId = 1;

    public GradeService() {
        gradesByCourse = new HashMap<>();
        loadAllGrades();
    }

    private void loadAllGrades() {
        CourseService courseService = new CourseService();
        List<Course> courses = courseService.getAllCourses();
        
        for (Course course : courses) {
            String filename = "grades_" + course.getCourseId() + ".json";
            Type listType = new TypeToken<List<Grade>>(){}.getType();
            List<Grade> grades = FileUtil.loadJson(filename, listType);
            
            if (grades == null) {
                grades = new ArrayList<>();
            }
            
            gradesByCourse.put(course.getCourseId(), grades);
            
            for (Grade grade : grades) {
                try {
                    int id = Integer.parseInt(grade.getId());
                    if (id >= nextGradeId) {
                        nextGradeId = id + 1;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    private void saveGradesForCourse(String courseId) {
        String filename = "grades_" + courseId + ".json";
        List<Grade> grades = gradesByCourse.get(courseId);
        if (grades != null) {
            FileUtil.saveJson(filename, grades);
        }
    }

    public String addGrade(String courseId, String courseName, String studentName, double score) {
        String gradeId = String.valueOf(nextGradeId++);
        Grade grade = new Grade(gradeId, courseId, courseName, studentName, score);
        
        gradesByCourse.computeIfAbsent(courseId, k -> new ArrayList<>()).add(grade);
        saveGradesForCourse(courseId);
        
        return gradeId;
    }

    public boolean updateGrade(String gradeId, double newScore) {
        for (List<Grade> grades : gradesByCourse.values()) {
            for (Grade grade : grades) {
                if (grade.getId().equals(gradeId)) {
                    grade.setScore(newScore);
                    saveGradesForCourse(grade.getCourseId());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteGrade(String gradeId) {
        for (Map.Entry<String, List<Grade>> entry : gradesByCourse.entrySet()) {
            List<Grade> grades = entry.getValue();
            for (int i = 0; i < grades.size(); i++) {
                if (grades.get(i).getId().equals(gradeId)) {
                    grades.remove(i);
                    saveGradesForCourse(entry.getKey());
                    return true;
                }
            }
        }
        return false;
    }

    public Grade getGradeById(String gradeId) {
        for (List<Grade> grades : gradesByCourse.values()) {
            for (Grade grade : grades) {
                if (grade.getId().equals(gradeId)) {
                    return grade;
                }
            }
        }
        return null;
    }

    public List<Grade> getAllGrades() {
        List<Grade> allGrades = new ArrayList<>();
        for (List<Grade> grades : gradesByCourse.values()) {
            allGrades.addAll(grades);
        }
        return allGrades;
    }

    public List<Grade> getGradesByCourse(String courseId) {
        return new ArrayList<>(gradesByCourse.getOrDefault(courseId, new ArrayList<>()));
    }

    public List<Grade> getGradesByStudent(String studentName) {
        List<Grade> result = new ArrayList<>();
        for (List<Grade> grades : gradesByCourse.values()) {
            result.addAll(grades.stream()
                    .filter(grade -> grade.getStudentName().equals(studentName))
                    .collect(Collectors.toList()));
        }
        return result;
    }

    public List<String> checkDuplicateStudentNames(String courseId, String studentName) {
        List<String> duplicates = new ArrayList<>();
        List<Grade> grades = gradesByCourse.get(courseId);
        if (grades != null) {
            for (Grade grade : grades) {
                if (grade.getStudentName().equals(studentName)) {
                    duplicates.add("课程: " + grade.getCourseName() + " (ID: " + grade.getCourseId() + ")");
                }
            }
        }
        return duplicates;
    }

    public boolean isCourseInUse(String courseId) {
        List<Grade> grades = gradesByCourse.get(courseId);
        return grades != null && !grades.isEmpty();
    }
}