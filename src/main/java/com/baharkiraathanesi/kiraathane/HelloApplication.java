package com.baharkiraathanesi.kiraathane;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        // Reduce noisy PDFBox/FontBox logging (they scan system fonts at first use)
        try {
            Logger.getLogger("org.apache.pdfbox").setLevel(Level.SEVERE);
            Logger.getLogger("org.apache.fontbox").setLevel(Level.SEVERE);
        } catch (Exception e) {
            // ignore logging configuration failures
        }

        primaryStage = stage;
        changeScene("login-view.fxml");
        stage.setTitle("Bahar Kıraathanesi Otomasyonu");
        stage.show();
    }

    public static void changeScene(String fxml) {
        try {
            // Try two locations: project resource root and package folder.
            String[] candidates = new String[]{"/" + fxml, "/com/baharkiraathanesi/kiraathane/" + fxml};
            URL url = null;
            String found = null;
            for (String c : candidates) {
                url = HelloApplication.class.getResource(c);
                if (url != null) {
                    found = c;
                    break;
                }
            }

            if (url == null) {
                System.err.println("FXML bulunamadı. Aranan yollar: ");
                for (String c : candidates) System.err.println("  " + c);
                System.err.println("Lütfen FXML dosyasını 'src/main/resources' altında uygun pakete koyun.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void main(String[] args) {
        launch();
    }
}
