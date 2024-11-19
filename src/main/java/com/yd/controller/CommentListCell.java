package com.yd.controller;

import com.yd.dao.CommentDAO;
import com.yd.dao.PostDAO;
import com.yd.dao.UserDAO;
import com.yd.model.Comment;
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

public class CommentListCell extends ListCell<Comment> {

    private HBox content;
    private ImageView profileImageView;
    private VBox commentContent;
    private HBox header;
    private Label userIdLabel;
    private Label commentTextLabel;
    private Label postInfoLabel;
    private HBox footer;
    private Button likeButton;
    private Label likeCountLabel;

    private UserDAO userDAO = new UserDAO();
    private CommentDAO commentDAO = new CommentDAO();
    private User currentUser;

    public CommentListCell(User currentUser) {
        this.currentUser = currentUser;

        // 프로필 이미지 설정
        profileImageView = new ImageView();
        profileImageView.setFitWidth(30);
        profileImageView.setFitHeight(30);
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);
        profileImageView.setCache(true);

        // 사용자 아이디 레이블
        userIdLabel = new Label();
        userIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        // 헤더 HBox
        header = new HBox(10, profileImageView, userIdLabel);
        header.setPadding(new Insets(5, 0, 5, 0));

        // 포스트 정보 레이블
        postInfoLabel = new Label();
        //postInfoLabel.setText("ㄴ " + "@" + post.getWriterId() + ": " + post.getText());
        postInfoLabel.setStyle("-fx-font-style: italic; -fx-font-size: 12px; -fx-text-fill: #555555;");
        postInfoLabel.setWrapText(true);

        // 댓글 텍스트 레이블
        commentTextLabel = new Label();
        commentTextLabel.setWrapText(true);
        commentTextLabel.setStyle("-fx-font-size: 12px;");

        // 좋아요 버튼 및 레이블
        likeButton = new Button("♡");
        likeCountLabel = new Label("0");

        // 푸터 HBox
        footer = new HBox(10, likeButton, likeCountLabel);
        footer.setPadding(new Insets(5, 0, 5, 0));

        // 댓글 내용 VBox
        commentContent = new VBox(5, header, commentTextLabel, footer);
        commentContent.setPadding(new Insets(10));
        commentContent.getStyleClass().add("comment-cell");

        // 전체 콘텐츠 HBox
        content = new HBox(commentContent);
        content.setSpacing(10);
    }

    @Override
    protected void updateItem(Comment comment, boolean empty) {
        super.updateItem(comment, empty);
        if (empty || comment == null) {
            setText(null);
            setGraphic(null);
        } else {
            // 사용자 정보 로드
            User writer = userDAO.getUserById(comment.getWriterId());
            if (writer != null && writer.getProfileImage() != null) {
                Image profileImage = new Image(new ByteArrayInputStream(writer.getProfileImage()));
                profileImageView.setImage(profileImage);
            } else {
                // 기본 프로필 이미지
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_profile.png")));
            }

            userIdLabel.setText("@" + comment.getWriterId());

            commentTextLabel.setText(comment.getText());

            // 포스트 정보 표시
            Post post = comment.getPost();
            if (post != null) {
                postInfoLabel.setText("ㄴ " + "@" + post.getWriterId() + ": " + post.getText());
            } else {
                postInfoLabel.setText("ㄴ " + "Unknown Post");
            }

            // 좋아요 상태 및 카운트 업데이트
            boolean isLiked = commentDAO.isCommentLiked(comment.getCommentId(), currentUser.getId());
            likeButton.setText(isLiked ? "♥" : "♡");
            likeCountLabel.setText(String.valueOf(comment.getNumOfLikes()));

            // 좋아요 버튼 이벤트 핸들러
            likeButton.setOnAction(e -> {
                if (commentDAO.isCommentLiked(comment.getCommentId(), currentUser.getId())) {
                    boolean success = commentDAO.unlikeComment(comment.getCommentId(), currentUser.getId());
                    if (success) {
                        comment.setNumOfLikes(comment.getNumOfLikes() - 1);
                    }
                } else {
                    boolean success = commentDAO.likeComment(comment.getCommentId(), currentUser.getId());
                    if (success) {
                        comment.setNumOfLikes(comment.getNumOfLikes() + 1);
                    }
                }
                // 상태 및 카운트 업데이트
                boolean updatedLikeStatus = commentDAO.isCommentLiked(comment.getCommentId(), currentUser.getId());
                likeButton.setText(updatedLikeStatus ? "♥" : "♡");
                likeCountLabel.setText(String.valueOf(comment.getNumOfLikes()));
            });

            setGraphic(content);
        }
    }
}