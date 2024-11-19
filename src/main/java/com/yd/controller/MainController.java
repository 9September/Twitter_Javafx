package com.yd.controller;

import com.yd.dao.FollowDAO;
import com.yd.dao.PostDAO;
import com.yd.dao.RetweetDAO;
import com.yd.dao.UserDAO;
import com.yd.model.Follow;
import com.yd.model.Post;
import com.yd.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class MainController {

    @FXML
    private TextArea postTextArea;

    @FXML
    private ListView<Post> postListView;

    @FXML
    private Button attachImageButton;

    @FXML
    private ListView<User> followingListView;

    @FXML
    private ListView<User> recommendListView; // 팔로우 추천 리스트
    @FXML
    private Label usernameLabel; // 사용자 이름 표시

    private PostDAO postDAO = new PostDAO();
    private FollowDAO followDAO = new FollowDAO();
    private UserDAO userDAO = new UserDAO();

    private User currentUser;
    private boolean isLoading = false;
    @FXML
    private ImageView profileImageView;
    @FXML
    private ImageView postProfileImageView;
    private RetweetDAO retweetDAO = new RetweetDAO();
    @FXML
    private ImageView twitterImage;
    @FXML
    private Label headerLabel;
    @FXML
    private Button logoutButton;
    private byte[] attachedImageBytes = null;
    @FXML
    private ImageView attachedImageView;

    private int postOffset = 0;
    private final int postLimit = 20;
    private ObservableList<Post> postItems = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            // 로그인되지 않은 경우 로그인 화면으로 이동
            goToLogin();
            return;
        }
        usernameLabel.setText("@" + currentUser.getId());
        Image profileImage = getImageFromBytes(currentUser.getProfileImage());
        profileImageView.setImage(profileImage);

        // ObservableList 초기화 및 설정
        postListView.setItems(postItems);
        setupPostListView();

        // 다른 초기화 작업
        loadFollowingList();
        setupFollowingListView();
        setupRecommendListView();
        loadRecommendList();

        loadMorePosts();
    }

    // 바이트 배열을 Image로 변환하는 유틸리티 메서드
    private Image getImageFromBytes(byte[] imageBytes) {
        if (imageBytes != null && imageBytes.length > 0) {
            return new Image(new ByteArrayInputStream(imageBytes));
        } else {
            // 기본 이미지 로드
            return new Image(getClass().getResourceAsStream("/images/default_profile.png"));
        }
    }


    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Twitter - Login");
            stage.setWidth(400);
            stage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 포스트 목록 로드
    private void loadPosts() {
        postOffset = 0; // 오프셋 초기화
        postItems.clear(); // 기존 아이템 초기화
        loadMorePosts(); // 초기 포스트 로드
    }

    private void loadMorePosts() {
        if (isLoading) return;
        isLoading = true;

        // 새로운 스레드에서 데이터 로드 (UI 스레드 블로킹 방지)
        Task<List<Post>> loadTask = new Task<>() {
            @Override
            protected List<Post> call() throws Exception {
                return postDAO.getAllPosts(postOffset, postLimit);
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<Post> posts = loadTask.getValue();
            if (!posts.isEmpty()) {
                postItems.addAll(posts);
                postOffset += posts.size();
            }
            isLoading = false;
        });

        loadTask.setOnFailed(event -> {
            isLoading = false;
            Throwable error = loadTask.getException();
            showAlert("오류", "포스트를 로드하는 중 오류가 발생했습니다: " + error.getMessage());
        });

        new Thread(loadTask).start();
    }

    // 포스트 ListView 설정
    private void setupPostListView() {
        postListView.setCellFactory(param -> new ListCell<>() {
            private VBox content = new VBox();
            private HBox header = new HBox();
            private Label userIdLabel = new Label();
            private Label textLabel = new Label();
            private ImageView postImageView = new ImageView();
            private ImageView postProfileImageView = new ImageView();
            private HBox footer = new HBox(10);
            private Button likeButton = new Button();
            private Label likeCountLabel = new Label();
            private Button commentButton = new Button("댓글");
            private Button retweetButton = new Button();
            private Label retweetCountLabel = new Label();

            {
                // 프로필 이미지 설정
                postProfileImageView.setFitWidth(40);
                postProfileImageView.setFitHeight(40);
                postProfileImageView.setPreserveRatio(true);

                // 사용자 아이디 레이블 설정
                userIdLabel.setStyle("-fx-font-weight: bold;");

                // 텍스트 레이블 설정
                textLabel.setWrapText(true);

                // 포스트 이미지 뷰 설정
                postImageView.setFitWidth(400);
                postImageView.setPreserveRatio(true);
                postImageView.setSmooth(true);
                postImageView.setCache(true);

                // 좋아요 버튼 및 레이블 설정
                footer.getChildren().addAll(likeButton, likeCountLabel, commentButton);
                footer.setSpacing(10);

                // 헤더에 프로필 이미지와 사용자 아이디 추가
                header.getChildren().addAll(postProfileImageView, userIdLabel);
                header.setSpacing(10);

                // 콘텐츠에 헤더, 텍스트, 이미지 추가
                content.getChildren().addAll(header, textLabel, postImageView, footer);
                content.setSpacing(5);
            }

            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    byte[] writerImageBytes = userDAO.getUserProfileImage(post.getWriterId());

                    Image profileImage = getImageFromBytes(writerImageBytes);
                    postProfileImageView.setImage(profileImage != null ? profileImage : getDefaultProfileImage());

                    userIdLabel.setText("@" + post.getWriterId());
                    textLabel.setText(post.getText());

                    if (post.getImage() != null && post.getImage().length > 0) {
                        Image postImage = getImageFromBytes(post.getImage());
                        postImageView.setImage(postImage);
                        postImageView.setVisible(true);
                    } else {
                        postImageView.setImage(null);
                        postImageView.setVisible(false);
                    }

                    // 좋아요 상태 및 카운트 업데이트
                    boolean isLiked = postDAO.isPostLiked(post.getPostId(), currentUser.getId());
                    likeButton.setText(isLiked ? "♥" : "♡");
                    likeCountLabel.setText(String.valueOf(post.getNumOfLikes()));

                    // 리트윗 상태 및 카운트 업데이트
                    boolean isRetweeted = retweetDAO.isRetweeted(post.getPostId(), currentUser.getId());
                    retweetButton.setText(isRetweeted ? "🔁" : "🔁");
                    retweetCountLabel.setText(String.valueOf(post.getNumOfRetweets()));

                    // 이벤트 핸들러 설정
                    likeButton.setOnAction(e -> {
                        handleLikeAction(post, likeButton, likeCountLabel);
                    });

                    commentButton.setOnAction(e -> {
                        openCommentWindow(post);
                    });

                    retweetButton.setOnAction(e -> {
                        handleRetweetAction(post, retweetButton, retweetCountLabel);
                    });

                    setGraphic(content);

                    // 마지막 아이템 근처에 도달하면 추가 데이터 로드
                    if (getIndex() >= postItems.size() - 5 && !isLoading) {
                        loadMorePosts();
                    }
                }
            }

            private void handleLikeAction(Post post, Button likeButton, Label likeCountLabel) {
                if (postDAO.isPostLiked(post.getPostId(), currentUser.getId())) {
                    boolean success = postDAO.unlikePost(post.getPostId(), currentUser.getId());
                    if (success) {
                        post.setNumOfLikes(post.getNumOfLikes() - 1);
                    }
                } else {
                    boolean success = postDAO.likePost(post.getPostId(), currentUser.getId());
                    if (success) {
                        post.setNumOfLikes(post.getNumOfLikes() + 1);
                    }
                }
                // 좋아요 상태 및 카운트 업데이트
                boolean isLiked = postDAO.isPostLiked(post.getPostId(), currentUser.getId());
                likeButton.setText(isLiked ? "♥" : "♡");
                likeCountLabel.setText(String.valueOf(post.getNumOfLikes()));
            }

            private void handleRetweetAction(Post post, Button retweetButton, Label retweetCountLabel) {
                if (retweetDAO.isRetweeted(post.getPostId(), currentUser.getId())) {
                    boolean success = retweetDAO.removeRetweet(post.getPostId(), currentUser.getId());
                    if (success) {
                        post.setNumOfRetweets(post.getNumOfRetweets() - 1);
                    }
                } else {
                    boolean success = retweetDAO.addRetweet(post.getPostId(), currentUser.getId());
                    if (success) {
                        post.setNumOfRetweets(post.getNumOfRetweets() + 1);
                    }
                }
                // 리트윗 상태 및 카운트 업데이트
                boolean isRetweeted = retweetDAO.isRetweeted(post.getPostId(), currentUser.getId());
                retweetButton.setText(isRetweeted ? "🔁" : "🔁");
                retweetCountLabel.setText(String.valueOf(post.getNumOfRetweets()));
            }

            private Image getDefaultProfileImage() {
                return new Image(getClass().getResourceAsStream("/images/default_profile.png"));
            }
        });
    }

    private void openCommentWindow(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment.fxml"));
            Parent root = loader.load();

            // PostController에 포스트 전달
            PostController postController = loader.getController();
            postController.setPost(post);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Comments for Post ID: " + post.getPostId());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setWidth(600);
            stage.setHeight(400);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("오류", "댓글 창을 여는 중 오류가 발생했습니다.");
        }
    }

    @FXML
    private void handleAttachImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("이미지 첨부");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("이미지 파일", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(attachImageButton.getScene().getWindow());
        if (selectedFile != null) {
            // 파일 크기 제한 (예: 5MB)
            if (selectedFile.length() > 5 * 1024 * 1024) { // 5MB
                showAlert("경고", "이미지 파일 크기가 너무 큽니다. 5MB 이하의 파일을 선택해주세요.");
                return;
            }

            try {
                attachedImageBytes = Files.readAllBytes(selectedFile.toPath());
                Image image = new Image(new ByteArrayInputStream(attachedImageBytes));
                attachedImageView.setImage(image); // 이미지 미리보기 설정
                showAlert("성공", "이미지가 첨부되었습니다.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("오류", "이미지 첨부 중 오류가 발생했습니다.");
            }
        }
    }



    private void loadFollowingList() {
        List<User> followingUsers = followDAO.getFollowingUsers(currentUser.getId());
        ObservableList<User> items = FXCollections.observableArrayList(followingUsers);
        followingListView.setPlaceholder(new Label("팔로잉 목록이 없습니다."));
        followingListView.setItems(items);
    }

    // 팔로우 목록 ListView 설정
    private void setupFollowingListView() {
        followingListView.setCellFactory(param -> new ListCell<>() {
            private HBox content;
            private ImageView profileImageView;
            private Label nameLabel;

            {
                content = new HBox(10);
                profileImageView = new ImageView();
                profileImageView.setFitWidth(40);
                profileImageView.setFitHeight(40);
                profileImageView.setPreserveRatio(true);

                nameLabel = new Label();
                nameLabel.getStyleClass().add("user-name-label");

                content.getChildren().addAll(profileImageView, nameLabel);
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Image profileImage = getImageFromBytes(user.getProfileImage());
                    profileImageView.setImage(profileImage != null ? profileImage : getDefaultProfileImage());
                    nameLabel.setText(user.getId());

                    setGraphic(content);

                    // Hover 시 스타일 적용
                    content.setOnMouseEntered(e -> content.setStyle("-fx-background-color: #f0f0f0;"));
                    content.setOnMouseExited(e -> content.setStyle("-fx-background-color: transparent;"));
                }
            }
        });

        followingListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // 더블 클릭 시 언팔로우
                User selectedUser = followingListView.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    unfollowUser(selectedUser.getId()); // 사용자 아이디로 언팔로우
                }
            }
        });
    }

    private Image getDefaultProfileImage() {
        return new Image(getClass().getResourceAsStream("/images/default_profile.png"));
    }

    private void setupRecommendListView() {
        recommendListView.setCellFactory(param -> new ListCell<>() {
            private HBox content;
            private ImageView profileImageView;
            private Label nameLabel;

            {
                content = new HBox(10);
                profileImageView = new ImageView();
                profileImageView.setFitWidth(40);
                profileImageView.setFitHeight(40);
                profileImageView.setPreserveRatio(true);

                nameLabel = new Label();
                nameLabel.getStyleClass().add("user-name-label");

                content.getChildren().addAll(profileImageView, nameLabel);
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Image profileImage = getImageFromBytes(user.getProfileImage());
                    profileImageView.setImage(profileImage != null ? profileImage : getDefaultProfileImage());
                    nameLabel.setText(user.getId());

                    setGraphic(content);

                    // Hover 시 스타일 적용
                    content.setOnMouseEntered(e -> content.setStyle("-fx-background-color: #f0f0f0;"));
                    content.setOnMouseExited(e -> content.setStyle("-fx-background-color: transparent;"));
                }
            }
        });

        recommendListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // 더블 클릭 시 팔로우
                User selectedUser = recommendListView.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    followUser(selectedUser.getId()); // 사용자 아이디로 팔로우
                }
            }
        });
    }

    // 팔로우 추천 목록 로드
    private void loadRecommendList() {
        List<User> recommendUsers = followDAO.getRecommendUsers(currentUser.getId());
        ObservableList<User> items = FXCollections.observableArrayList(recommendUsers);
        recommendListView.setPlaceholder(new Label("추천할 사용자가 없습니다."));
        recommendListView.setItems(items);
    }

    @FXML
    void handleFollowRecommendUser(MouseEvent event) {
        if (event.getClickCount() == 2) { // 더블 클릭 시 팔로우
            User selectedUser = recommendListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                followUser(selectedUser.getId());
            }
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // 사용자 팔로우
    private void followUser(String userId) {
        boolean success = followDAO.followUser(userId, currentUser.getId());
        if (success) {
            loadFollowingList();
            loadRecommendList();
            showAlert("성공", userId + "님을 팔로우했습니다.");
        } else {
            showAlert("오류", "팔로우에 실패했습니다.");
        }
    }

    // 특정 사용자를 언팔로우하는 기능
    private void unfollowUser(String userId) {
        boolean success = followDAO.unfollowUser(userId, currentUser.getId());
        if (success) {
            loadFollowingList();
            loadRecommendList();
            showAlert("성공", userId + "님의 팔로우를 취소했습니다.");
        } else {
            showAlert("오류", "언팔로우에 실패했습니다.");
        }
    }

    @FXML
    void handlePost(ActionEvent event) {
        String text = postTextArea.getText().trim();
        if (text.isEmpty()) {
            showAlert("경고", "포스트 내용을 입력해주세요.");
            return;
        }

        byte[] imageData = attachedImageBytes; // 올바른 이미지 데이터 사용

        boolean success = postDAO.addPost(text, imageData, currentUser.getId());
        if (success) {
            postTextArea.clear();
            attachedImageBytes = null; // 이미지 선택 초기화
            attachedImageView.setImage(null); // 이미지 미리보기 초기화
            loadPosts(); // 포스트 다시 로드
            showAlert("성공", "포스트가 작성되었습니다.");
        } else {
            showAlert("오류", "포스트 작성에 실패했습니다.");
        }
    }


    @FXML
    void goToMyPage(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mypage.fxml"));
            Parent root = loader.load();

            MyPageController myPageController = loader.getController();
            myPageController.setUser(currentUser); // 현재 사용자 정보를 전달하는 메서드가 있어야 함


            Stage stage = (Stage) profileImageView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Page");
            stage.setWidth(1080);
            stage.setHeight(720);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("오류", "마이페이지 로드 중 오류가 발생했습니다.");
        }
    }

    @FXML
    private void goToMainPage(MouseEvent event) {
        try {
            // 현재 창을 닫고 메인 창을 다시 로드하는 방식
            Stage currentStage = (Stage) twitterImage.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUser(currentUser);

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Twitter - Main");
            currentStage.setWidth(1080);
            currentStage.setHeight(720);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("오류", "메인 페이지 로드 중 오류가 발생했습니다.");
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        usernameLabel.setText("@" + user.getId());

        // 프로필 이미지 설정
        Image profileImage = getImageFromBytes(user.getProfileImage());
        profileImageView.setImage(profileImage);
    }
}