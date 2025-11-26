package erciyes.edu.tr.bahar19.View;

import erciyes.edu.tr.bahar19.MockDataUtility;
import erciyes.edu.tr.bahar19.Model.Order;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.text.DecimalFormat;

public class PaymentView extends BorderPane {

    private final int tableNumber;
    private final double totalAmount;

    private TextField txtPaidAmount;
    private Label lblChangeAmount;
    private Button btnFinish;

    // Para formatı için yardımcı
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    public PaymentView(int tableNumber, double totalAmount) {
        this.tableNumber = tableNumber;
        this.totalAmount = totalAmount; // Ödenecek toplam tutar
        initView();
    }

    private void initView() {
        this.setStyle("-fx-background-color: #ECEFF1;");

        // 1. ÜST KISIM (HEADER)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #B71C1C;");

        Label lblTitle = new Label("Hesap Kesme: Masa " + tableNumber);
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        header.getChildren().add(lblTitle);
        this.setTop(header);

        // 2. ORTA KISIM (Ödeme Kartı)
        VBox paymentCard = createPaymentCard();
        this.setCenter(paymentCard);
        BorderPane.setAlignment(paymentCard, Pos.CENTER);

        // 3. ALT KISIM (Geri Dönüş Butonu)
        HBox footer = createFooter();
        this.setBottom(footer);
    }

    private VBox createPaymentCard() {
        VBox card = new VBox(25); // Elemanlar arası boşluk
        card.setPadding(new Insets(40));
        card.setMaxWidth(450);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        // --- TOPLAM TUTAR GÖSTERGESİ ---
        Label lblTotalDisplay = new Label("ÖDENECEK TOPLAM");
        lblTotalDisplay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTotalDisplay.setTextFill(Color.GRAY);

        Label lblAmount = new Label(df.format(totalAmount) + " TL");
        lblAmount.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        lblAmount.setTextFill(Color.web("#B71C1C")); // Kırmızı

        // --- FORM (GridPane) ---
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(20);

        // Ödenen Tutar Alanı
        Label lblPaid = new Label("Ödenen Tutar (TL):");
        lblPaid.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        txtPaidAmount = new TextField();
        txtPaidAmount.setPromptText("Müşteriden alınan tutarı giriniz");
        txtPaidAmount.setFont(Font.font("Segoe UI", 14));
        txtPaidAmount.setPrefWidth(250);

        // Para Üstü Alanı
        Label lblChange = new Label("Para Üstü (TL):");
        lblChange.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        lblChangeAmount = new Label("0,00 TL");
        lblChangeAmount.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblChangeAmount.setTextFill(Color.web("#2E7D32")); // Yeşil

        formGrid.add(lblPaid, 0, 0);
        formGrid.add(txtPaidAmount, 1, 0);
        formGrid.add(lblChange, 0, 1);
        formGrid.add(lblChangeAmount, 1, 1);

        // --- HESAPLAMA OLAYI ---
        txtPaidAmount.textProperty().addListener((obs, oldVal, newVal) -> calculateChange(newVal));

        // --- İŞLEMİ TAMAMLA BUTONU ---
        btnFinish = new Button("İŞLEMİ TAMAMLA ve MASAYI KAPAT");
        btnFinish.setMaxWidth(Double.MAX_VALUE);
        btnFinish.setPadding(new Insets(15));
        btnFinish.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        btnFinish.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        btnFinish.setDisable(true); // Başlangıçta inaktif

        btnFinish.setOnAction(e -> finishPayment());

        card.getChildren().addAll(lblTotalDisplay, lblAmount, formGrid, btnFinish);
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private void calculateChange(String input) {
        try {
            double paid = Double.parseDouble(input.replace(',', '.')); // Virgülü noktaya çevir
            double change = paid - totalAmount;

            if (change >= 0) {
                lblChangeAmount.setText(df.format(change) + " TL");
                lblChangeAmount.setTextFill(Color.web("#2E7D32")); // Yeşil
                btnFinish.setDisable(false); // Ödeme yeterliyse butonu aktifleştir
            } else {
                lblChangeAmount.setText("Eksik: " + df.format(Math.abs(change)) + " TL");
                lblChangeAmount.setTextFill(Color.web("#D32F2F")); // Kırmızı
                btnFinish.setDisable(true); // Eksik ödeme varsa inaktif
            }
        } catch (NumberFormatException e) {
            lblChangeAmount.setText("Geçersiz Tutar");
            lblChangeAmount.setTextFill(Color.GRAY);
            btnFinish.setDisable(true);
        }
    }

    private void finishPayment() {
        // MOCK CONTROLLER GÖREVİ:
        // 1. Siparişi veritabanından/MockMap'ten sil (Kapat)
        MockDataUtility.closeOrder(tableNumber);

        System.out.println("Masa " + tableNumber + " hesabı kapatıldı. Masalar ekranına dönülüyor.");

        // TablesView'a geri dön
        Scene scene = this.getScene();
        scene.setRoot(new TablesView());
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button btnBack = new Button("⬅ Geri");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #455A64; -fx-font-size: 14px; -fx-border-color: #455A64; -fx-border-radius: 5;");
        btnBack.setOnAction(e -> {
            // Bir önceki OrderView'a geri dön
            Scene scene = this.getScene();
            scene.setRoot(new OrderView(tableNumber));
        });

        footer.getChildren().add(btnBack);
        return footer;
    }
}