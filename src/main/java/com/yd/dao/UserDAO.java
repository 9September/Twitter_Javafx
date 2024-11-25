package com.yd.dao;

import com.yd.model.User;
import com.yd.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;

public class UserDAO {

    // 사용자 등록
    public boolean registerUser(User user) {
        String sql = "INSERT INTO USERS (ID, PASSWORD, EMAIL, BIRTHDAY, PHONE_NUMBER) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            stmt.setString(5, user.getPhoneNumber());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // 예외 로그 출력
            e.printStackTrace();
            return false;
        }
    }

    // 사용자 로그인
    public User loginUser(String id, String password) {
        String sql = "SELECT * FROM USERS WHERE ID = ? AND PASSWORD = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("ID"),
                        rs.getString("PASSWORD"),
                        rs.getString("EMAIL"),
                        rs.getTimestamp("CREATED_AT").toLocalDateTime(),
                        rs.getDate("BIRTHDAY").toLocalDate(),
                        rs.getString("PHONE_NUMBER"),
                        rs.getBytes("profile_image")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ID로 사용자 정보 조회
    public User getUserById(String id) {
        String sql = "SELECT * FROM USERS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("ID"),
                        rs.getString("PASSWORD"),
                        rs.getString("EMAIL"),
                        rs.getTimestamp("CREATED_AT").toLocalDateTime(),
                        rs.getDate("BIRTHDAY").toLocalDate(),
                        rs.getString("PHONE_NUMBER"),
                        rs.getBytes("profile_image")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 사용자 정보 업데이트 (재추가된 메서드)
    public boolean updateUser(String id, String email, String phoneNumber, LocalDate birthday) {
        String sql = "UPDATE USERS SET EMAIL = ?, PHONE_NUMBER = ?, BIRTHDAY = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, phoneNumber);
            if (birthday != null) {
                stmt.setDate(3, Date.valueOf(birthday));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            stmt.setString(4, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // 예외 로그 출력
            e.printStackTrace();
            return false;
        }
    }

    // 사용자 프로필 이미지 업데이트 메서드 추가
    public boolean updateUserProfileImage(String id, byte[] imageBytes) {
        String sql = "UPDATE USERS SET profile_image = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (imageBytes != null) {
                stmt.setBytes(1, imageBytes);
            } else {
                stmt.setNull(1, java.sql.Types.BLOB);
            }
            stmt.setString(2, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // 예외 로그 출력
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyUserInfo(String id, String email, LocalDate birthday, String phoneNumber) {
        String sql = "SELECT * FROM USERS WHERE ID = ? AND EMAIL = ? AND BIRTHDAY = ? AND PHONE_NUMBER = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, email);
            stmt.setDate(3, Date.valueOf(birthday));
            stmt.setString(4, phoneNumber);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String userId, String newPassword) {
        String sql = "UPDATE USERS SET PASSWORD = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] getUserProfileImage(String userId) {
        String sql = "SELECT profile_image FROM USERS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("profile_image");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 기본 이미지 사용 시 null 반환
    }

    public int getFollowerCount(String userId) {
        String sql = "SELECT COUNT(*) AS follower_count FROM FOLLOW WHERE FOLLOWER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("follower_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getFollowingCount(String userId) {
        String sql = "SELECT COUNT(*) AS following_count FROM FOLLOW WHERE FOLLOWING_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("following_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
