package com.yd.controller;

import com.yd.dao.CommentDAO;
import com.yd.model.Comment;
import com.yd.model.Post;
import com.yd.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class PostController {

    @FXML
    private Label postLabel;

    @FXML
    private ListView<Comment> commentListView;

    @FXML
    private TextField commentField;

    @FXML
    private Button addCommentButton;

    private CommentDAO commentDAO = new CommentDAO();
    private User currentUser;
    private Post currentPost;

    public void setPost(Post post) {
        this.currentPost = post;
        postLabel.setText("[" + post.getWriterId() + "] " + post.getText());
        loadComments();
    }

    @FXML
    public void initialize() {
        currentUser = LoginController.getCurrentUser();
        commentListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty || comment == null) {
                    setText(null);
                } else {
                    setText("[" + comment.getWriterId() + "] " + comment.getText() + " (" + comment.getNumOfLikes() + " likes)");
                }
            }
        });
    }

    // 댓글 목록 로드
    private void loadComments() {
        if (currentPost == null) return;
        List<Comment> comments = commentDAO.getCommentsByPostId(currentPost.getPostId());
        ObservableList<Comment> items = FXCollections.observableArrayList(comments);
        commentListView.setItems(items);
    }

    @FXML
    void handleAddComment() {
        String text = commentField.getText();
        if (text.isEmpty()) {
            return;
        }
        boolean success = commentDAO.addComment(text, currentUser.getId(), currentPost.getPostId());
        if (success) {
            commentField.clear();
            loadComments();
        } else {
            // 댓글 추가 실패 시 메시지 표시
            Alert alert = new Alert(Alert.AlertType.ERROR, "댓글 작성에 실패했습니다.");
            alert.showAndWait();
        }
    }
}