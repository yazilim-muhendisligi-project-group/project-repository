package erciyes.edu.tr.bahar19.View;

import erciyes.edu.tr.bahar19.MockDataUtility;
import erciyes.edu.tr.bahar19.Model.Order;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportView extends BorderPane {

    private TableView<Order> reportTable;
    private Label lblTotalRevenue;

    public ReportView() {
        initView();
        loadReportData();
    }

    private void initView() {
        this.setStyle("-fx-background-color: #ECEFF1;");

        // 1. ÜST KISIM (HEADER)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #455A64;");

        Label lblTitle = new Label("Z Raporu (Gün Sonu)");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        header.getChildren().add(lblTitle);
        this.setTop(header);

        // 2. ÖZET VE TABLO (MERKEZ)
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));

        lblTotalRevenue = new Label("Toplam Gelir: 0.00 TL");
        lblTotalRevenue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblTotalRevenue.setTextFill(Color.web("#2E7D32"));

        reportTable = createReportTable();

        centerContent.getChildren().addAll(lblTotalRevenue, reportTable);
        VBox.setVgrow(reportTable, javafx.scene.layout.Priority.ALWAYS);
        this.setCenter(centerContent);

        // 3. ALT KISIM (Çıktı Butonu)
        HBox footer = createFooter();
        this.setBottom(footer);
    }

    private TableView<Order> createReportTable() {
        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- Sütun Tanımlamaları ---

        TableColumn<Order, Integer> tableNumCol = new TableColumn<>("Masa No");
        tableNumCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));

        TableColumn<Order, Double> amountCol = new TableColumn<>("Tutar (TL)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Sipariş ID'sini göstermek için (İsteğe bağlı)
        TableColumn<Order, Integer> idCol = new TableColumn<>("Sipariş ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        table.getColumns().addAll(idCol, tableNumCol, amountCol);
        return table;
    }

    private void loadReportData() {
        List<Order> orders = MockDataUtility.getCompletedOrders();
        ObservableList<Order> data = FXCollections.observableArrayList(orders);
        reportTable.setItems(data);

        // Toplam geliri hesapla
        double total = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        lblTotalRevenue.setText(String.format("Toplam Gelir: %.2f TL", total));
    }

    private HBox createFooter() {
        HBox footer = new HBox(40);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        // Ana Menü Butonu
        Button btnBack = new Button("⬅ Ana Menü");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #455A64; -fx-font-size: 14px; -fx-border-color: #455A64; -fx-border-radius: 5;");
        btnBack.setOnAction(e -> {
            Scene scene = this.getScene();
            scene.setRoot(new MainMenuView());
        });

        // Çıktı Al Butonu
        Button btnExport = new Button("📄 PDF Çıktı Al");
        btnExport.setPrefWidth(200);
        btnExport.setPadding(new Insets(10));
        btnExport.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnExport.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        btnExport.setOnAction(e -> exportReport(reportTable)); // Raporu dışa aktar

        // Butonları hizalama
        HBox left = new HBox(btnBack);
        HBox center = new HBox(btnExport);
        HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(center, javafx.scene.layout.Priority.ALWAYS);
        left.setAlignment(Pos.CENTER_LEFT);
        center.setAlignment(Pos.CENTER);

        footer.getChildren().addAll(left, center);
        return footer;
    }

    // PDF/Çıktı Simülasyonu Metodu
    private void exportReport(TableView<Order> table) {
        // Gerçek dünyada bu, iText veya Apache POI gibi kütüphanelerle PDF/Excel oluşturur.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Z Raporu Çıktısı");
        alert.setHeaderText("Rapor Dışa Aktarılıyor");
        alert.setContentText(String.format("Z Raporu (Toplam: %.2f TL) başarıyla PDF olarak simüle edildi. Gerçek projede dosya kaydetme penceresi açılırdı.",
                table.getItems().stream().mapToDouble(Order::getTotalAmount).sum()));
        alert.showAndWait();
        System.out.println("Rapor Çıktısı: PDF Export Simülasyonu Tamamlandı.");
    }
}