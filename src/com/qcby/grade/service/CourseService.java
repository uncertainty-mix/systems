package com.qcby.grade.service;

import com.google.gson.reflect.TypeToken;
import com.qcby.grade.model.Course;
import com.qcby.grade.util.FileUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CourseService {
    private static final String COURSES_FILE = "courses.json";
    private List<Course> courses;

    public CourseService() {
        loadCourses();
        initDefaultCourses();
    }

    private void loadCourses() {
        Type listType = new TypeToken<List<Course>>(){}.getType();
        courses = FileUtil.loadJson(COURSES_FILE, listType);
        if (courses == null) {
            courses = new ArrayList<>();
        }
    }

    private void initDefaultCourses() {
        if (courses.isEmpty()) {
            courses.add(new Course("1", "高等数学"));
            courses.add(new Course("2", "大学英语"));
            courses.add(new Course("3", "计算机科学导论"));
            courses.add(new Course("4", "大学物理"));
            courses.add(new Course("5", "普通化学"));
            saveCourses();
        }
    }

    private void saveCourses() {
        FileUtil.saveJson(COURSES_FILE, courses);
    }

    public List<Course> searchCoursesByName(String courseName) {
        return courses.stream()
                .filter(course -> course.getCourseName().contains(courseName))
                .collect(Collectors.toList());
    }

    public Course getCourseById(String courseId) {
        return courses.stream()
                .filter(course -> course.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(courses);
    }

    public boolean addCourse(String courseId, String courseName) {
        if (getCourseById(courseId) != null) {
            return false;
        }
        courses.add(new Course(courseId, courseName));
        saveCourses();
        return true;
    }

    public boolean updateCourse(String courseId, String newCourseName) {
        Course course = getCourseById(courseId);
        if (course != null) {
            course.setCourseName(newCourseName);
            saveCourses();
            return true;
        }
        return false;
    }

    public boolean deleteCourse(String courseId) {
        Course course = getCourseById(courseId);
        if (course != null) {
            courses.remove(course);
            saveCourses();
            return true;
        }
        return false;
    }

    public boolean isCourseInUse(String courseId, GradeService gradeService) {
        return gradeService.isCourseInUse(courseId);
    }
}