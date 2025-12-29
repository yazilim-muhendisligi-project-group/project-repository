package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(HelloApplication.class.getName());
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseConnection.setupDatabase();
        primaryStage = stage;

        changeScene("login-view.fxml");
        stage.setTitle("DİJİTAL ÇIRAK");

        if (!stage.isMaximized()) {
            stage.setWidth(814);
            stage.setHeight(768);
        }

        try (InputStream iconStream = getClass().getResourceAsStream("/images/cay_icon.png")) {
            if (iconStream != null) {
                Image appIcon = new Image(iconStream);
                stage.getIcons().add(appIcon);
            } else {
                LOGGER.warning("Uygulama ikonu bulunamadi");
            }
        }

        stage.show();
    }

    public static void changeScene(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxml));
            Parent root = fxmlLoader.load();

            boolean isMaximized = (primaryStage.getScene() != null) && primaryStage.isMaximized();
            double width = (primaryStage.getScene() != null) ? primaryStage.getWidth() : 814;
            double height = (primaryStage.getScene() != null) ? primaryStage.getHeight() : 768;

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            if (isMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(width);
                primaryStage.setHeight(height);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Sahne degistirilemedi: " + fxml, e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}