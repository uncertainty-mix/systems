package com.qcby.grade.service;

import com.google.gson.reflect.TypeToken;
import com.qcby.grade.model.User;
import com.qcby.grade.util.CryptoUtil;
import com.qcby.grade.util.FileUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final String USERS_FILE = "users.json";
    private List<User> users;

    public UserService() {
        loadUsers();
        initDefaultUsers();
    }

    private void loadUsers() {
        Type listType = new TypeToken<List<User>>(){}.getType();
        users = FileUtil.loadJson(USERS_FILE, listType);
        if (users == null) {
            users = new ArrayList<>();
        }
    }

    private void initDefaultUsers() {
        if (users.isEmpty()) {
            String teacherSalt = CryptoUtil.generateSalt();
            String teacherHash = CryptoUtil.hashPassword("teacher123", teacherSalt);
            User teacher = new User("teacher", teacherHash, teacherSalt, User.UserRole.TEACHER);
            users.add(teacher);

            String studentSalt = CryptoUtil.generateSalt();
            String studentHash = CryptoUtil.hashPassword("student123", studentSalt);
            User student = new User("student", studentHash, studentSalt, User.UserRole.STUDENT);
            users.add(student);

            saveUsers();
        }
    }

    private void saveUsers() {
        FileUtil.saveJson(USERS_FILE, users);
    }

    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                if (CryptoUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
                    return user;
                }
                break;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}