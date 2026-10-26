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

                return new user(
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
    }

    public List<user> getAllUsers() {
        return getUsers("SELECT id, username, password, identity, status FROM users ORDER BY id");
    }

    public List<user> getalltheuser() {
        return getAllUsers();
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

    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ? AND UPPER(status) = 'PENDING'";

        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("User ID " + userId + " has been approved.");
                return true;
            }

            System.out.println("No pending user found with ID " + userId + ". Nothing was changed.");
            return false;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    public boolean addUserByAdmin(String username, String password, String identity, String status) {
        String sql = "INSERT INTO users(username, password, identity, status) VALUES(?, ?, ?, ?)";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password);
            stmt.setString(3, identity.trim().toUpperCase());
            stmt.setString(4, status.trim().toUpperCase());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Could not add user: " + e.getMessage());
            return false;
        }
    }

    // Required update method using PreparedStatement without string concatenation
    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus) {
        String sql = "UPDATE users SET identity = ?, status = ? WHERE id = ?";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole.trim().toUpperCase());
            stmt.setString(2, newStatus.trim().toUpperCase());
            stmt.setInt(3, userId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("User ID " + userId + " updated successfully.");
                return true;
            } else {
                System.out.println("User ID not found.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.trim().toUpperCase());
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Could not update user status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ? AND UPPER(identity) <> 'ADMIN'";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Could not delete user: " + e.getMessage());
            return false;
        }
    }
}