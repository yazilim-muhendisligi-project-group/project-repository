package com.baharkiraathanesi.kiraathane;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        changeScene("login-view.fxml");
        stage.setTitle("Kıraathane Otomasyonu");
        Image appIcon = new Image(getClass().getResourceAsStream("/images/cay_icon.png"));
        stage.getIcons().add(appIcon);
        stage.show();
    }

    public static void changeScene(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxml));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Sahne değiştirilemedi: " + fxml);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}