package com.yd.controller;

import com.yd.dao.UserDAO;
import com.yd.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ForgotPasswordController {

    @FXML
    private TextField idField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField birthdayField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label messageLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    void handleVerifyUser(ActionEvent event) {
        String id = idField.getText();
        String email = emailField.getText();
        String birthdayStr = birthdayField.getText();
        String phone = phoneField.getText();

        if (id.isEmpty() || email.isEmpty() || birthdayStr.isEmpty() || phone.isEmpty()) {
            messageLabel.setText("모든 필드를 입력해주세요.");
            return;
        }

        LocalDate birthday;
        try {
            birthday = LocalDate.parse(birthdayStr);
        } catch (DateTimeParseException e) {
            messageLabel.setText("생년월일 형식이 올바르지 않습니다.");
            return;
        }

        boolean isUserValid = userDAO.verifyUserInfo(id, email, birthday, phone);
        if (isUserValid) {
            // 비밀번호 재설정 화면으로 이동
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/reset_password.fxml"));
                Stage stage = (Stage) idField.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("비밀번호 재설정");

                // ResetPasswordController에 사용자 ID 전달
                ResetPasswordController controller = loader.getController();
                controller.setUserId(id);

            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("비밀번호 재설정 화면을 로드하는 중 오류가 발생했습니다.");
            }
        } else {
            messageLabel.setText("입력하신 정보와 일치하는 사용자가 없습니다.");
        }
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) idField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("로그인");
            stage.setWidth(400);
            stage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("로그인 화면을 로드하는 중 오류가 발생했습니다.");
        }
    }
}