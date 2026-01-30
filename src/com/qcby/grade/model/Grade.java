package com.qcby.grade.model;

public class Grade {
    private String id;
    private String courseId;
    private String courseName;
    private String studentName;
    private double score;

    public Grade() {}

    public Grade(String id, String courseId, String courseName, String studentName, double score) {
        this.id = id;
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentName = studentName;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id='" + id + '\'' +
                ", courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", studentName='" + studentName + '\'' +
                ", score=" + score +
                '}';
    }
}