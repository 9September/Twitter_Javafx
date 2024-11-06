package com.yd.dao;

import com.yd.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RetweetDAO {

    // 리트윗 추가
    public boolean addRetweet(int postId, String userId) {
        String sql = "INSERT INTO RETWEETS (POST_ID, USER_ID) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // 이미 리트윗한 경우 예외가 발생할 수 있음
            e.printStackTrace();
            return false;
        }
    }

    // 리트윗 취소
    public boolean removeRetweet(int postId, String userId) {
        String sql = "DELETE FROM RETWEETS WHERE POST_ID = ? AND USER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 사용자가 특정 포스트를 리트윗했는지 확인
    public boolean isRetweeted(int postId, String userId) {
        String sql = "SELECT * FROM RETWEETS WHERE POST_ID = ? AND USER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}