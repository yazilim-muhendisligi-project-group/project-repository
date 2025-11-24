package erciyes.edu.tr.bahar19;

import erciyes.edu.tr.bahar19.View.GirisEkrani;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Kendi yazdığımız View sınıfından bir tane oluştur
        GirisEkrani girisEkrani = new GirisEkrani();

        // 2. Sahneye bu View'ı koy
        Scene scene = new Scene(girisEkrani, 800, 600);

        primaryStage.setTitle("Kıraathane Otomasyonu v1.0");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}