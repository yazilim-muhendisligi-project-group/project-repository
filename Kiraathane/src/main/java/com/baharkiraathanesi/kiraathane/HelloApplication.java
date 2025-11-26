package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.database.DatabaseUpdater; // 1. Bu importu ekleyin
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    // 2. init() metodunu ekliyoruz. Uygulama başlatılmadan hemen önce burası çalışır.
    @Override
    public void init() throws Exception {
        System.out.println("Uygulama başlatılıyor, veritabanı kontrol ediliyor...");
        // Veritabanı tablolarını oluştur veya güncelle
        DatabaseUpdater.updateDatabase();
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        // Veritabanı hazırlandıktan sonra giriş ekranını aç
        changeScene("login-view.fxml");
        stage.setTitle("Cafe Otomasyonu");
        stage.show();
    }

    public static void changeScene(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxml));
            // Sahne boyutlarını ihtiyacınıza göre ayarlayabilirsiniz
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen(); // Sahneyi ekranın ortasına al
        } catch (IOException e) {
            System.err.println("Sahne değiştirilemedi: " + fxml);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}