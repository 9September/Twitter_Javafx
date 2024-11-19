package com.yd.dao;

import java.sql.*;

public class HashtagDAO {
    private Connection connection;

    public HashtagDAO() {
        // 데이터베이스 연결 설정
    }

    public int getOrCreateHashtag(String tag, Connection connection) throws SQLException {
        String selectSQL = "SELECT HASHTAG_ID FROM HASHTAGS WHERE TAG_NAME = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
            selectStmt.setString(1, tag);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("HASHTAG_ID");
            }
        }

        String insertSQL = "INSERT INTO HASHTAGS (TAG_NAME) VALUES (?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, tag);
            insertStmt.executeUpdate();
            ResultSet rs = insertStmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("해시태그 삽입 실패: " + tag);
            }
        }
    }
}