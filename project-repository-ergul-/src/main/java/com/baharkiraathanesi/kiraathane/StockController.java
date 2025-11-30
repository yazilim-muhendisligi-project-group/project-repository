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
import java.util.logging.Logger;

/**
 * Stok yönetimi kontrolcüsü
 * Ürün ekleme, silme ve stok güncelleme işlemlerini yönetir
 */
public class StockController {

    private static final Logger LOGGER = Logger.getLogger(StockController.class.getName());

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

        // Ürün adı
        TextField nameField = new TextField();
        nameField.setPromptText("Örn: Türk Kahvesi");

        // Ana kategori seçimi
        ComboBox<String> mainCategoryCombo = new ComboBox<>();
        mainCategoryCombo.getItems().addAll("İçecek", "Yiyecek", "Tatlı", "Diğer");
        mainCategoryCombo.setPromptText("Ana Kategori Seçin");

        // Alt kategori seçimi
        ComboBox<String> subCategoryCombo = new ComboBox<>();
        subCategoryCombo.setPromptText("Önce ana kategori seçin");
        subCategoryCombo.setDisable(true);

        // Ana kategori değişince alt kategorileri güncelle
        mainCategoryCombo.setOnAction(e -> {
            subCategoryCombo.getItems().clear();
            String selected = mainCategoryCombo.getValue();

            if ("İçecek".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sıcak İçecek", "Soğuk İçecek", "Alkollü İçecek", "Alkolsüz İçecek");
            } else if ("Yiyecek".equals(selected)) {
                subCategoryCombo.getItems().addAll("Ana Yemek", "Ara Sıcak", "Aperatif", "Salata");
            } else if ("Tatlı".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sütlü Tatlı", "Şerbetli Tatlı", "Pasta", "Dondurma");
            } else if ("Diğer".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sigara", "Nargile", "Diğer");
            }

            subCategoryCombo.setDisable(false);
            subCategoryCombo.setPromptText("Alt Kategori Seçin");
        });

        // Detay kategori (opsiyonel)
        ComboBox<String> detailCategoryCombo = new ComboBox<>();
        detailCategoryCombo.setPromptText("Detay kategori (opsiyonel)");
        detailCategoryCombo.setDisable(true);

        // Alt kategori değişince detay kategorileri güncelle
        subCategoryCombo.setOnAction(e -> {
            detailCategoryCombo.getItems().clear();
            String mainCat = mainCategoryCombo.getValue();
            String subCat = subCategoryCombo.getValue();

            if ("İçecek".equals(mainCat) && "Sıcak İçecek".equals(subCat)) {
                detailCategoryCombo.getItems().addAll("Kahve", "Çay", "Bitki Çayı", "Sıcak Çikolata");
                detailCategoryCombo.setDisable(false);
            } else if ("İçecek".equals(mainCat) && "Soğuk İçecek".equals(subCat)) {
                detailCategoryCombo.getItems().addAll("Meşrubat", "Meyve Suyu", "Smoothie", "Soğuk Kahve");
                detailCategoryCombo.setDisable(false);
            } else if ("Yiyecek".equals(mainCat) && "Ana Yemek".equals(subCat)) {
                detailCategoryCombo.getItems().addAll("Et Yemekleri", "Tavuk Yemekleri", "Balık", "Vejeteryan");
                detailCategoryCombo.setDisable(false);
            } else {
                detailCategoryCombo.setDisable(true);
                detailCategoryCombo.setPromptText("Detay kategori yok");
            }
        });

        // Fiyat
        TextField priceField = new TextField();
        priceField.setPromptText("Fiyat (TL)");

        // Paket sayısı (YENİ SİSTEM)
        TextField packageField = new TextField();
        packageField.setPromptText("Örn: 5 (paket)");
        packageField.setText("1");

        // Paket başına porsiyon (YENİ SİSTEM)
        TextField portionField = new TextField();
        portionField.setPromptText("Örn: 200 (bardak/paket)");
        portionField.setText("1");

        // Birim seçimi
        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("adet", "porsiyon", "fincan", "bardak", "tabak", "dilim", "kg", "gr");
        unitCombo.setPromptText("Birim Seçin");
        unitCombo.setValue("adet");

