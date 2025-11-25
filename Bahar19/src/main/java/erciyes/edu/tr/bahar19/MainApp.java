package erciyes.edu.tr.bahar19;

import erciyes.edu.tr.bahar19.View.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Kendi yazdığımız View sınıfından bir tane oluştur
        LoginView girisEkrani = new LoginView();

        // 2. Sahneye bu View'ı koy
        Scene scene = new Scene(girisEkrani, 1000, 600);

        primaryStage.setTitle("Kıraathane Otomasyonu v1.0");
        // --- İKON EKLEME KISMI BURASI ---
        try {
            // "resources/images" klasöründeki resmi alıp pencere ikonu yapıyoruz
            Image appIcon = new Image(getClass().getResourceAsStream("/images/cay_icon.png"));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.out.println("Uygulama ikonu bulunamadı, varsayılan Java ikonu kullanılacak.");
        }
        // --------------------------------
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}