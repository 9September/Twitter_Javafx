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

}