        grid.add(new Label("Ürün Adı:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Ana Kategori:"), 0, 1);
        grid.add(mainCategoryCombo, 1, 1);
        grid.add(new Label("Alt Kategori:"), 0, 2);
        grid.add(subCategoryCombo, 1, 2);
        grid.add(new Label("Detay Kategori:"), 0, 3);
        grid.add(detailCategoryCombo, 1, 3);
        grid.add(new Label("Fiyat (TL):"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(new Label("Paket Sayısı:"), 0, 5);
        grid.add(packageField, 1, 5);
        grid.add(new Label("Paket Başına Porsiyon:"), 0, 6);
        grid.add(portionField, 1, 6);
        grid.add(new Label("Birim:"), 0, 7);
        grid.add(unitCombo, 1, 7);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String mainCat = mainCategoryCombo.getValue();
                    String subCat = subCategoryCombo.getValue();
                    String detailCat = detailCategoryCombo.getValue();
                    double price = Double.parseDouble(priceField.getText().trim());
                    int stockPackage = Integer.parseInt(packageField.getText().trim());
                    int portionsPerPackage = Integer.parseInt(portionField.getText().trim());
                    String unit = unitCombo.getValue();

                    if (name.isEmpty() || mainCat == null || subCat == null || unit == null) {
                        throw new IllegalArgumentException("Zorunlu alanlar doldurulmalıdır!");
                    }

                    if (stockPackage <= 0 || portionsPerPackage <= 0) {
                        throw new IllegalArgumentException("Paket ve porsiyon sayısı pozitif olmalıdır!");
                    }

                    // Kategoriyi birleştir: "İçecek > Sıcak İçecek > Kahve"
                    String fullCategory = mainCat + " > " + subCat;
                    if (detailCat != null && !detailCat.isEmpty()) {
                        fullCategory += " > " + detailCat;
                    }

                    // YENİ SİSTEM: 6 parametre (name, category, price, stockPackage, unit, portionsPerPackage)
                    boolean success = productDAO.addProduct(name, fullCategory, price, stockPackage, unit, portionsPerPackage);

                    if (success) {
                        int totalStock = stockPackage * portionsPerPackage;
                        LOGGER.info("✅ Yeni ürün eklendi: " + name + " (" + fullCategory + ") - "
                                   + stockPackage + " paket × " + portionsPerPackage + " = " + totalStock + " " + unit);
                        loadStockData();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Başarılı");
                        alert.setContentText("Ürün başarıyla eklendi!\n\n"
                                           + "Ürün: " + name + "\n"
                                           + "Kategori: " + fullCategory + "\n"
                                           + "Stok: " + stockPackage + " paket × " + portionsPerPackage
                                           + " = " + totalStock + " " + unit);
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setContentText("Fiyat, paket sayısı ve porsiyon sayısı sayısal değer olmalıdır!");
                    alert.showAndWait();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setContentText("Hata: " + e.getMessage());
                    alert.showAndWait();
                    LOGGER.info("❌ Ürün eklenirken hata: " + e.getMessage());
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
            LOGGER.info("Stok verileri yükleniyor...");

            // Sütunları konfigür et
            productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

            // Veritabanından ürünleri çek
            List<Product> productList = productDAO.getAllProducts();

            if (productList != null) {
                LOGGER.info("✅ " + productList.size() + " ürün bulundu");

                // ObservableList'e dönüştür
                ObservableList<Product> products = FXCollections.observableArrayList(productList);

                // Tabloya ekle
                stockTable.setItems(products);
                LOGGER.info("✅ Stok tablosu başarıyla dolduruldu!");
            } else {
                LOGGER.info("⚠️ Ürün listesi NULL!");
                stockTable.setItems(FXCollections.observableArrayList());
            }
        } catch (Exception e) {
            LOGGER.info("❌ StockController Hatası: " + e.getMessage());
        }
    }

    @FXML
    public void goBackToMenu() {
        System.out.println("Ana menüye gidiliyor...");
        HelloApplication.changeScene("main-menu.fxml");
    }
}
