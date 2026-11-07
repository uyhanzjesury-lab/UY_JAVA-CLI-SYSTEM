package com.scc.dao;

import com.scc.model.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Userdao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public Userdao() {
        DatabaseInitializer.initialize();
        try (Connection conn = ClinicdatabaseConnection.getConnection()) {
            defaultAdminGenerator(conn);
        } catch (SQLException e) {
            System.err.println("Error creating default admin: " + e.getMessage());
        }
    }

    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE identity = ?";
        String insertSql = "INSERT INTO users(username, password, identity, status) VALUES(?, ?, ?, ?)";

        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, "ADMIN");
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                        insert.setString(1, "admin");
                        insert.setString(2, "admin123");
                        insert.setString(3, "ADMIN");
                        insert.setString(4, "APPROVED");
                        insert.executeUpdate();
                    }
                    System.out.println("-> Default Admin created (Username: admin | Password: admin123)");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password, String identity) {
        String sql = "INSERT INTO users(username, password, identity, status) VALUES(?, ?, ?, 'PENDING')";

        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password);
            stmt.setString(3, identity.trim().toUpperCase());
            stmt.executeUpdate();

            System.out.println("Registration successful. Your account is PENDING admin approval.");
            return true;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                System.out.println("That username is already registered. Please choose another.");
            } else {
                System.err.println("Registration failed: " + e.getMessage());
            }
            return false;
        }
    }

    public user login(String username, String password) {
        String sql = "SELECT id, username, password, identity, status FROM users WHERE username = ? AND password = ?";
        user loggedInUser = null;

        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Invalid username or password.");
                    return null;
                }

                String status = rs.getString("status");
                if (!"APPROVED".equalsIgnoreCase(status) && !"ACTIVE".equalsIgnoreCase(status)) {
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println("Your account is pending admin approval.");
                    } else {
                        System.out.println("Your account is not active. Please contact the administrator.");
                    }
                    return null;
                }

                loggedInUser = new user(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("identity"),
                        status
                );
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            return null;
        }

        if (loggedInUser != null) {
            // Dynamically determine Action Type based on the user's identity (e.g., LOGIN_ADMIN, LOGIN_STUDENT)
            String actionType = "LOGIN_" + loggedInUser.identity().trim().toUpperCase().replace(" ", "_");
            String description = "User " + loggedInUser.username() + " (" + loggedInUser.identity() + " - ID: " + loggedInUser.id() + ") logged in.";
            logDao.logAction(loggedInUser.id(), actionType, description);
        }
        return loggedInUser;
    }

    public List<user> getAllUsers() {
        return getUsers("SELECT id, username, password, identity, status FROM users ORDER BY id");
    }

    public List<user> getPendingUsers() {
        return getUsers("SELECT id, username, password, identity, status FROM users WHERE UPPER(status) = 'PENDING' ORDER BY id");
    }

    private List<user> getUsers(String sql) {
        List<user> users = new ArrayList<>();
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new user(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("identity"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public boolean approveUser(int userId, int adminId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ? AND UPPER(status) = 'PENDING'";
        int rowsUpdated = 0;

        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            rowsUpdated = stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }

        if (rowsUpdated > 0) {
            System.out.println("User ID " + userId + " has been approved.");
            logDao.logAction(adminId, "USER_APPROVED", "Admin ID " + adminId + " approved account for User ID " + userId);
            return true;
        }

        System.out.println("No pending user found with ID " + userId + ". Nothing was changed.");
        return false;
    }

    public boolean addUserByAdmin(String username, String password, String identity, String status, int adminId) {
        String sql = "INSERT INTO users(username, password, identity, status) VALUES(?, ?, ?, ?)";

        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password);
            stmt.setString(3, identity.trim().toUpperCase());
            stmt.setString(4, status.trim().toUpperCase());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not add user: " + e.getMessage());
            return false;
        }

        logDao.logAction(adminId, "USER_ADDED", "Admin ID " + adminId + " created new user " + username + " with role " + identity);
        return true;
    }

    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus, int adminId) {
        String sql = "UPDATE users SET identity = ?, status = ? WHERE id = ?";
        int rowsUpdated = 0;

        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole.trim().toUpperCase());
            stmt.setString(2, newStatus.trim().toUpperCase());
            stmt.setInt(3, userId);

            rowsUpdated = stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }

        if (rowsUpdated > 0) {
            System.out.println("User ID " + userId + " updated successfully.");
            logDao.logAction(adminId, "USER_UPDATED", "Admin ID " + adminId + " changed role of User " + userId + " to " + newRole + " (Status: " + newStatus + ")");
            return true;
        } else {
            System.out.println("User ID not found.");
            return false;
        }
    }

    public boolean deleteUser(int userId, int adminId) {
        String sql = "DELETE FROM users WHERE id = ? AND UPPER(identity) <> 'ADMIN'";
        boolean deleted = false;

        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            deleted = stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Could not delete user: " + e.getMessage());
            return false;
        }

        if (deleted) {
            logDao.logAction(adminId, "USER_DELETED", "Admin ID " + adminId + " deleted User ID " + userId);
        }
        return deleted;
    }

    public boolean logClinicConsultation(int userId, String patientName, String diagnosis) {
        String description = "Consultation recorded for patient " + patientName + " - Diagnosis: " + diagnosis;
        System.out.println("-> " + description);
        return logDao.logAction(userId, "CONSULTATION_LOGGED", description);
    }
}