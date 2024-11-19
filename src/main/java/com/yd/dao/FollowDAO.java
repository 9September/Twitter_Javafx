package com.yd.dao;

import com.yd.model.Follow;
import com.yd.model.User;
import com.yd.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FollowDAO {

    // 사용자 팔로우
    public boolean followUser(String followingId, String followerId) {
        String sql = "INSERT INTO FOLLOW (FOLLOWING_ID, FOLLOWER_ID) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, followingId);
            stmt.setString(2, followerId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // 이미 팔로우한 경우 예외가 발생할 수 있음
            e.printStackTrace();
            return false;
        }
    }

    // 사용자 언팔로우
    public boolean unfollowUser(String followingId, String followerId) {
        String sql = "DELETE FROM FOLLOW WHERE FOLLOWING_ID = ? AND FOLLOWER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, followingId);
            stmt.setString(2, followerId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getFollowingUsers(String userId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT U.* FROM USERS U JOIN FOLLOW F ON U.ID = F.FOLLOWING_ID WHERE F.FOLLOWER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("ID"));
                user.setProfileImage(rs.getBytes("PROFILE_IMAGE"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> getRecommendUsers(String userId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT U.* FROM USERS U WHERE U.ID NOT IN (SELECT FOLLOWING_ID FROM FOLLOW WHERE FOLLOWER_ID = ?) AND U.ID <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("ID"));
                user.setProfileImage(rs.getBytes("PROFILE_IMAGE"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
