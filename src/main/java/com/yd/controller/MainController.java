package com.yd.controller;

import com.yd.dao.FollowDAO;
import com.yd.dao.PostDAO;
import com.yd.dao.RetweetDAO;
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

    private File selectedImageFile;

    @FXML
    private Button myPageButton;

    @FXML
    private Button logoutButton;

    @FXML
    private ListView<String> followingListView;

    @FXML
    private ListView<String> recommendListView; // 팔로우 추천 리스트
    @FXML
    private Label usernameLabel; // 사용자 이름 표시

    private PostDAO postDAO = new PostDAO();
    private FollowDAO followDAO = new FollowDAO();

    private User currentUser;
    private boolean isLoading = false;
    @FXML
    private ImageView profileImageView;
    private RetweetDAO retweetDAO = new RetweetDAO();

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

        // ObservableList 초기화 및 설정
        //postItems = FXCollections.observableArrayList();
        postListView.setItems(postItems);
        setupPostListView();

        // 초기 오프셋 설정 및 첫 데이터 로드
        //postOffset = 0;
        //loadMorePosts();

        // 다른 초기화 작업
        loadFollowingList();
        setupFollowingListView();
        loadRecommendList(); // 팔로우 추천 목록 로드

        loadMorePosts();
    }


    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) postListView.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Twitter Clone - Login");
            stage.setWidth(800);
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
                return postDAO.getPostsByUserAndFollowing(currentUser.getId(), postOffset, postLimit);
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
            // 오류 처리 코드 추가 가능
        });

        new Thread(loadTask).start();
    }

    // 포스트 ListView 설정
    private void setupPostListView() {
        postListView.setCellFactory(param -> new ListCell<>() {
            private Button likeButton = new Button();
            private Label likeCountLabel = new Label();
            private HBox hBox = new HBox(10, likeButton, likeCountLabel);

            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 셀 레이아웃 구성
                    VBox content = new VBox();
                    content.setSpacing(5);

                    // 상단: 프로필 이미지, 사용자 아이디
                    HBox header = new HBox();
                    header.setSpacing(10);

                    ImageView profileImageView = new ImageView(new Image("/images/default_profile.png"));
                    profileImageView.setFitWidth(40);
                    profileImageView.setFitHeight(40);

                    Label userIdLabel = new Label(post.getWriterId());

                    header.getChildren().addAll(profileImageView, userIdLabel);

                    // 본문: 텍스트 내용
                    Label textLabel = new Label(post.getText());
                    textLabel.setWrapText(true);

                    // 이미지가 있을 경우 추가
                    ImageView postImageView = null;
                    if (post.getImage() != null) {
                        InputStream is = new ByteArrayInputStream(post.getImage());
                        Image image = new Image(is);
                        postImageView = new ImageView(image);
                        postImageView.setFitWidth(400);
                        postImageView.setPreserveRatio(true);
                    }

                    // 하단: 하트, 댓글, 리트윗 버튼
                    HBox footer = new HBox();
                    footer.setSpacing(20);

                    Button likeButton = new Button();
                    updateLikeButton(likeButton, post);

                    Button commentButton = new Button("댓글");
                    Button retweetButton = new Button("리트윗");

                    updateRetweetButton(retweetButton, post);

                    footer.getChildren().addAll(likeButton, commentButton, retweetButton);

                    // 이벤트 핸들러 설정
                    likeButton.setOnAction(e -> {
                        handleLikeAction(post, likeButton);
                    });

                    commentButton.setOnAction(e -> {
                        openCommentWindow(post);
                    });

                    retweetButton.setOnAction(e -> {
                        handleRetweetAction(post, retweetButton);
                    });

                    // content에 요소 추가
                    content.getChildren().addAll(header, textLabel);
                    if (postImageView != null) {
                        content.getChildren().add(postImageView);
                    }
                    content.getChildren().add(footer);

                    setGraphic(content);

                    // 마지막 아이템 근처에 도달하면 추가 데이터 로드
                    if (getIndex() >= postItems.size() - 5 && !isLoading) {
                        loadMorePosts();
                    }
                }
            }

            private void updateLikeButton(Button likeButton, Post post) {
                boolean isLiked = postDAO.isPostLiked(post.getPostId(), currentUser.getId());
                likeButton.setText(isLiked ? "♥ " + post.getNumOfLikes() : "♡ " + post.getNumOfLikes());
            }

            private void handleLikeAction(Post post, Button likeButton) {
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
                updateLikeButton(likeButton, post);
            }

            private void handleRetweetAction(Post post, Button retweetButton) {
                if (retweetDAO.isRetweeted(post.getPostId(), currentUser.getId())) {
                    boolean success = retweetDAO.removeRetweet(post.getPostId(), currentUser.getId());
                    if (success) {
                        post.setNumOfRetweets(post.getNumOfRetweets() - 1);
                        updateRetweetButton(retweetButton, post);
                    }
                } else {
                    boolean success = retweetDAO.addRetweet(post.getPostId(), currentUser.getId());
                    if (success) {
                        post.setNumOfRetweets(post.getNumOfRetweets() + 1);
                        updateRetweetButton(retweetButton, post);
                    }
                }
            }

            private void updateRetweetButton(Button retweetButton, Post post) {
                boolean isRetweeted = retweetDAO.isRetweeted(post.getPostId(), currentUser.getId());
                retweetButton.setText(isRetweeted ? "리트윗 취소 " + post.getNumOfRetweets() : "리트윗 " + post.getNumOfRetweets());
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
        }
    }

    @FXML
    void handleAttachImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("이미지 선택");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(postTextArea.getScene().getWindow());
    }

    // 팔로우 목록 로드
    private void loadFollowingList() {
        List<Follow> following = followDAO.getFollowing(currentUser.getId());
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Follow follow : following) {
            items.add(follow.getFollowingId());
        }
        followingListView.setItems(items);
    }

    // 팔로우 목록 ListView 설정
    private void setupFollowingListView() {
        followingListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // 더블 클릭 시 언팔로우
                String selectedUser = followingListView.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    unfollowUser(selectedUser);
                }
            }
        });
    }

    // 팔로우 추천 목록 로드
    private void loadRecommendList() {
        List<String> recommendUsers = followDAO.getRecommendUsers(currentUser.getId());
        ObservableList<String> items = FXCollections.observableArrayList(recommendUsers);
        recommendListView.setItems(items);
    }

    @FXML
    void handleFollowRecommendUser(MouseEvent event) {
        if (event.getClickCount() == 2) { // 더블 클릭 시 팔로우
            String selectedUser = recommendListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                followUser(selectedUser);
            }
        }
    }

    // 사용자 팔로우
    private void followUser(String userId) {
        boolean success = followDAO.followUser(userId, currentUser.getId());
        if (success) {
            loadFollowingList();
            loadRecommendList();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, userId + "님을 팔로우했습니다.", ButtonType.OK);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "팔로우에 실패했습니다.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // 특정 사용자를 언팔로우하는 기능
    private void unfollowUser(String userId) {
        boolean success = followDAO.unfollowUser(userId, currentUser.getId());
        if (success) {
            loadFollowingList();
            loadRecommendList();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, userId + "님의 팔로우를 취소했습니다.", ButtonType.OK);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "언팔로우에 실패했습니다.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void handlePost(ActionEvent event) {
        String text = postTextArea.getText();
        if (text.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "포스트 내용을 입력해주세요.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        byte[] imageData = null;
        if (selectedImageFile != null) {
            try {
                imageData = Files.readAllBytes(selectedImageFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "이미지 파일을 읽는 중 오류가 발생했습니다.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
        }
        boolean success = postDAO.addPost(text, imageData, currentUser.getId());
        if (success) {
            postTextArea.clear();
            selectedImageFile = null; // 이미지 선택 초기화
            loadPosts();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "포스트 작성에 실패했습니다.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void goToMyPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mypage.fxml"));
            Scene myPageScene = new Scene(loader.load(), 800, 600);

            Stage stage = (Stage) myPageButton.getScene().getWindow();
            stage.setScene(myPageScene);
            stage.setTitle("Twitter");
            stage.setWidth(800);
            stage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        LoginController.setCurrentUser(null); // 현재 사용자 정보 초기화
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Twitter");
            stage.setWidth(800);
            stage.setHeight(600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
