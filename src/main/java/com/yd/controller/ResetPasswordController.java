package com.yd.controller;

import com.yd.dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetPasswordController {

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private UserDAO userDAO = new UserDAO();
    private String userId;

    // 사용자 ID를 설정하는 메서드
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("모든 필드를 입력해주세요.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            messageLabel.setText("비밀번호가 일치하지 않습니다.");
            return;
        }

        boolean updated = userDAO.updatePassword(userId, newPassword);
        if (updated) {
            messageLabel.setText("비밀번호가 성공적으로 변경되었습니다.");

            // 로그인 화면으로 이동
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Stage stage = (Stage) newPasswordField.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("로그인");
                stage.setWidth(400);
                stage.setHeight(600);
            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("로그인 화면으로 돌아가는 중 오류가 발생했습니다.");
            }
        } else {
            messageLabel.setText("비밀번호 변경 중 오류가 발생했습니다.");
        }
    }
}