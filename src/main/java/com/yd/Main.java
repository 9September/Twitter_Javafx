package com.yd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        primaryStage.setScene(new Scene(loader.load(), 400, 600)); // 창 크기 설정
        primaryStage.setTitle("Twitter - Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}