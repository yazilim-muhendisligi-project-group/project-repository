package erciyes.edu.tr.bahar19.View;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MasaSecimEkrani extends Application {

    private FlowPane masalarContainer; // Masaların duracağı alan
    private int masaSayac = 1; // Yeni masa eklenirken numara vermek için

    @Override
    public void start(Stage primaryStage) {
        // --- 1. Ana Düzen (BorderPane) ---
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // --- 2. Üst Kısım (Sağ Üstte Ekle Butonu) ---
        HBox topMenu = new HBox();
        topMenu.setAlignment(Pos.CENTER_RIGHT); // Sağa yasla

        Button btnMasaEkle = new Button("+ Yeni Masa Ekle");
        btnMasaEkle.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");

        // Butona basınca yeni masa ekleme fonksiyonu
        btnMasaEkle.setOnAction(e -> masaEkle());

        topMenu.getChildren().add(btnMasaEkle);
        root.setTop(topMenu);

        // --- 3. Orta Kısım (Masalar) ---
        masalarContainer = new FlowPane();
        masalarContainer.setHgap(20); // Yatay boşluk
        masalarContainer.setVgap(20); // Dikey boşluk
        masalarContainer.setPadding(new Insets(20, 0, 20, 0));
        masalarContainer.setAlignment(Pos.TOP_LEFT);

        // Başlangıçta 10 adet masa ekle
        for (int i = 0; i < 10; i++) {
            masaEkle();
        }

        // Masalar çok olursa kaydırma çubuğu çıksın diye ScrollPane içine alıyoruz
        ScrollPane scrollPane = new ScrollPane(masalarContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        root.setCenter(scrollPane);

        // --- 4. Alt Kısım (Sol Altta Geri Butonu) ---
        HBox bottomMenu = new HBox();
        bottomMenu.setAlignment(Pos.CENTER_LEFT); // Sola yasla

        Button btnGeri = new Button("← Ana Menü");
        btnGeri.setStyle("-fx-font-size: 14px;");
        btnGeri.setOnAction(e -> System.out.println("Ana menüye dönülüyor...")); // Buraya kendi geçiş kodunu yazabilirsin

        bottomMenu.getChildren().add(btnGeri);
        root.setBottom(bottomMenu);

        // --- Sahne Ayarları ---
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Kahvehane Masa Takip Sistemi");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Yeni bir masa oluşturup ekrana ekleyen metot
    private void masaEkle() {
        MasaView yeniMasa = new MasaView("Masa " + masaSayac++);
        masalarContainer.getChildren().add(yeniMasa);
    }

    /**
     * ÖZEL MASA SINIFI (Custom Component)
     * Her masa kendi görselini ve dolu/boş mantığını burada tutar.
     */
    private class MasaView extends StackPane {
        private boolean doluMu = false;
        private Circle durumIsigi;
        private Label durumYazisi;
        private Rectangle masaSekli;

        public MasaView(String masaIsmi) {
            // 1. Masanın Kendisi (Dikdörtgen)
            masaSekli = new Rectangle(120, 80);
            masaSekli.setArcWidth(10); // Köşeleri hafif yuvarlat
            masaSekli.setArcHeight(10);
            masaSekli.setFill(Color.SADDLEBROWN); // Ahşap rengi
            masaSekli.setStroke(Color.BLACK);

            // 2. Durum Işığı (Daire)
            durumIsigi = new Circle(8); // Yarıçap
            durumIsigi.setStroke(Color.DARKGRAY);
            durumIsigi.setFill(Color.WHITE); // Varsayılan boş

            // 3. Yazılar (Masa Adı ve Durum)
            Label lblIsim = new Label(masaIsmi);
            lblIsim.setTextFill(Color.WHITE);
            lblIsim.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            durumYazisi = new Label(""); // Başlangıçta boş
            durumYazisi.setTextFill(Color.YELLOW); // Uyarı rengi
            durumYazisi.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            // İçerikleri dikey hizala
            VBox icerik = new VBox(5); // Elemanlar arası 5px boşluk
            icerik.setAlignment(Pos.CENTER);
            icerik.getChildren().addAll(lblIsim, durumIsigi, durumYazisi);

            // StackPane (MasaView) içine ekle
            this.getChildren().addAll(masaSekli, icerik);

            // Tıklama Olayı: Masaya tıklayınca durumu değiştir (Test için)
            this.setOnMouseClicked(e -> durumuDegistir());
        }

        // Dolu/Boş durumunu değiştiren metot
        public void durumuDegistir() {
            this.doluMu = !this.doluMu; // Tersi yap

            if (doluMu) {
                // DOLU DURUMU
                durumIsigi.setFill(Color.RED);
                durumIsigi.setStroke(Color.DARKRED);
                durumYazisi.setText("DOLU");
                masaSekli.setOpacity(0.8); // Doluyken masa biraz koyulaşsın
            } else {
                // BOŞ DURUMU
                durumIsigi.setFill(Color.WHITE);
                durumIsigi.setStroke(Color.DARKGRAY);
                durumYazisi.setText(""); // Boşken yazı yok (istediğin gibi)
                masaSekli.setOpacity(1.0);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
