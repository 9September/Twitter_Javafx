package com.yd.controller;

import com.yd.dao.UserDAO;
import com.yd.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
    private Label headerLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private ImageView twitterImage;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button uploadProfileImageButton;

    @FXML
    public void initialize() {
        currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
        }
        Image profileImage = getImageFromBytes(currentUser.getProfileImage());
        profileImageView.setImage(profileImage);
    }

    public void setUser(User user) {
        this.currentUser = user;
        usernameLabel.setText("@" + user.getId());

        // 프로필 이미지를 데이터베이스에서 불러와 설정
        Image profileImage = getImageFromBytes(user.getProfileImage());
        profileImageView.setImage(profileImage);
    }

    private Image getImageFromBytes(byte[] imageBytes) {
        if (imageBytes != null && imageBytes.length > 0) {
            return new Image(new ByteArrayInputStream(imageBytes));
        } else {
            // 기본 이미지 로드
            return new Image(getClass().getResourceAsStream("/images/default_profile.png"));
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
    private void handleProfileImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("프로필 사진 업로드");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("이미지 파일", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selectedFile != null) {
            // 파일 크기 제한 (예: 2MB)
            if (selectedFile.length() > 2 * 1024 * 1024) { // 2MB
                Alert alert = new Alert(Alert.AlertType.WARNING, "이미지 파일 크기가 너무 큽니다. 2MB 이하의 파일을 선택해주세요.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            try {
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());

                // 데이터베이스에 프로필 이미지 업데이트
                boolean success = userDAO.updateUserProfileImage(currentUser.getId(), imageBytes);
                if (success) {
                    // 현재 사용자 객체 업데이트
                    currentUser.setProfileImage(imageBytes);

                    // ImageView 업데이트
                    Image profileImage = getImageFromBytes(imageBytes);
                    profileImageView.setImage(profileImage);

                    // 메인 페이지의 프로필 이미지도 업데이트하려면, 메인 페이지 컨트롤러에 접근하거나 창을 다시 로드해야 합니다.

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "프로필 사진이 업데이트되었습니다.", ButtonType.OK);
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "프로필 사진을 업데이트하지 못했습니다.", ButtonType.OK);
                    alert.showAndWait();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "이미지 파일을 읽는 중 오류가 발생했습니다.", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void goToMainPage(MouseEvent event) {
        try {
            // 현재 창을 닫고 메인 창을 다시 로드하는 방식
            Stage currentStage = (Stage) twitterImage.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();

            // 필요시 컨트롤러에 사용자 정보 전달
            MainController mainController = loader.getController();
            mainController.setUser(currentUser);

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Twitter - Main");
            currentStage.setWidth(800);
            currentStage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "메인 페이지 로드 중 오류가 발생했습니다.", ButtonType.OK);
            alert.showAndWait();
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
            stage.setWidth(400);
            stage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}