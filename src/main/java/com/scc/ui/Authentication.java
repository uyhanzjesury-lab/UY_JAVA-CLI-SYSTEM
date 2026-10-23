package com.scc.ui;

import com.scc.dao.Userdao;
import com.scc.model.user;

import java.util.Scanner;

public class Authentication {
    private final Userdao userDao;
    private final Scanner scanner;

    public Authentication(Userdao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n================================");
            System.out.println(" SCHOOL CLINIC APPOINTMENT SYSTEM");
            System.out.println("================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegistration();
                case "3" -> running = false;
                default -> System.out.println("Invalid option. Please choose 1, 2, or 3.");
            }
        }
        System.out.println("Application closed.");
    }

    private void handleRegistration() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.println("Select identity: [1] Nurse [2] Clinic Staff [3] Registrar [4] Student");
        System.out.print("Choose identity: ");
        String value = scanner.nextLine().trim();

        String identity = switch (value) {
            case "1" -> "Nurse";
            case "2" -> "Clinic Staff";
            case "3" -> "Registrar";
            case "4" -> "Student";
            default -> null;
        };

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password cannot be blank.");
        } else if (identity == null) {
            System.out.println("Invalid identity selection.");
        } else {
            userDao.registerUser(username, password, identity);
        }
    }

    private void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        user loggedInUser = userDao.login(username, password);
        if (loggedInUser == null) {
            return;
        }

        System.out.println("\n-> Welcome " + loggedInUser.username()
                + " | IDENTITY: " + loggedInUser.identity());

        if ("ADMIN".equalsIgnoreCase(loggedInUser.identity())) {
            new AccountManageMenu(userDao, scanner).display(loggedInUser);
        } else {
            System.out.println("Login successful, but a menu for this identity has not been added yet.");
        }
    }
}
