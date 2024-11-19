package com.yd.dao;

import com.yd.model.Post;
import com.yd.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    // 포스트 추가
    public boolean addPost(String text, byte[] image, String writerId) {
        String sql = "INSERT INTO POSTS (TEXT, IMAGE, WRITER_ID) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, text);
            stmt.setBytes(2, image);
            stmt.setString(3, writerId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 포스트 좋아요
    public boolean likePost(int postId, String userId) {
        String sql = "INSERT INTO POST_LIKE (POST_ID, USER_ID) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // 이미 좋아요를 누른 경우 예외가 발생할 수 있음
            e.printStackTrace();
            return false;
        }
    }

    // 포스트 좋아요 취소
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

    // 특정 사용자가 좋아요 한 포스트인지 확인
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

    // 현재 사용자와 팔로잉하는 사용자의 포스트 가져오기
    public List<Post> getPostsByUserId(String userId) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM POSTS WHERE WRITER_ID = ? ORDER BY CREATED_AT DESC";
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
                posts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public int getPostCountByUserId(String userId) {
        String sql = "SELECT COUNT(*) AS post_count FROM POSTS WHERE WRITER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("post_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Post getPostById(int postId) {
        String sql = "SELECT * FROM posts WHERE POST_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Post(
                        rs.getInt("POST_ID"),
                        rs.getString("TEXT"),
                        rs.getBytes("IMAGE"),
                        rs.getString("WRITER_ID"),
                        rs.getTimestamp("CREATED_AT").toLocalDateTime(),
                        rs.getInt("NUM_OF_LIKES"),
                        rs.getInt("NUM_OF_RETWEETS")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Post> getAllPosts(int offset, int limit) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM POSTS ORDER BY CREATED_AT DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
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
                posts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
}
