package com.yd.dao;

import com.yd.model.Post;
import com.yd.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public List<Post> searchPosts(String query, int offset, int limit) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM POSTS p " +
                "LEFT JOIN POST_HASHTAGS ph ON p.POST_ID = ph.POST_ID " +
                "LEFT JOIN HASHTAGS h ON ph.HASHTAG_ID = h.HASHTAG_ID " +
                "WHERE p.WRITER_ID LIKE ? " +
                "OR p.TEXT LIKE ? " +
                "OR h.TAG_NAME LIKE ? " +
                "ORDER BY p.CREATED_AT DESC LIMIT ? OFFSET ?";
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            String wildcardQuery = "%" + query + "%";
            stmt.setString(1, wildcardQuery); // 사용자 ID
            stmt.setString(2, wildcardQuery); // 포스트 내용
            if (query.startsWith("#")) {
                stmt.setString(3, query.substring(1)); // 해시태그 검색 시 '#' 제거
            } else {
                stmt.setString(3, "%" + query + "%"); // 일반적인 해시태그 검색
            }
            stmt.setInt(4, limit);
            stmt.setInt(5, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Post post = mapResultSetToPost(rs);
                posts.add(post);
            }
        }
        return posts;
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setPostId(rs.getInt("post_id"));
        post.setWriterId(rs.getString("writer_id"));
        post.setText(rs.getString("text"));
        post.setImage(rs.getBytes("image"));
        post.setNumOfLikes(rs.getInt("num_of_likes"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return post;
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

    public boolean addPostWithHashtags(String text, byte[] imageData, String writerId) throws SQLException {
        Connection conn = null;
        PreparedStatement postStmt = null;
        PreparedStatement selectHashtagStmt = null;
        PreparedStatement insertHashtagStmt = null;
        PreparedStatement insertPostHashtagStmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 포스트 추가
            String postSql = "INSERT INTO POSTS (TEXT, IMAGE, WRITER_ID) VALUES (?, ?, ?)";
            postStmt = conn.prepareStatement(postSql, Statement.RETURN_GENERATED_KEYS);
            postStmt.setString(1, text);
            if (imageData != null) {
                postStmt.setBytes(2, imageData);
            } else {
                postStmt.setNull(2, Types.BLOB);
            }
            postStmt.setString(3, writerId);
            int affectedRows = postStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("포스트 추가 실패, 영향 받은 행 없음.");
            }

            rs = postStmt.getGeneratedKeys();
            if (rs.next()) {
                int postId = rs.getInt(1);

                // 2. 해시태그 추출
                Set<String> hashtags = extractHashtags(text);

                // 3. 해시태그 처리
                String selectHashtagSql = "SELECT HASHTAG_ID FROM HASHTAGS WHERE TAG_NAME = ?";
                selectHashtagStmt = conn.prepareStatement(selectHashtagSql);

                String insertHashtagSql = "INSERT INTO HASHTAGS (TAG_NAME) VALUES (?)";
                insertHashtagStmt = conn.prepareStatement(insertHashtagSql, Statement.RETURN_GENERATED_KEYS);

                String insertPostHashtagSql = "INSERT INTO POST_HASHTAGS (POST_ID, HASHTAG_ID) VALUES (?, ?)";
                insertPostHashtagStmt = conn.prepareStatement(insertPostHashtagSql);

                for (String tag : hashtags) {
                    int hashtagId = -1;

                    // 해시태그 존재 여부 확인
                    selectHashtagStmt.setString(1, tag);
                    ResultSet hashtagRs = selectHashtagStmt.executeQuery();
                    if (hashtagRs.next()) {
                        hashtagId = hashtagRs.getInt("HASHTAG_ID");
                    } else {
                        // 해시태그 추가
                        insertHashtagStmt.setString(1, tag);
                        int insertCount = insertHashtagStmt.executeUpdate();
                        if (insertCount == 0) {
                            throw new SQLException("해시태그 추가 실패: " + tag);
                        }
                        ResultSet generatedHashtagRs = insertHashtagStmt.getGeneratedKeys();
                        if (generatedHashtagRs.next()) {
                            hashtagId = generatedHashtagRs.getInt(1);
                        } else {
                            throw new SQLException("해시태그 ID 획득 실패: " + tag);
                        }
                    }

                    // POST_HASHTAGS 삽입
                    insertPostHashtagStmt.setInt(1, postId);
                    insertPostHashtagStmt.setInt(2, hashtagId);
                    insertPostHashtagStmt.executeUpdate();
                }

                // 트랜잭션 커밋
                conn.commit();
                return true;
            } else {
                throw new SQLException("포스트 ID 획득 실패.");
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // 롤백
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw e;
        } finally {
            // 리소스 정리
            if (rs != null) rs.close();
            if (postStmt != null) postStmt.close();
            if (selectHashtagStmt != null) selectHashtagStmt.close();
            if (insertHashtagStmt != null) insertHashtagStmt.close();
            if (insertPostHashtagStmt != null) insertPostHashtagStmt.close();
            if (conn != null) conn.setAutoCommit(true); // 오토커밋 복원
        }
    }

    /**
     * 텍스트에서 해시태그를 추출하는 메서드
     *
     * @param text 포스트 내용
     * @return 해시태그 Set (소문자)
     */
    private Set<String> extractHashtags(String text) {
        Set<String> hashtags = new HashSet<>();
        Matcher matcher = Pattern.compile("#(\\w+)").matcher(text);
        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase()); // 소문자로 통일
        }
        return hashtags;
    }

    public void addPostHashtag(int postId, int hashtagId, Connection conn) throws SQLException {
        String sql = "INSERT INTO POST_HASHTAGS (POST_ID, HASHTAG_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, hashtagId);
            stmt.executeUpdate();
        }
    }
}
