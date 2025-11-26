package erciyes.edu.tr.bahar19.View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TablesView extends BorderPane {
    private FlowPane tablesGrid; // Masaların dizileceği alan
    private int tableCount = 0;  // Masa sayacı

    public TablesView() {
        initView();
    }

    private void initView() {
        // GENEL AYARLAR
        this.setStyle("-fx-background-color: #ECEFF1;");

        // 1. ÜST KISIM (HEADER & GERİ BUTONU)
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #B71C1C; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 0, 0, 0, 5);");

        Button btnBack = new Button("⬅ Geri");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 5;");
        btnBack.setOnAction(e -> {
            // Ana Menüye Dönüş
            Scene scene = this.getScene();
            scene.setRoot(new MainMenuView());
        });

        Label lblTitle = new Label("Masalar Yönetimi");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        header.getChildren().addAll(btnBack, lblTitle);
        this.setTop(header);

        // 2. ORTA KISIM (MASALAR GRİDİ)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true); // Genişliğe uydur
        scrollPane.setStyle("-fx-background-color: transparent;");

        tablesGrid = new FlowPane();
        tablesGrid.setPadding(new Insets(20));
        tablesGrid.setHgap(20); // Yatay boşluk
        tablesGrid.setVgap(20); // Dikey boşluk
        tablesGrid.setAlignment(Pos.TOP_LEFT);
        tablesGrid.setStyle("-fx-background-color: #ECEFF1;");

        // Başlangıçta 10 Masa Ekle
        for (int i = 1; i <= 10; i++) {
            addTableCard();
        }

        scrollPane.setContent(tablesGrid);
        this.setCenter(scrollPane);

        // 3. ALT KISIM (MASA EKLE BUTONU)
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button btnAddTable = new Button("+ Yeni Masa Ekle");
        btnAddTable.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnAddTable.setPrefWidth(200);
        btnAddTable.setStyle(
                "-fx-background-color: #2E7D32;" + // Yeşil
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );

        // Butona basınca yeni masa üret
        btnAddTable.setOnAction(e -> addTableCard());

        footer.getChildren().add(btnAddTable);
        this.setBottom(footer);
    }

    // --- MASA KARTI OLUŞTURMA METODU ---
    private void addTableCard() {
        tableCount++;
        int currentNumber = tableCount; // Lambda içinde kullanmak için

        // KART (VBOX)
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefSize(160, 160);
        card.setAlignment(Pos.CENTER);
        // Varsayılan Stil (Boş Masa - Yeşilimsi Çerçeve)
        String defaultStyle = "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-border-color: #4CAF50; -fx-border-radius: 10; -fx-border-width: 2;";
        card.setStyle(defaultStyle);

        // BAŞLIK
        Label lblMasaNo = new Label("MASA " + currentNumber);
        lblMasaNo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblMasaNo.setTextFill(Color.web("#37474F"));

        // RADIO BUTTONLAR (DOLU / BOŞ)
        ToggleGroup group = new ToggleGroup();
        RadioButton rbBos = new RadioButton("Boş");
        RadioButton rbDolu = new RadioButton("Dolu");

        rbBos.setToggleGroup(group);
        rbDolu.setToggleGroup(group);
        rbBos.setSelected(true); // Varsayılan boş

        // Radio Button Renk Değişimi Mantığı
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbDolu) {
                // Doluysa Kırmızı Çerçeve ve Hafif Kırmızı Arka Plan
                card.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-border-color: #D32F2F; -fx-border-radius: 10; -fx-border-width: 2;");
            } else {
                // Boşsa Yeşil Çerçeve ve Beyaz Arka Plan
                card.setStyle(defaultStyle);
            }
        });

        HBox radioBox = new HBox(10, rbBos, rbDolu);
        radioBox.setAlignment(Pos.CENTER);

        // GİT BUTONU (Detay Ekranına Geçiş)
        Button btnGit = new Button("Siparişe Git >");
        btnGit.setStyle("-fx-background-color: #B71C1C; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        btnGit.setOnAction(e -> {
            System.out.println("Masa " + currentNumber + " detayına gidiliyor...");
            Scene scene = this.getScene();
            if (scene != null) {
                scene.setRoot(new OrderView(currentNumber));
            }
        });

        // Karta elemanları ekle
        card.getChildren().addAll(lblMasaNo, radioBox, btnGit);

        // Gride kartı ekle
        tablesGrid.getChildren().add(card);
    }
}