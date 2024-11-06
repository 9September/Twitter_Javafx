package com.yd.controller;

import com.yd.dao.UserDAO;
import com.yd.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class MyPageController {

    @FXML
    private Label idLabel;

    @FXML
    private TextField emailField;

    @FXML
    private Label birthdayLabel;

    @FXML
    private TextField phoneField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button backButton;

    @FXML
    private ImageView logoImageView; // 로고 ImageView 추가

    private UserDAO userDAO = new UserDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
        }
    }

    // 사용자 정보 로드
    private void loadUserInfo() {
        idLabel.setText(currentUser.getId());
        emailField.setText(currentUser.getEmail());
        birthdayLabel.setText(currentUser.getBirthday().toString());
        phoneField.setText(currentUser.getPhoneNumber());
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        String newEmail = emailField.getText();
        String newPhone = phoneField.getText();

        boolean success = userDAO.updateUser(currentUser.getId(), newEmail, newPhone);
        if (success) {
            messageLabel.setText("정보가 업데이트되었습니다.");
            // 업데이트된 정보를 currentUser에 반영
            currentUser.setEmail(newEmail);
            currentUser.setPhoneNumber(newPhone);
        } else {
            messageLabel.setText("정보 업데이트에 실패했습니다.");
        }
    }

    @FXML
    void goToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Twitter");
            stage.setWidth(800);
            stage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        LoginController.setCurrentUser(null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) idLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Twitter");
            stage.setWidth(800);
            stage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}