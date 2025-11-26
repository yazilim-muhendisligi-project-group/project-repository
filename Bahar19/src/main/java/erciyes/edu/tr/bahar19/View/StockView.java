// erciyes.edu.tr.bahar19.View.StockView.java
package erciyes.edu.tr.bahar19.View;

import erciyes.edu.tr.bahar19.MockDataUtility;
import erciyes.edu.tr.bahar19.Model.InventoryItem; // Yeni model sınıfı
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

public class StockView extends BorderPane {

    private TableView<InventoryItem> stockTable; // InventoryItem kullanılacak

    public StockView() {
        initView();
    }

    private void initView() {
        this.setStyle("-fx-background-color: #ECEFF1;");

        // 1. ÜST KISIM (HEADER)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #B71C1C;");

        Label lblTitle = new Label("Stok Takip Paneli");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        header.getChildren().add(lblTitle);
        this.setTop(header);

        // 2. ORTA KISIM (TableView)
        stockTable = createStockTable();

        VBox centerBox = new VBox(10, stockTable);
        centerBox.setPadding(new Insets(20));
        this.setCenter(centerBox);

        // 3. ALT KISIM (Butonlar)
        HBox footer = createFooter();
        this.setBottom(footer);

        loadStockData();
    }

    private TableView<InventoryItem> createStockTable() {
        TableView<InventoryItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setEditable(false);

        // --- Sütun Tanımlamaları ---
        // PropertyValueFactory, InventoryItem sınıfındaki getName, getCurrentStock, vb. metotlarını kullanır.

        TableColumn<InventoryItem, String> nameCol = new TableColumn<>("Ürün Adı");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<InventoryItem, Integer> stockCol = new TableColumn<>("Mevcut Stok");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("currentStock"));

        TableColumn<InventoryItem, String> unitCol = new TableColumn<>("Birim");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<InventoryItem, Integer> criticalCol = new TableColumn<>("Kritik");
        criticalCol.setCellValueFactory(new PropertyValueFactory<>("criticalLevel"));

        table.getColumns().addAll(nameCol, stockCol, unitCol, criticalCol);
        return table;
    }

    private void loadStockData() {
        // MockDataUtility'den YENİ ENVANTER listesini al
        List<InventoryItem> inventoryList = MockDataUtility.getMockInventoryList();
        ObservableList<InventoryItem> data = FXCollections.observableArrayList(inventoryList);
        stockTable.setItems(data);
    }

    private HBox createFooter() {
        HBox footer = new HBox(40);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        // Ana Menü Butonu (Sol Tarafta)
        Button btnBack = new Button("⬅ Ana Menü");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #455A64; -fx-font-size: 14px; -fx-border-color: #455A64; -fx-border-radius: 5;");
        btnBack.setOnAction(e -> {
            Scene scene = this.getScene();
            scene.setRoot(new MainMenuView());
        });

        // Stok Güncelle Butonu (Ortada)
        Button btnUpdateStock = new Button("🔄 Stok Güncelle");
        btnUpdateStock.setPrefWidth(200);
        btnUpdateStock.setPadding(new Insets(10));
        btnUpdateStock.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnUpdateStock.setStyle("-fx-background-color: #EF6C00; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        btnUpdateStock.setOnAction(e -> {
            System.out.println("Stok güncelleme işlemi simüle ediliyor.");
            loadStockData();
        });

        // Butonları hizalama
        HBox left = new HBox(btnBack);
        HBox center = new HBox(btnUpdateStock);
        HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(center, javafx.scene.layout.Priority.ALWAYS);
        left.setAlignment(Pos.CENTER_LEFT);
        center.setAlignment(Pos.CENTER);

        footer.getChildren().addAll(left, center);
        return footer;
    }
}