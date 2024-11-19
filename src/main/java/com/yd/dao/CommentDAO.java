package com.yd.dao;

import com.yd.model.Comment;
import com.yd.model.Post;
import com.yd.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private PostDAO postDAO = new PostDAO();

    // 댓글 추가
    public boolean addComment(String text, String writerId, int postId) {
        String sql = "INSERT INTO COMMENTS (TEXT, WRITER_ID, POST_ID) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, text);
            stmt.setString(2, writerId);
            stmt.setInt(3, postId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 특정 포스트의 댓글 가져오기
    public List<Comment> getCommentsByPostId(int postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM COMMENTS WHERE POST_ID = ? ORDER BY CREATED_AT DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment(
                        rs.getInt("COMMENT_ID"),
                        rs.getString("TEXT"),
                        rs.getString("WRITER_ID"),
                        rs.getInt("POST_ID"),
                        rs.getInt("NUM_OF_LIKES"),
                        rs.getTimestamp("CREATED_AT").toLocalDateTime()
                );
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public List<Comment> getCommentsByUserId(String userId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.writer_id, c.text, c.post_id, c.num_of_likes, " +
                "p.text AS post_text, p.writer_id AS post_writer_id, p.image AS post_image, " +
                "p.created_at AS post_created_at, p.num_of_likes AS post_num_of_likes, " +
                "p.num_of_retweets AS post_num_of_retweets " +
                "FROM comments c " +
                "JOIN posts p ON c.post_id = p.post_id " +
                "WHERE c.writer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment(
                            rs.getInt("comment_id"),
                            rs.getString("text"),
                            rs.getString("writer_id"),
                            rs.getInt("post_id"),
                            rs.getInt("num_of_likes"),
                            rs.getTimestamp("post_created_at").toLocalDateTime()
                    );

                    // 포스트 정보 설정
                    Post post = new Post(
                            rs.getInt("post_id"),
                            rs.getString("post_writer_id"),
                            rs.getBytes("post_image"),
                            rs.getString("post_text"),
                            null,
                            0,
                            0
                    );
                    comment.setPost(post);

                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // 댓글 좋아요
    public boolean likeComment(int commentId, String userId) {
        String sql = "INSERT INTO COMMENTS_LIKE (COMMENT_ID, USER_ID) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // 이미 좋아요를 누른 경우 예외가 발생할 수 있음
            e.printStackTrace();
            return false;
        }
    }

    // 댓글 좋아요 취소
    public boolean unlikeComment(int commentId, String userId) {
        String sql = "DELETE FROM COMMENTS_LIKE WHERE COMMENT_ID = ? AND USER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 사용자가 특정 댓글을 좋아요 했는지 확인
    public boolean isCommentLiked(int commentId, String userId) {
        String sql = "SELECT * FROM COMMENTS_LIKE WHERE COMMENT_ID = ? AND USER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
