package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.ProductDAO;
import com.baharkiraathanesi.kiraathane.model.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

public class StockController {

    @FXML
    private TableView<Product> stockTable;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, Integer> stockQuantityColumn;

    @FXML
    private TableColumn<Product, String> unitColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        System.out.println("=== StockController Başlatılıyor ===");

        // Çift tıklama ile stok güncelleme
        stockTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Product selectedProduct = stockTable.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    updateProductStock(selectedProduct);
                }
            }
        });

        Platform.runLater(this::loadStockData);
    }

    @FXML
    private void addNewProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Yeni Ürün Ekle");
        dialog.setHeaderText("Yeni Ürün Bilgilerini Girin");

        ButtonType addButtonType = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Ürün Adı");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Kategori (örn: İçecek)");
        TextField priceField = new TextField();
        priceField.setPromptText("Fiyat");
        TextField stockField = new TextField();
        stockField.setPromptText("Başlangıç Stok Miktarı");
        TextField unitField = new TextField();
        unitField.setPromptText("Birim (örn: Adet, Porsiyon)");

        grid.add(new Label("Ürün Adı:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Kategori:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Fiyat (TL):"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stok Miktarı:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Birim:"), 0, 4);
        grid.add(unitField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String category = categoryField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    int stock = Integer.parseInt(stockField.getText().trim());
                    String unit = unitField.getText().trim();

                    if (name.isEmpty() || category.isEmpty() || unit.isEmpty()) {
                        throw new IllegalArgumentException("Tüm alanlar doldurulmalıdır!");
                    }

                    boolean success = productDAO.addProduct(name, category, price, stock, unit);
                    if (success) {
                        System.out.println("✅ Yeni ürün eklendi: " + name);
                        loadStockData();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Başarılı");
                        alert.setContentText("Ürün başarıyla eklendi!");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setContentText("Fiyat ve stok sayısal değer olmalıdır!");
                    alert.showAndWait();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setContentText("Hata: " + e.getMessage());
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void deleteSelectedProduct() {
        Product selectedProduct = stockTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText("Ürün Seçilmedi");
            alert.setContentText("Lütfen silmek istediğiniz ürünü seçin.");
            alert.showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Ürün Sil");
        confirmAlert.setHeaderText("Ürünü silmek istediğinize emin misiniz?");
        confirmAlert.setContentText(selectedProduct.getName() + " silinecek. Bu işlem geri alınamaz!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = productDAO.deleteProduct(selectedProduct.getId());

            if (success) {
                System.out.println("✅ Ürün silindi: " + selectedProduct.getName());
                loadStockData();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Başarılı");
                alert.setContentText("Ürün başarıyla silindi!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hata");
                alert.setContentText("Ürün silinirken bir hata oluştu!");
                alert.showAndWait();
            }
        }
    }

    private void updateProductStock(Product product) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(product.getStockQty()));
        dialog.setTitle("Stok Güncelle");
        dialog.setHeaderText(product.getName() + " için yeni stok miktarı");
        dialog.setContentText("Yeni stok miktarı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStock -> {
            try {
                int stockAmount = Integer.parseInt(newStock.trim());
                if (stockAmount < 0) {
                    throw new IllegalArgumentException("Stok miktarı negatif olamaz!");
                }

                boolean success = productDAO.updateProductStock(product.getId(), stockAmount);
                if (success) {
                    System.out.println("✅ Stok güncellendi: " + product.getName() + " -> " + stockAmount);
                    loadStockData();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Başarılı");
                    alert.setContentText("Stok başarıyla güncellendi!");
                    alert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hata");
                alert.setContentText("Geçerli bir sayı girin!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hata");
                alert.setContentText("Hata: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void loadStockData() {
        try {
            System.out.println("Stok verileri yükleniyor...");

            // Sütunları konfigür et
            productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

            // Veritabanından ürünleri çek
            List<Product> productList = productDAO.getAllProducts();

            if (productList != null) {
                System.out.println("✅ " + productList.size() + " ürün bulundu");

                // ObservableList'e dönüştür
                ObservableList<Product> products = FXCollections.observableArrayList(productList);

                // Tabloya ekle
                stockTable.setItems(products);
                System.out.println("✅ Stok tablosu başarıyla dolduruldu!");
            } else {
                System.out.println("❌ Ürün listesi NULL!");
                stockTable.setItems(FXCollections.observableArrayList());
            }
        } catch (Exception e) {
            System.err.println("❌ StockController Hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goBackToMenu() {
        System.out.println("Ana menüye gidiliyor...");
        HelloApplication.changeScene("main-menu.fxml");
    }
}
