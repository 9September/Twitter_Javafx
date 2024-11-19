package com.yd.dao;

import com.yd.model.Post;
import com.yd.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LikeDAO {

    public List<Post> getLikedPostsByUserId(String userId) {
        List<Post> likedPosts = new ArrayList<>();
        String sql = "SELECT P.* FROM POST_LIKE PL JOIN POSTS P ON PL.POST_ID = P.POST_ID WHERE PL.USER_ID = ? ORDER BY P.CREATED_AT DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Post post = new Post(
                        rs.getInt("POST_ID"),
                        rs.getString("TEXT"),
                        rs.getBytes("IMAGE"),
                        rs.getString("WRITER_ID"),
                        rs.getTimestamp("CREATED_AT").toLocalDateTime(),
                        rs.getInt("NUM_OF_LIKES"),
                        rs.getInt("NUM_OF_RETWEETS")
                );
                likedPosts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return likedPosts;
    }

    public int getTotalLikesByUserId(String userId) {
        String sql = "SELECT SUM(NUM_OF_LIKES) AS total_likes FROM POSTS WHERE WRITER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_likes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 좋아요 체크
    public boolean isPostLiked(int postId, String userId) {
        String sql = "SELECT * FROM POST_LIKE WHERE POST_ID = ? AND USER_ID = ?";
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

    // 좋아요 추가
    public boolean likePost(int postId, String userId) {
        String sql = "INSERT INTO POST_LIKE (POST_ID, USER_ID) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // 이미 좋아요한 경우 예외 발생 가능
            e.printStackTrace();
            return false;
        }
    }

    // 좋아요 취소
    public boolean unlikePost(int postId, String userId) {
        String sql = "DELETE FROM POST_LIKE WHERE POST_ID = ? AND USER_ID = ?";
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

}
