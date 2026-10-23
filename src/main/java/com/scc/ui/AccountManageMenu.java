package com.scc.ui;
import com.scc.dao.Userdao;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {


    private final Userdao userdao;
    private final Scanner scanner;


    public AccountManageMenu(Userdao Userdao, Scanner scanner) {
        this.userdao = Userdao;
        this.scanner = scanner;

    }


    public void display(user admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================");
            System.out.println("          ADMIN DASHBOARD        ");
            System.out.println("=================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User identity / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();

                default -> System.out.println("-> [ERROR] Invalid option.");

            }
        }
    }

    private void viewAllUsers() {
        try {
            List<user> users = userdao.getalltheuser();
            displayTableUser(users, "ALL REGISTERED USERS");
        } catch (Exception e) {
            System.out.println("Error displaying users: " + e.getMessage());
        }
    }

    private void displayTableUser(List<user> users, String title) {
        System.out.println("\n========== " + title + " ==========");

        if (users == null || users.isEmpty()) {
            System.out.println("No registered users found.");
            return;
        }

        System.out.println("+------+-----------------+-----------------+------------+");
        System.out.printf("| %-4s | %-15s | %-15s | %-10s |%n",
                "ID", "Username", "Identity", "Status");
        System.out.println("+------+-----------------+-----------------+------------+");

        for (user u : users) {
            System.out.printf("| %-4d | %-15s | %-15s | %-10s |%n",
                    u.id(),
                    u.username(),
                    u.identity(),
                    u.status());
        }

        System.out.println("+------+-----------------+-----------------+------------+");
    }
        private void viewAllUser () {
            displayTableUser(userdao.getalltheuser(), "ALL REGISTERED USER");
        }

    }













