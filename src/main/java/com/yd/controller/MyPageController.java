package com.yd.controller;

import com.yd.dao.CommentDAO;
import com.yd.dao.LikeDAO;
import com.yd.dao.PostDAO;
import com.yd.dao.UserDAO;
import com.yd.model.Comment;
import com.yd.model.Post;
import com.yd.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.LocalDate;
import java.util.List;

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
    private DatePicker birthdayPicker;
    @FXML
    private Button backButton;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Label headerLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private ImageView centralProfileImageView;
    @FXML
    private ImageView twitterImage;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button uploadProfileImageButton;
    @FXML
    private TabPane myTabPane;
    @FXML
    private ListView<Post> myPostsListView;
    @FXML
    private ListView<Comment> myCommentsListView;
    @FXML
    private ListView<Post> likedPostsListView;
    @FXML
    private Label postCountLabel;
    @FXML
    private Label followerCountLabel;
    @FXML
    private Label followingCountLabel;

    private UserDAO userDAO = new UserDAO();
    private PostDAO postDAO = new PostDAO();
    private CommentDAO commentDAO = new CommentDAO();
    private LikeDAO likeDAO = new LikeDAO();
    private User currentUser;


    @FXML
    public void initialize() {
        currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
        }
        Image profileImage = getImageFromBytes(currentUser.getProfileImage());
        profileImageView.setImage(profileImage);

        centralProfileImageView.setImage(profileImage != null ? profileImage : getDefaultProfileImage());
        //loadUserInfo();

        setupMyPostsListView();
        setupMyCommentsListView();
        setupLikedPostsListView();
    }

    public void setUser(User user) {
        this.currentUser = user;
        usernameLabel.setText("@" + user.getId());

        // 프로필 이미지를 데이터베이스에서 불러와 설정
        Image profileImage = getImageFromBytes(user.getProfileImage());
        profileImageView.setImage(profileImage);

        centralProfileImageView.setImage(profileImage != null ? profileImage : getDefaultProfileImage());

        loadUserInfo();
        loadMyPosts();
        loadMyComments();
        loadLikedPosts();
        loadUserStatistics();
    }

    private Image getImageFromBytes(byte[] imageBytes) {
        if (imageBytes != null && imageBytes.length > 0) {
            return new Image(new ByteArrayInputStream(imageBytes));
        } else {
            // 기본 이미지 로드
            return new Image(getClass().getResourceAsStream("/images/default_profile.png"));
        }
    }

    private Image getDefaultProfileImage() {
        return new Image(getClass().getResourceAsStream("/images/default_profile.png"));
    }

    // 사용자 정보 로드
    private void loadUserInfo() {
        idLabel.setText(currentUser.getId());
        emailField.setText(currentUser.getEmail());
        if (currentUser.getBirthday() != null) {
            birthdayPicker.setValue(currentUser.getBirthday());
        }
        phoneField.setText(currentUser.getPhoneNumber());
    }

    private void loadUserStatistics() {
        // 게시글 수
        int postCount = postDAO.getPostCountByUserId(currentUser.getId());
        postCountLabel.setText(String.valueOf(postCount));

        // 팔로워 수
        int followerCount = userDAO.getFollowerCount(currentUser.getId());
        followerCountLabel.setText(String.valueOf(followerCount));

        // 팔로잉 수
        int followingCount = userDAO.getFollowingCount(currentUser.getId());
        followingCountLabel.setText(String.valueOf(followingCount));
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        String newEmail = emailField.getText();
        String newPhone = phoneField.getText();
        LocalDate newBirthday = birthdayPicker.getValue();

        // 생일이 선택되지 않은 경우 현재 생일 유지
        if (newBirthday == null && currentUser.getBirthday() != null) {
            newBirthday = currentUser.getBirthday();
        }

        boolean success = userDAO.updateUser(currentUser.getId(), newEmail, newPhone, newBirthday);
        if (success) {
            messageLabel.setText("정보가 업데이트되었습니다.");
            // 업데이트된 정보를 currentUser에 반영
            currentUser.setEmail(newEmail);
            currentUser.setPhoneNumber(newPhone);
            currentUser.setBirthday(newBirthday);
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
            // 파일 크기 제한 (예: 20MB)
            if (selectedFile.length() > 20 * 1024 * 1024) { // 2MB
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
                    centralProfileImageView.setImage(profileImage);

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
            currentStage.setWidth(1080);
            currentStage.setHeight(720);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "메인 페이지 로드 중 오류가 발생했습니다.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    private void goToMainPageButton(ActionEvent event) {
        try {
            Stage currentStage = (Stage) twitterImage.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUser(currentUser);

            // 현재 Stage 가져오기
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Twitter - Main");
            stage.setWidth(1080);
            stage.setHeight(720);
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

    // 내가 올린 포스트 로드
    private void loadMyPosts() {
        List<Post> myPosts = postDAO.getPostsByUserId(currentUser.getId());
        ObservableList<Post> myPostsList = FXCollections.observableArrayList(myPosts);
        myPostsListView.setItems(myPostsList);
    }

    // 내가 단 댓글 로드
    private void loadMyComments() {
        List<Comment> myComments = commentDAO.getCommentsByUserId(currentUser.getId());
        ObservableList<Comment> myCommentsList = FXCollections.observableArrayList(myComments);
        myCommentsListView.setItems(myCommentsList);
    }

    // 내가 좋아요 한 포스트 로드
    private void loadLikedPosts() {
        List<Post> likedPosts = likeDAO.getLikedPostsByUserId(currentUser.getId());
        ObservableList<Post> likedPostsList = FXCollections.observableArrayList(likedPosts);
        likedPostsListView.setItems(likedPostsList);
    }

    // ListView 셀 팩토리 설정
    private void setupMyPostsListView() {
        myPostsListView.setCellFactory(param -> new PostListCell(currentUser));
    }

    private void setupMyCommentsListView() {
        myCommentsListView.setCellFactory(param -> new CommentListCell(currentUser));
    }

    private void setupLikedPostsListView() {
        likedPostsListView.setCellFactory(param -> new PostListCell(currentUser));
    }
}