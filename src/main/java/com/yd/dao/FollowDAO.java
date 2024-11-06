package com.yd.dao;

import com.yd.model.Follow;
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

    // 특정 사용자를 팔로우하고 있는지 확인
    public boolean isFollowing(String followingId, String followerId) {
        String sql = "SELECT * FROM FOLLOW WHERE FOLLOWING_ID = ? AND FOLLOWER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, followingId);
            stmt.setString(2, followerId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 팔로워 목록 가져오기
    public List<Follow> getFollowers(String userId) {
        List<Follow> followers = new ArrayList<>();
        String sql = "SELECT * FROM FOLLOW WHERE FOLLOWING_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Follow follow = new Follow(
                        rs.getString("FOLLOWING_ID"),
                        rs.getString("FOLLOWER_ID"),
                        rs.getTimestamp("CREATE_AT").toLocalDateTime()
                );
                followers.add(follow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return followers;
    }

    // 팔로잉 목록 가져오기
    public List<Follow> getFollowing(String userId) {
        List<Follow> following = new ArrayList<>();
        String sql = "SELECT * FROM FOLLOW WHERE FOLLOWER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Follow follow = new Follow(
                        rs.getString("FOLLOWING_ID"),
                        rs.getString("FOLLOWER_ID"),
                        rs.getTimestamp("CREATE_AT").toLocalDateTime()
                );
                following.add(follow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return following;
    }

    // 팔로워 ID 목록 가져오기
    public List<String> getFollowersIds(String userId) {
        List<String> followers = new ArrayList<>();
        String sql = "SELECT FOLLOWER_ID FROM FOLLOW WHERE FOLLOWING_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                followers.add(rs.getString("FOLLOWER_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return followers;
    }

    // 팔로잉 ID 목록 가져오기
    public List<String> getFollowingIds(String userId) {
        List<String> following = new ArrayList<>();
        String sql = "SELECT FOLLOWING_ID FROM FOLLOW WHERE FOLLOWER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                following.add(rs.getString("FOLLOWING_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return following;
    }

    // 팔로우 추천 기능
    public List<String> getRecommendUsers(String userId) {
        List<String> recommendUsers = new ArrayList<>();
        String sql = "SELECT ID FROM USERS WHERE ID != ? AND ID NOT IN (SELECT FOLLOWING_ID FROM FOLLOW WHERE FOLLOWER_ID = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recommendUsers.add(rs.getString("ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recommendUsers;
    }
}
