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
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            defaultAdminGenerator(conn);
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE identity = 'ADMIN'";
        String insertSql = "INSERT INTO users(username, password, identity, status) VALUES('admin', 'admin123', 'ADMIN', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("-> Default Admin created (Username: admin | Password: admin123)");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public static boolean registerUser(String username, String password, String identity) {
        String sql = "INSERT INTO users(username, password, identity, status) VALUES(?, ?, ?, ?)";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, username);
            pstm.setString(2, password);
            pstm.setString(3, identity.toUpperCase());
            pstm.setString(4, "PENDING"); // Set default status as PENDING or ACTIVE
            pstm.executeUpdate();

            System.out.println("User registered successfully in the database!");
            return true;

        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    public static user login(String username, String password) {
        String sql = "SELECT id, username, password, identity, status FROM users WHERE username = ? AND password = ?";

        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(" -> Account is pending, waiting for Approval");
                        return null;
                    }
                    return new user(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("identity"),
                            rs.getString("status")
                    );
                } else {
                    System.out.println(" -> Invalid username or password");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }

    public List<user> getalltheuser() {
        List<user> userList = new ArrayList<>();
        String sql = "SELECT id, username, password, identity, status " +
                "FROM users ";

        try (Connection con = ClinicdatabaseConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userList .add (new user(
                 rs.getInt("id"),
                 rs.getString("username"),
                 rs.getString("password"),
                 rs.getString("identity"),
                 rs.getString("status")
                ));


            }
        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
        return userList;
    }



}