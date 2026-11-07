package com.scc.dao;

import com.scc.model.ActivityLog;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public boolean logAction(int userId, String actionType, String description) {
        String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String sql = "INSERT INTO activity_logs (user_id, action_type, description, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType.trim().toUpperCase());
            pstmt.setString(3, description.trim());
            pstmt.setString(4, currentTimestamp);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error recording audit log: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM activity_logs ORDER BY timestamp DESC";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("action_type"),
                        rs.getString("description"),
                        rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching activity logs: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ActivityLog(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs for user ID " + userId + ": " + e.getMessage());
        }
        return logs;
    }

    // New method to clear all audit logs from the database
    public boolean clearAllLogs() {
        String sql = "DELETE FROM activity_logs";
        try (Connection conn = ClinicdatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Error clearing activity logs: " + e.getMessage());
            return false;
        }
    }
}