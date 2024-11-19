package com.yd.controller;

import com.yd.dao.LikeDAO;
import com.yd.dao.PostDAO;
import com.yd.dao.RetweetDAO;
import com.yd.dao.UserDAO;
import com.yd.model.Post;
import com.yd.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;

public class PostListCell extends ListCell<Post> {

    private HBox content;
    private ImageView profileImageView;
    private VBox postContent;
    private HBox header;
    private Label userIdLabel;
    private Label postTextLabel;
    private ImageView postImageView;
    private HBox footer;
    private Button likeButton;
    private Label likeCountLabel;
    private Button retweetButton;
    private Label retweetCountLabel;

    private UserDAO userDAO = new UserDAO();
    private LikeDAO likeDAO = new LikeDAO();
    private User currentUser;

    public PostListCell(User currentUser) {
        this.currentUser = currentUser;

        // 프로필 이미지 설정
        profileImageView = new ImageView();
        profileImageView.setFitWidth(40);
        profileImageView.setFitHeight(40);
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);
        profileImageView.setCache(true);

        // 사용자 아이디 레이블
        userIdLabel = new Label();
        userIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // 헤더 HBox
        header = new HBox(10, profileImageView, userIdLabel);
        header.setPadding(new Insets(5, 0, 5, 0));

        // 포스트 텍스트 레이블
        postTextLabel = new Label();
        postTextLabel.setWrapText(true);
        postTextLabel.setStyle("-fx-font-size: 14px;");

        // 포스트 이미지 뷰
        postImageView = new ImageView();
        postImageView.setFitWidth(400);
        postImageView.setPreserveRatio(true);
        postImageView.setSmooth(true);
        postImageView.setCache(true);
        postImageView.setVisible(false); // 기본적으로 숨김

        // 좋아요 버튼 및 레이블
        likeButton = new Button("♡");
        likeCountLabel = new Label("0");

        // 푸터 HBox
        footer = new HBox(15, likeButton, likeCountLabel);
        footer.setPadding(new Insets(5, 0, 5, 0));

        // 포스트 내용 VBox
        postContent = new VBox(5, header, postTextLabel, postImageView, footer);
        postContent.setPadding(new Insets(10));
        postContent.getStyleClass().add("post-cell");

        // 전체 콘텐츠 HBox
        content = new HBox(postContent);
        content.setSpacing(10);
    }

    @Override
    protected void updateItem(Post post, boolean empty) {
        super.updateItem(post, empty);
        if (empty || post == null) {
            setText(null);
            setGraphic(null);
        } else {
            // 사용자 정보 로드
            User writer = userDAO.getUserById(post.getWriterId());
            if (writer != null && writer.getProfileImage() != null) {
                Image profileImage = new Image(new ByteArrayInputStream(writer.getProfileImage()));
                profileImageView.setImage(profileImage);
            } else {
                // 기본 프로필 이미지
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_profile.png")));
            }

            userIdLabel.setText("@" + post.getWriterId());

            postTextLabel.setText(post.getText());

            // 포스트 이미지 설정
            if (post.getImage() != null && post.getImage().length > 0) {
                Image postImage = new Image(new ByteArrayInputStream(post.getImage()));
                postImageView.setImage(postImage);
                postImageView.setVisible(true);
            } else {
                postImageView.setImage(null);
                postImageView.setVisible(false);
            }

            // 좋아요 상태 및 카운트 업데이트
            boolean isLiked = likeDAO.isPostLiked(post.getPostId(), currentUser.getId());
            likeButton.setText(isLiked ? "♥" : "♡");
            likeCountLabel.setText(String.valueOf(post.getNumOfLikes()));

            // 좋아요 버튼 이벤트 핸들러
            likeButton.setOnAction(e -> {
                if (likeDAO.isPostLiked(post.getPostId(), currentUser.getId())) {
                    boolean success = likeDAO.unlikePost(post.getPostId(), currentUser.getId());
                    if (success) {
                        post.setNumOfLikes(post.getNumOfLikes() - 1);
                    }
                } else {
                    boolean success = likeDAO.likePost(post.getPostId(), currentUser.getId());
                    if (success) {
                        post.setNumOfLikes(post.getNumOfLikes() + 1);
                    }
                }
                // 상태 및 카운트 업데이트
                boolean updatedLikeStatus = likeDAO.isPostLiked(post.getPostId(), currentUser.getId());
                likeButton.setText(updatedLikeStatus ? "♥" : "♡");
                likeCountLabel.setText(String.valueOf(post.getNumOfLikes()));
            });

            setGraphic(content);
        }
    }
}
