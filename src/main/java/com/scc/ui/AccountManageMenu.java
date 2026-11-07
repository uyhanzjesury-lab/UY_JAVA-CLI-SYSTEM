package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.Userdao;
import com.scc.model.ActivityLog;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {
    private final Userdao userdao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public AccountManageMenu(Userdao userdao, Scanner scanner) {
        this.userdao = userdao;
        this.logDao = new ActivityLogDao();
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
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser(admin);
                case "4" -> addUser(admin);
                case "5" -> updateUserProcess(admin);
                case "6" -> deleteUser(admin);
                case "7" -> auditLogMenu();
                case "8" -> loggedIn = false;
                default -> System.out.println("Invalid option. Please choose 1-8.");
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

    private void approveUser(user admin) {
        viewPendingUsers();
        int userId = readIntInput("Enter the ID to approve (0 to cancel): ");

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }
        userdao.approveUser(userId, admin.id());
    }

    private void addUser(user admin) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter identity (NURSE, CLINIC STAFF, REGISTRAR, STUDENT, SCHOOL FACULTY STAFF, ADMIN): ");
        String identity = scanner.nextLine().trim();

        if (username.isBlank() || password.isBlank() || identity.isBlank()) {
            System.out.println("All fields are required.");
            return;
        }

        if (userdao.addUserByAdmin(username, password, identity, "APPROVED", admin.id())) {
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

        userdao.updateUserRoleAndStatus(userId, newRole, newStatus, currentAdmin.id());
    }

    private void deleteUser(user admin) {
        viewAllUsers();
        int id = readIntInput("Enter user ID to delete (0 to cancel): ");
        if (id == 0) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (userdao.deleteUser(id, admin.id())) {
            System.out.println("User account deleted.");
        } else {
            System.out.println("User was not found, or the administrator account cannot be deleted.");
        }
    }

    // --- Admin Audit Log Interface & Menu with Clear Option ---
    private void auditLogMenu() {
        boolean viewing = true;
        while (viewing) {
            System.out.println("\n=================================");
            System.out.println("       SYSTEM AUDIT LOGS         ");
            System.out.println("=================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Audit Logs");
            System.out.println("4. Back to Admin Dashboard");
            System.out.print("Choose an option (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> displayAuditLogs(logDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
                case "2" -> {
                    int filterUserId = readIntInput("Enter User ID to filter by: ");
                    displayAuditLogs(logDao.getLogsByUserId(filterUserId), "LOGS FOR USER ID: " + filterUserId);
                }
                case "3" -> {
                    System.out.print("Are you sure you want to delete ALL audit logs from the database? (y/n): ");
                    String confirm = scanner.nextLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        if (logDao.clearAllLogs()) {
                            System.out.println("-> All audit logs have been successfully cleared.");
                        } else {
                            System.out.println("-> Failed to clear audit logs.");
                        }
                    } else {
                        System.out.println("-> Action cancelled.");
                    }
                }
                case "4" -> viewing = false;
                default -> System.out.println("Invalid option. Please choose between 1 and 4.");
            }
        }
    }

    private void displayAuditLogs(List<ActivityLog> logs, String title) {
        System.out.println("\n================ " + title + " ================");
        if (logs == null || logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        System.out.printf("%-5s | %-8s | %-25s | %-45s | %-20s%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        for (ActivityLog log : logs) {
            System.out.printf("%-5d | %-8d | %-25s | %-45s | %-20s%n",
                    log.id(),
                    log.userId(),
                    shorten(log.actionType(), 25),
                    shorten(log.description(), 45),
                    shorten(log.timestamp(), 20)
            );
        }
    }

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
            System.out.print("Enter new role (STUDENT, TEACHER, ADMIN, NURSE, CLINIC STAFF, REGISTRAR, SCHOOL FACULTY STAFF) or 0 to cancel: ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0") || role.equals("CANCEL")) {
                return null;
            }
            if (role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN") ||
                    role.equals("NURSE") || role.equals("CLINIC STAFF") || role.equals("REGISTRAR") ||
                    role.equals("SCHOOL FACULTY STAFF")) {
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