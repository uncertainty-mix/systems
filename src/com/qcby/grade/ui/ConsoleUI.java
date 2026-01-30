package com.qcby.grade.ui;

import com.qcby.grade.model.Course;
import com.qcby.grade.model.Grade;
import com.qcby.grade.model.User;
import com.qcby.grade.service.CourseService;
import com.qcby.grade.service.GradeService;
import com.qcby.grade.service.UserService;
import com.qcby.grade.util.ExcelUtil;
import com.qcby.grade.util.PasswordUtil;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.qcby.grade.util.StrUtils.repeat;

public class ConsoleUI {
    private Scanner scanner;
    private UserService userService;
    private CourseService courseService;
    private GradeService gradeService;
    private User currentUser;

    public ConsoleUI() {
        scanner = new Scanner(System.in);
        userService = new UserService();
        courseService = new CourseService();
        gradeService = new GradeService();
    }

    public void start() {
        System.out.println("=== 成绩管理系统 ===");
        
        if (!login()) {
            System.out.println("登录失败，系统退出。");
            return;
        }

        showMainMenu();
    }

    private boolean login() {
        System.out.println("\n请登录系统：");
        System.out.println("默认账号：");
        System.out.println("老师 - 用户名: teacher, 密码: teacher123");
        System.out.println("学生 - 用户名: student, 密码: student123");
        
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("\n用户名: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("密码: ");
            String password = PasswordUtil.readPassword("");
            
            currentUser = userService.login(username, password);
            if (currentUser != null) {
                System.out.println("\n登录成功！欢迎 " + 
                    (currentUser.getRole() == User.UserRole.TEACHER ? "老师" : "学生") + 
                    ": " + currentUser.getUsername());
                return true;
            } else {
                System.out.println("用户名或密码错误！剩余尝试次数: " + (2 - attempts));
            }
        }
        return false;
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== 主菜单 ===");
            
            if (currentUser.getRole() == User.UserRole.TEACHER) {
                System.out.println("1. 成绩管理");
                System.out.println("2. 课程管理");
                System.out.println("3. 退出系统");
            } else {
                System.out.println("1. 查询成绩");
                System.out.println("2. 查询课程");
                System.out.println("3. 退出系统");
            }
            
            System.out.print("请选择操作: ");
            String choice = scanner.nextLine().trim();
            
            if (currentUser.getRole() == User.UserRole.TEACHER) {
                handleTeacherChoice(choice);
            } else {
                handleStudentChoice(choice);
            }
        }
    }

    private void handleTeacherChoice(String choice) {
        switch (choice) {
            case "1":
                showGradeManagementMenu();
                break;
            case "2":
                showCourseManagementMenu();
                break;
            case "3":
                System.out.println("感谢使用成绩管理系统，再见！");
                System.exit(0);
                break;
            default:
                System.out.println("无效选择，请重新输入！");
        }
    }

    private void handleStudentChoice(String choice) {
        switch (choice) {
            case "1":
                queryGrades();
                break;
            case "2":
                queryCourses();
                break;
            case "3":
                System.out.println("感谢使用成绩管理系统，再见！");
                System.exit(0);
                break;
            default:
                System.out.println("无效选择，请重新输入！");
        }
    }

    private void addGrade() {
        System.out.println("\n=== 添加成绩 ===");
        
        Course selectedCourse = selectCourse();
        if (selectedCourse == null) {
            return;
        }
        
        System.out.print("请输入学生姓名: ");
        String studentName = scanner.nextLine().trim();
        
        if (studentName.isEmpty()) {
            System.out.println("学生姓名不能为空！");
            return;
        }
        
        List<String> duplicates = gradeService.checkDuplicateStudentNames(selectedCourse.getCourseId(), studentName);
        if (!duplicates.isEmpty()) {
            System.out.println("警告：该课程中已存在该学生的成绩记录：");
            List<Grade> existingGrades = gradeService.getGradesByCourse(selectedCourse.getCourseId()).stream()
                    .filter(g -> g.getStudentName().equals(studentName))
                    .collect(Collectors.toList());
            displayGradesWithoutExport(existingGrades);
            System.out.println("但仍可继续添加成绩。");
        }
        
        double score;
        try {
            System.out.print("请输入成绩 (0-100): ");
            score = Double.parseDouble(scanner.nextLine().trim());
            
            if (score < 0 || score > 100) {
                System.out.println("成绩必须在0-100之间！");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("成绩格式错误！");
            return;
        }
        
        String gradeId = gradeService.addGrade(selectedCourse.getCourseId(), 
                                             selectedCourse.getCourseName(), 
                                             studentName, score);
        
        System.out.println("成绩添加成功！成绩ID: " + gradeId);
    }

    private Course selectCourse() {
        System.out.print("请输入课程名称进行搜索: ");
        String courseName = scanner.nextLine().trim();
        
        if (courseName.isEmpty()) {
            System.out.println("课程名称不能为空！");
            return null;
        }
        
        List<Course> courses = courseService.searchCoursesByName(courseName);
        
        if (courses.isEmpty()) {
            System.out.println("未找到匹配的课程！");
            return null;
        }
        
        if (courses.size() == 1) {
            Course course = courses.get(0);
            System.out.println("找到课程: " + course.getCourseName() + " (ID: " + course.getCourseId() + ")");
            return course;
        }
        
        System.out.println("找到多个匹配的课程：");
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            System.out.println((i + 1) + ". " + course.getCourseName() + " (ID: " + course.getCourseId() + ")");
        }
        
        try {
            System.out.print("请选择课程编号 (1-" + courses.size() + "): ");
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            if (choice >= 1 && choice <= courses.size()) {
                return courses.get(choice - 1);
            } else {
                System.out.println("选择无效！");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误！");
            return null;
        }
    }

    private Course selectCourseForOperation(String operation) {
        System.out.print("请输入要" + operation + "的课程名称进行搜索: ");
        String courseName = scanner.nextLine().trim();
        
        if (courseName.isEmpty()) {
            System.out.println("课程名称不能为空！");
            return null;
        }
        
        List<Course> courses = courseService.searchCoursesByName(courseName);
        
        if (courses.isEmpty()) {
            System.out.println("未找到匹配的课程！");
            return null;
        }
        
        if (courses.size() == 1) {
            Course course = courses.get(0);
            System.out.println("找到课程: " + course.getCourseName() + " (ID: " + course.getCourseId() + ")");
            return course;
        }
        
        System.out.println("找到多个匹配的课程：");
        displayCoursesWithoutExport(courses);
        
        System.out.print("请输入要" + operation + "的课程ID: ");
        String courseId = scanner.nextLine().trim();
        
        if (courseId.isEmpty()) {
            System.out.println("课程ID不能为空！");
            return null;
        }
        
        for (Course course : courses) {
            if (course.getCourseId().equals(courseId)) {
                return course;
            }
        }
        
        System.out.println("在搜索结果中未找到指定ID的课程！");
        return null;
    }

    private Grade selectGradeForOperation(String operation) {
        System.out.print("请输入要" + operation + "的学生姓名进行搜索: ");
        String studentName = scanner.nextLine().trim();
        
        if (studentName.isEmpty()) {
            System.out.println("学生姓名不能为空！");
            return null;
        }
        
        List<Grade> grades = gradeService.getGradesByStudent(studentName);
        
        if (grades.isEmpty()) {
            System.out.println("未找到该学生的成绩记录！");
            return null;
        }
        
        if (grades.size() == 1) {
            Grade grade = grades.get(0);
            System.out.println("找到成绩记录: " + grade.getCourseName() + " - " + grade.getStudentName() + " (成绩ID: " + grade.getId() + ")");
            return grade;
        }
        
        System.out.println("找到该学生的多条成绩记录：");
        displayGradesWithoutExport(grades);
        
        System.out.print("请输入要" + operation + "的成绩ID: ");
        String gradeId = scanner.nextLine().trim();
        
        if (gradeId.isEmpty()) {
            System.out.println("成绩ID不能为空！");
            return null;
        }
        
        for (Grade grade : grades) {
            if (grade.getId().equals(gradeId)) {
                return grade;
            }
        }
        
        System.out.println("在搜索结果中未找到指定ID的成绩记录！");
        return null;
    }

    private void updateGrade() {
        System.out.println("\n=== 修改成绩 ===");
        
        Grade selectedGrade = selectGradeForOperation("修改");
        if (selectedGrade == null) {
            return;
        }
        
        System.out.println("当前成绩信息：");
        System.out.println("课程: " + selectedGrade.getCourseName() + " (ID: " + selectedGrade.getCourseId() + ")");
        System.out.println("学生: " + selectedGrade.getStudentName());
        System.out.println("当前成绩: " + selectedGrade.getScore());
        
        try {
            System.out.print("请输入新成绩 (0-100): ");
            double newScore = Double.parseDouble(scanner.nextLine().trim());
            
            if (newScore < 0 || newScore > 100) {
                System.out.println("成绩必须在0-100之间！");
                return;
            }
            
            if (gradeService.updateGrade(selectedGrade.getId(), newScore)) {
                System.out.println("成绩修改成功！");
            } else {
                System.out.println("成绩修改失败！");
            }
        } catch (NumberFormatException e) {
            System.out.println("成绩格式错误！");
        }
    }

    private void deleteGrade() {
        System.out.println("\n=== 删除成绩 ===");
        
        Grade selectedGrade = selectGradeForOperation("删除");
        if (selectedGrade == null) {
            return;
        }
        
        System.out.println("要删除的成绩信息：");
        System.out.println("课程: " + selectedGrade.getCourseName() + " (ID: " + selectedGrade.getCourseId() + ")");
        System.out.println("学生: " + selectedGrade.getStudentName());
        System.out.println("成绩: " + selectedGrade.getScore());
        
        System.out.print("确认删除？(y/N): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(confirm) || "yes".equals(confirm)) {
            if (gradeService.deleteGrade(selectedGrade.getId())) {
                System.out.println("成绩删除成功！");
            } else {
                System.out.println("成绩删除失败！");
            }
        } else {
            System.out.println("取消删除操作。");
        }
    }

    private void queryGrades() {
        System.out.println("\n=== 查询成绩 ===");
        System.out.println("1. 查看所有成绩");
        System.out.println("2. 按课程查询");
        System.out.println("3. 按学生查询");
        
        System.out.print("请选择查询方式: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                queryAllGrades();
                break;
            case "2":
                queryGradesByCourse();
                break;
            case "3":
                queryGradesByStudent();
                break;
            default:
                System.out.println("无效选择！");
        }
    }

    private void queryAllGrades() {
        List<Grade> grades = gradeService.getAllGrades();
        
        if (grades.isEmpty()) {
            System.out.println("暂无成绩记录。");
            return;
        }
        
        System.out.println("\n所有成绩记录：");
        displayGrades(grades);
    }

    private void queryGradesByCourse() {
        Course selectedCourse = selectCourse();
        if (selectedCourse == null) {
            return;
        }
        
        List<Grade> grades = gradeService.getGradesByCourse(selectedCourse.getCourseId());
        
        if (grades.isEmpty()) {
            System.out.println("该课程暂无成绩记录。");
            return;
        }
        
        System.out.println("\n课程 " + selectedCourse.getCourseName() + " 的成绩记录：");
        displayGrades(grades);
    }

    private void queryGradesByStudent() {
        System.out.print("请输入学生姓名: ");
        String studentName = scanner.nextLine().trim();
        
        if (studentName.isEmpty()) {
            System.out.println("学生姓名不能为空！");
            return;
        }
        
        List<Grade> grades = gradeService.getGradesByStudent(studentName);
        
        if (grades.isEmpty()) {
            System.out.println("该学生暂无成绩记录。");
            return;
        }
        
        if (grades.size() == 1) {
            Grade grade = grades.get(0);
            System.out.println("\n学生 " + studentName + " 的成绩记录：");
            System.out.println("成绩ID: " + grade.getId());
            System.out.println("课程: " + grade.getCourseName() + " (ID: " + grade.getCourseId() + ")");
            System.out.println("学生: " + grade.getStudentName());
            System.out.println("成绩: " + grade.getScore());
            return;
        }
        
        System.out.println("\n学生 " + studentName + " 的成绩记录：");
        displayGrades(grades);
        
        System.out.print("是否查看某条记录的详情？请输入成绩ID（直接回车跳过）: ");
        String gradeId = scanner.nextLine().trim();
        
        if (!gradeId.isEmpty()) {
            for (Grade grade : grades) {
                if (grade.getId().equals(gradeId)) {
                    System.out.println("\n成绩记录详情：");
                    System.out.println("成绩ID: " + grade.getId());
                    System.out.println("课程: " + grade.getCourseName() + " (ID: " + grade.getCourseId() + ")");
                    System.out.println("学生: " + grade.getStudentName());
                    System.out.println("成绩: " + grade.getScore());
                    return;
                }
            }
            System.out.println("未找到指定ID的成绩记录！");
        }
    }

    private void displayGrades(List<Grade> grades) {
        for (Grade grade : grades) {
            System.out.println("成绩ID: " + grade.getId() + 
                             ", 课程: " + grade.getCourseName() + 
                             ", 课程ID: " + grade.getCourseId() + 
                             ", 学生: " + grade.getStudentName() + 
                             ", 成绩: " + grade.getScore());
        }
        System.out.println("共 " + grades.size() + " 条记录");
        
        if (!grades.isEmpty()) {
            askForExcelExport(grades, null);
        }
    }

    private void displayGradesWithoutExport(List<Grade> grades) {
        for (Grade grade : grades) {
            System.out.println("成绩ID: " + grade.getId() + 
                             ", 课程: " + grade.getCourseName() + 
                             ", 课程ID: " + grade.getCourseId() + 
                             ", 学生: " + grade.getStudentName() + 
                             ", 成绩: " + grade.getScore());
        }
        System.out.println("共 " + grades.size() + " 条记录");
    }

    private void displayCourses(List<Course> courses) {
        for (Course course : courses) {
            System.out.println("课程ID: " + course.getCourseId() + 
                             ", 课程名称: " + course.getCourseName());
        }
        System.out.println("共 " + courses.size() + " 条记录");
        
        if (!courses.isEmpty()) {
            askForExcelExport(null, courses);
        }
    }

    private void displayCoursesWithoutExport(List<Course> courses) {
        for (Course course : courses) {
            System.out.println("课程ID: " + course.getCourseId() + 
                             ", 课程名称: " + course.getCourseName());
        }
        System.out.println("共 " + courses.size() + " 条记录");
    }

    private void askForExcelExport(List<Grade> grades, List<Course> courses) {
        System.out.print("是否下载查询结果为Excel文件？(y/N): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(choice) || "yes".equals(choice)) {
            try {
                String filename;
                if (grades != null) {
                    filename = ExcelUtil.generateFilename("成绩查询结果");
                    ExcelUtil.exportGradesToExcel(grades, filename);
                } else {
                    filename = ExcelUtil.generateFilename("课程查询结果");
                    ExcelUtil.exportCoursesToExcel(courses, filename);
                }
                System.out.println("Excel文件已导出: exports/" + filename);
            } catch (Exception e) {
                System.out.println("导出Excel失败: " + e.getMessage());
            }
        }
    }

    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    private void showGradeManagementMenu() {
        while (true) {
            System.out.println("\n=== 成绩管理 ===");
            System.out.println("1. 添加成绩");
            System.out.println("2. 修改成绩");
            System.out.println("3. 删除成绩");
            System.out.println("4. 查询成绩");
            System.out.println("5. 返回主菜单");
            
            System.out.print("请选择操作: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    addGrade();
                    break;
                case "2":
                    updateGrade();
                    break;
                case "3":
                    deleteGrade();
                    break;
                case "4":
                    queryGrades();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("无效选择，请重新输入！");
            }
        }
    }

    private void showCourseManagementMenu() {
        while (true) {
            System.out.println("\n=== 课程管理 ===");
            System.out.println("1. 添加课程");
            System.out.println("2. 修改课程");
            System.out.println("3. 删除课程");
            System.out.println("4. 查询课程");
            System.out.println("5. 返回主菜单");
            
            System.out.print("请选择操作: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    addCourse();
                    break;
                case "2":
                    updateCourse();
                    break;
                case "3":
                    deleteCourse();
                    break;
                case "4":
                    queryCourses();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("无效选择，请重新输入！");
            }
        }
    }

    private void addCourse() {
        System.out.println("\n=== 添加课程 ===");
        
        System.out.print("请输入课程ID: ");
        String courseId = scanner.nextLine().trim();
        
        if (courseId.isEmpty()) {
            System.out.println("课程ID不能为空！");
            return;
        }
        
        System.out.print("请输入课程名称: ");
        String courseName = scanner.nextLine().trim();
        
        if (courseName.isEmpty()) {
            System.out.println("课程名称不能为空！");
            return;
        }
        
        if (courseService.addCourse(courseId, courseName)) {
            System.out.println("课程添加成功！");
        } else {
            System.out.println("课程添加失败！课程ID已存在。");
        }
    }

    private void updateCourse() {
        System.out.println("\n=== 修改课程 ===");
        
        Course selectedCourse = selectCourseForOperation("修改");
        if (selectedCourse == null) {
            return;
        }
        
        System.out.println("当前课程信息：");
        System.out.println("课程ID: " + selectedCourse.getCourseId());
        System.out.println("课程名称: " + selectedCourse.getCourseName());
        
        System.out.print("请输入新的课程名称: ");
        String newCourseName = scanner.nextLine().trim();
        
        if (newCourseName.isEmpty()) {
            System.out.println("课程名称不能为空！");
            return;
        }
        
        if (courseService.updateCourse(selectedCourse.getCourseId(), newCourseName)) {
            System.out.println("课程修改成功！");
        } else {
            System.out.println("课程修改失败！");
        }
    }

    private void deleteCourse() {
        System.out.println("\n=== 删除课程 ===");
        
        Course selectedCourse = selectCourseForOperation("删除");
        if (selectedCourse == null) {
            return;
        }
        
        if (courseService.isCourseInUse(selectedCourse.getCourseId(), gradeService)) {
            System.out.println("警告：该课程已有成绩记录，无法删除！");
            System.out.println("请先删除相关的成绩记录后再删除课程。");
            return;
        }
        
        System.out.println("要删除的课程信息：");
        System.out.println("课程ID: " + selectedCourse.getCourseId());
        System.out.println("课程名称: " + selectedCourse.getCourseName());
        
        System.out.print("确认删除？(y/N): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(confirm) || "yes".equals(confirm)) {
            if (courseService.deleteCourse(selectedCourse.getCourseId())) {
                System.out.println("课程删除成功！");
            } else {
                System.out.println("课程删除失败！");
            }
        } else {
            System.out.println("取消删除操作。");
        }
    }

    private void queryCourses() {
        System.out.println("\n=== 查询课程 ===");
        System.out.println("1. 查看所有课程");
        System.out.println("2. 按课程名称搜索");
        
        System.out.print("请选择查询方式: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                queryAllCourses();
                break;
            case "2":
                searchCoursesByName();
                break;
            default:
                System.out.println("无效选择！");
        }
    }

    private void queryAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        
        if (courses.isEmpty()) {
            System.out.println("暂无课程记录。");
            return;
        }
        
        System.out.println("\n所有课程记录：");
        displayCourses(courses);
    }

    private void searchCoursesByName() {
        System.out.print("请输入课程名称关键字: ");
        String courseName = scanner.nextLine().trim();
        
        if (courseName.isEmpty()) {
            System.out.println("课程名称不能为空！");
            return;
        }
        
        List<Course> courses = courseService.searchCoursesByName(courseName);
        
        if (courses.isEmpty()) {
            System.out.println("未找到匹配的课程。");
            return;
        }
        
        if (courses.size() == 1) {
            Course course = courses.get(0);
            System.out.println("\n搜索结果：");
            System.out.println("课程ID: " + course.getCourseId());
            System.out.println("课程名称: " + course.getCourseName());
            return;
        }
        
        System.out.println("\n搜索结果：");
        displayCourses(courses);
        
        System.out.print("是否查看某个课程的详情？请输入课程ID（直接回车跳过）: ");
        String courseId = scanner.nextLine().trim();
        
        if (!courseId.isEmpty()) {
            for (Course course : courses) {
                if (course.getCourseId().equals(courseId)) {
                    System.out.println("\n课程详情：");
                    System.out.println("课程ID: " + course.getCourseId());
                    System.out.println("课程名称: " + course.getCourseName());
                    return;
                }
            }
            System.out.println("未找到指定ID的课程！");
        }
    }
}