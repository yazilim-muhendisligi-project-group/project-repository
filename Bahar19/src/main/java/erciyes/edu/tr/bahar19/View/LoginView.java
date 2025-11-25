package erciyes.edu.tr.bahar19.View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginView extends StackPane {
    public LoginView(){
        initView();
    }

    private void initView() {
        // 1. ANA ARKA PLAN
        this.setStyle("-fx-background-color: #B71C1C;");

        // 2. ANA KART (YATAY KUTU - HBOX)
        HBox loginCard = new HBox();
        loginCard.setMaxWidth(800); // Daha geniş bir kart
        loginCard.setMaxHeight(450); // Yatay görünüm için yükseklik sınırı
        loginCard.setAlignment(Pos.CENTER);

        // Kartın dış tasarımı (Beyaz, gölgeli, yuvarlak)
        loginCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 0);"
        );

        // =================================================================
        // SOL TARAF (MARKALAMA VE İKON ALANI)
        // =================================================================
        VBox leftSide = new VBox(15); // Elemanlar arası 15px boşluk
        leftSide.setAlignment(Pos.CENTER);
        leftSide.setPadding(new Insets(40));
        // Sol tarafın genişliğini sabitleyelim ki sağ taraf sıkışmasın
        leftSide.setPrefWidth(350);
        // Sol tarafa hafif gri bir ton verelim, sağdan ayrılsın
        leftSide.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 15 0 0 15;");

        // --- İKON EKLEME ---
        ImageView iconView = new ImageView();
        try {
            // Resmi resources klasöründen çekmeye çalışıyoruz
            Image iconImage = new Image(getClass().getResourceAsStream("/images/cay_bardagi.png"));
            iconView.setImage(iconImage);
            iconView.setFitWidth(100); // İkon boyutu
            iconView.setPreserveRatio(true);
        } catch (Exception e) {
            // Resim bulunamazsa geçici olarak bir text gösterelim
            System.out.println("İkon bulunamadı: /images/cay_bardagi.png");
            // Alternatif olarak buraya geçici bir Label ekleyebilirsiniz.
        }

        // Başlıklar
        Label lblTitle = new Label("BAHAR\nKIRAATHANESİ");
        lblTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblTitle.setTextFill(Color.web("#263238")); // Koyu antrasit renk
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        Label lblSubtitle = new Label("Dijital Dönüşüm Paneli");
        lblSubtitle.setTextFill(Color.GRAY);
        lblSubtitle.setFont(Font.font("Segoe UI", 14));

        leftSide.getChildren().addAll(iconView, lblTitle, lblSubtitle);


        // =================================================================
        // SAĞ TARAF (FORM ALANI)
        // =================================================================
        VBox rightSide = new VBox(25); // Boşlukları artırdık
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setPadding(new Insets(50));
        HBox.setHgrow(rightSide, Priority.ALWAYS); // Kalan tüm genişliği kaplasın

        Label lblGirisBaslik = new Label("Hoş Geldiniz");
        lblGirisBaslik.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblGirisBaslik.setTextFill(Color.web("#1565C0"));

        // Form (GridPane)
        GridPane formGrid = new GridPane();
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setHgap(10);
        formGrid.setVgap(15); // Dikey boşlukları artırdık

        // Input stilleri için ortak CSS
        String inputStyle = "-fx-padding: 12px; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-font-size: 14px;";

        TextField txtUser = new TextField();
        txtUser.setPromptText("Kullanıcı Adı");
        txtUser.setStyle(inputStyle);
        txtUser.setPrefWidth(300);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Şifre");
        txtPass.setStyle(inputStyle);
        txtPass.setPrefWidth(300);

        // Grid'e ekle (Sadece kutuları ekliyoruz, labellara gerek kalmadı, prompt text var)
        formGrid.add(txtUser, 0, 0);
        formGrid.add(txtPass, 0, 1);

        GridPane.setHgrow(txtUser, Priority.ALWAYS);
        GridPane.setHgrow(txtPass, Priority.ALWAYS);

        // Buton (Turuncu)
        Button btnLogin = new Button("GİRİŞ YAP");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPadding(new Insets(12));
        btnLogin.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnLogin.setStyle(
                "-fx-background-color: #558B2F;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        btnLogin.setDefaultButton(true);

        btnLogin.setOnAction(e -> {
            // 1. Şifre kontrolü (Şimdilik her şeye izin veriyoruz)
            // İleride buraya if(txtUser.getText().equals("admin")...) eklenecek.

            System.out.println("Giriş Başarılı: " + txtUser.getText());

            // 2. SAYFA DEĞİŞTİRME SİHRİ
            // Butonun bulunduğu sahneyi al
            Scene currentScene = btnLogin.getScene();

            // Sahnenin kökünü 'AnaMenu' ile değiştir
            // (Bu işlem pencereyi kapatmadan içeriği değiştirir)
            currentScene.setRoot(new MainMenuView());

            // İsterseniz pencere boyutunu da büyütebilirsiniz:
            // currentScene.getWindow().setWidth(1024);
            // currentScene.getWindow().setHeight(768);
        });

        rightSide.getChildren().addAll(lblGirisBaslik, formGrid, btnLogin);

        // =================================================================
        // BİRLEŞTİRME
        // =================================================================
        // Sol ve Sağ tarafı yatay kartın içine koyuyoruz
        loginCard.getChildren().addAll(leftSide, rightSide);

        // Kartı ana panele (StackPane) ekliyoruz
        this.getChildren().add(loginCard);
    }
}