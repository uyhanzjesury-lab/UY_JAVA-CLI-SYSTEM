package com.scc.ui;

import com.scc.dao.Userdao;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {
    private final Userdao userdao;
    private final Scanner scanner;

    public AccountManageMenu(Userdao userdao, Scanner scanner) {
        this.userdao = userdao;
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
            System.out.println("5. Update User Role & Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> addUser();
                case "5" -> updateUserProcess(admin);
                case "6" -> deleteUser();
                case "7" -> loggedIn = false;
                default -> System.out.println("Invalid option. Please choose 1-7.");
            }
        }
        System.out.println("Admin logged out.");
    }

    private void viewAllUsers() {
        displayTableUser(userdao.getAllUsers(), "ALL REGISTERED USERS");
    }

    private void viewPendingUsers() {
        displayTableUser(userdao.getPendingUsers(), "PENDING USER APPROVALS");
    }

    private void approveUser() {
        viewPendingUsers();
        int userId = readIntInput("Enter the ID to approve (0 to cancel): ");

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }
        userdao.approveUser(userId);
    }

    private void addUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter identity (NURSE, CLINIC STAFF, REGISTRAR, STUDENT, ADMIN): ");
        String identity = scanner.nextLine().trim();

        if (username.isBlank() || password.isBlank() || identity.isBlank()) {
            System.out.println("All fields are required.");
            return;
        }

        if (userdao.addUserByAdmin(username, password, identity, "APPROVED")) {
            System.out.println("User added and approved successfully.");
        }
    }

    private void updateUserProcess(user currentAdmin) {
        viewAllUsers();

        int userId = readIntInput("Enter selected ID to update (0 to cancel): ");
        if (userId == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        // Self-Edit Restriction Warning Check
        if (userId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Operation cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Operation cancelled.");
            return;
        }

        userdao.updateUserRoleAndStatus(userId, newRole, newStatus);
    }

    // --- Exception-Safe Numeric Input Wrapper ---
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please enter a valid number.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (STUDENT, TEACHER, ADMIN, NURSE, CLINIC STAFF, REGISTRAR) or 0 to cancel: ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0") || role.equals("CANCEL")) {
                return null;
            }
            if (role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN") ||
                    role.equals("NURSE") || role.equals("CLINIC STAFF") || role.equals("REGISTRAR")) {
                return role;
            }
            System.out.println("Invalid role! Please enter a valid role option.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED, PENDING) or 0 to cancel: ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0") || status.equals("CANCEL")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status! Please enter APPROVED or PENDING.");
        }
    }

    private void deleteUser() {
        viewAllUsers();
        int id = readIntInput("Enter user ID to delete (0 to cancel): ");
        if (id == 0) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (userdao.deleteUser(id)) {
            System.out.println("User account deleted.");
        } else {
            System.out.println("User was not found, or the administrator account cannot be deleted.");
        }
    }

    private void displayTableUser(List<user> users, String title) {
        System.out.println("\n========== " + title + " ==========");
        if (users == null || users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.printf("%-5s %-20s %-20s %-12s%n", "ID", "Username", "Identity", "Status");
        System.out.println("---------------------------------------------------------------");
        for (user u : users) {
            System.out.printf("%-5d %-20s %-20s %-12s%n",
                    u.id(), shorten(u.username(), 20), shorten(u.identity(), 20), shorten(u.status(), 12));
        }
    }

    private String shorten(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }
}