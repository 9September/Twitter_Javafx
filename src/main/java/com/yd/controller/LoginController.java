package com.yd.controller;

import com.yd.dao.UserDAO;
import com.yd.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField idField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private ImageView logoImageView; // 로고 ImageView 추가

    private UserDAO userDAO = new UserDAO();

    // 현재 로그인한 사용자를 저장하는 변수
    private static User currentUser = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    @FXML
    public void initialize() {
        // 로고 이미지 로드
        Image logo = new Image(getClass().getResourceAsStream("/images/twitter_logo.png"));
        logoImageView.setImage(logo);
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String id = idField.getText();
        String password = passwordField.getText();
        if (id.isEmpty() || password.isEmpty()) {
            messageLabel.setText("ID와 Password를 입력하세요.");
            return;
        }

        User user = userDAO.loginUser(id, password);
        if (user != null) {
            currentUser = user;
            // 메인 화면으로 전환
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
                Stage stage = (Stage) idField.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Twitter");
                stage.setWidth(800);
                stage.setHeight(600);
            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("메인 화면을 로드하는 중 오류가 발생했습니다.");
            }
        } else {
            messageLabel.setText("잘못된 ID 또는 Password입니다.");
        }
    }

    @FXML
    void goToSignUp(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
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