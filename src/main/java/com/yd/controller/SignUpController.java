package com.yd.controller;


import com.yd.dao.UserDAO;
import com.yd.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class SignUpController {

    @FXML
    private TextField idField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField emailField;

    @FXML
    private DatePicker birthdayPicker;

    @FXML
    private TextField phoneField;

    @FXML
    private Label messageLabel;

    @FXML
    private ImageView logoImageView; // 로고 ImageView 추가

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // 로고 이미지 로드
        Image logo = new Image(getClass().getResourceAsStream("/images/twitter_logo.png"));
        logoImageView.setImage(logo);
    }

    @FXML
    void handleSignUp(ActionEvent event) {
        String id = idField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        LocalDate birthday = birthdayPicker.getValue();
        String phone = phoneField.getText();

        if (id.isEmpty() || password.isEmpty() || email.isEmpty() || birthday == null || phone.isEmpty()) {
            messageLabel.setText("모든 빈칸을 입력하세요.");
            return;
        }

        // 이메일 형식 검증
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            messageLabel.setText("유효한 이메일 주소를 입력해주세요.");
            return;
        }

        // 비밀번호 강도 검증 (예시: 최소 6자)
        if (password.length() < 6) {
            messageLabel.setText("비밀번호는 최소 6자 이상이어야 합니다.");
            return;
        }

        User user = new User(id, password, email, birthday, phone);
        boolean success = userDAO.registerUser(user);
        if (success) {
            messageLabel.setText("회원가입이 완료되었습니다.");
            // 로그인 화면으로 이동
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Stage stage = (Stage) idField.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Twitter");
                stage.setWidth(800);
                stage.setHeight(600);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            messageLabel.setText("회원가입에 실패했습니다. 다시 시도해주세요.");
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) idField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Twitter");
            stage.setWidth(800);
            stage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}