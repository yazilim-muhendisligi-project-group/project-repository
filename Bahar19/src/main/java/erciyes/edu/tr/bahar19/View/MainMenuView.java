package erciyes.edu.tr.bahar19.View;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainMenuView extends BorderPane {
    public MainMenuView() {
        initView();
    }

    private void initView() {
        // GENEL AYARLAR
        this.setStyle("-fx-background-color: #ECEFF1;"); // Açık gri modern zemin

        // 1. ÜST KISIM (HEADER)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new javafx.geometry.Insets(20));
        header.setStyle("-fx-background-color: #B71C1C;");

        Label lblTitle = new Label("Bahar Kıraathanesi - Yönetim Paneli");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        header.getChildren().add(lblTitle);
        this.setTop(header);

        // 2. ORTA KISIM (BUTONLAR)
        HBox centerMenu = new HBox(30); // Butonlar arası 30px boşluk
        centerMenu.setAlignment(Pos.CENTER);

        // -- BUTONLARI OLUŞTUR --
        // (Metin, Renk, Emoji/İkon)
        Button btnMasalar = createMenuButton("MASALAR", "#2E7D32", "🪑"); // Yeşil
        Button btnStok = createMenuButton("STOK TAKİP", "#EF6C00", "📦");   // Turuncu
        Button btnZRaporu = createMenuButton("Z RAPORU", "#455A64", "📄");  // Koyu Gri

        // -- TIKLAMA OLAYLARI (Şimdilik boş) --
        btnMasalar.setOnAction(e -> {
            System.out.println("Navigating to Tables view...");

            // Sayfa Geçişi
            Scene currentScene = this.getScene();
            if(currentScene != null) {
                currentScene.setRoot(new TablesView());
            }
        });
        btnStok.setOnAction(e -> {
            System.out.println("Stok ekranına gidiliyor...");
            Scene currentScene = btnStok.getScene();
            if(currentScene != null) {
                // Sayfayı değiştir
                currentScene.setRoot(new StockView());
            }
        });


        btnZRaporu.setOnAction(e -> {
            System.out.println("Z Raporu alınıyor...");
                    Scene currentScene = btnZRaporu.getScene();
                    if(currentScene != null) {
                        currentScene.setRoot(new ReportView());
                    }
        });

        centerMenu.getChildren().addAll(btnMasalar, btnStok, btnZRaporu);
        this.setCenter(centerMenu);
    }

    // Butonları standart oluşturmak için yardımcı metot
    private Button createMenuButton(String text, String colorHex, String icon) {
        Button btn = new Button(icon + "\n" + text);
        btn.setPrefSize(200, 150); // Büyük buton boyutu
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        btn.setTextFill(Color.WHITE);

        // Buton Stili (Gölge ve Renk)
        btn.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);" +
                        "-fx-cursor: hand;" +
                        "-fx-text-alignment: center;" // Yazıyı ortala
        );

        // Fare üzerine gelince rengi hafif açma efekti
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + colorHex + ", 20%); -fx-background-radius: 10; -fx-text-alignment: center;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 10; -fx-text-alignment: center;"));

        return btn;
    }
}