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
import java.util.logging.Level;
import java.util.logging.Logger;

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
        LOGGER.info("StockController baslatiliyor");
        stockTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Product selectedProduct = stockTable.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    updateProductStock(selectedProduct);
                }
            }
        });
        // --- YENİ EKLENEN KISIM: SAĞ TIK MENÜSÜ ---
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Ürün Bilgilerini Düzenle");
        editItem.setOnAction(event -> editSelectedProduct());

        MenuItem deleteItem = new MenuItem("Ürünü Sil");
        deleteItem.setOnAction(event -> deleteSelectedProduct());

        contextMenu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        stockTable.setContextMenu(contextMenu);

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
        nameField.setPromptText("Örn: Türk Kahvesi");

        ComboBox<String> mainCategoryCombo = new ComboBox<>();
        mainCategoryCombo.getItems().addAll("İçecek", "Yiyecek", "Tatlı", "Diğer");
        mainCategoryCombo.setPromptText("Ana Kategori Seçin");

        ComboBox<String> subCategoryCombo = new ComboBox<>();
        subCategoryCombo.setPromptText("Önce ana kategori seçin");
        subCategoryCombo.setDisable(true);

        mainCategoryCombo.setOnAction(e -> {
            subCategoryCombo.getItems().clear();
            String selected = mainCategoryCombo.getValue();

            if ("İçecek".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sıcak İçecek", "Soğuk İçecek", "Alkollü İçecek", "Alkolsüz İçecek");
            } else if ("Yiyecek".equals(selected)) {
                subCategoryCombo.getItems().addAll("Ana Yemek", "Ara Sıcak", "Aperitif", "Salata");
            } else if ("Tatlı".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sütlü Tatlı", "Şerbetli Tatlı", "Pasta", "Dondurma");
            } else if ("Diğer".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sigara", "Nargile", "Diğer");
            }

            subCategoryCombo.setDisable(false);
            subCategoryCombo.setPromptText("Alt Kategori Seçin");
        });

        ComboBox<String> detailCategoryCombo = new ComboBox<>();
        detailCategoryCombo.setPromptText("Detay kategori (opsiyonel)");
        detailCategoryCombo.setDisable(true);

        subCategoryCombo.setOnAction(e -> {
            detailCategoryCombo.getItems().clear();
            String mainCat = mainCategoryCombo.getValue();
            String subCat = subCategoryCombo.getValue();

            if ("İçecek".equals(mainCat) && "Sıcak İçecek".equals(subCat)) {
                detailCategoryCombo.getItems().addAll("Kahve", "Çay", "Bitki Çayi", "Sıcak Çikolata");
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

        TextField priceField = new TextField();
        priceField.setPromptText("Fiyat (TL)");

        TextField packageField = new TextField();
        packageField.setPromptText("Örn: 5 (paket)");
        packageField.setText("1");

        TextField portionField = new TextField();
        portionField.setPromptText("Örn: 200 (bardak/paket)");
        portionField.setText("1");

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

                    String fullCategory = mainCat + " > " + subCat;
                    if (detailCat != null && !detailCat.isEmpty()) {
                        fullCategory += " > " + detailCat;
                    }

                    boolean success = productDAO.addProduct(name, fullCategory, price, stockPackage, unit, portionsPerPackage);

                    if (success) {
                        int totalStock = stockPackage * portionsPerPackage;
                        LOGGER.info("Yeni ürün eklendi: " + name + " (" + fullCategory + ") - "
                                + stockPackage + " paket x " + portionsPerPackage + " = " + totalStock + " " + unit);
                        loadStockData();

                        showAlert(Alert.AlertType.INFORMATION, "Başarılı",
                                "Ürün başarıyla eklendi!\n\n"
                                + "Ürün: " + name + "\n"
                                + "Kategori: " + fullCategory + "\n"
                                + "Stok: " + stockPackage + " paket x " + portionsPerPackage
                                + " = " + totalStock + " " + unit);
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Fiyat, paket sayısı ve porsiyon sayısı sayısal değer olmalıdır!");
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Hata", e.getMessage());
                    LOGGER.log(Level.WARNING, "Ürün eklenirken hata", e);
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
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen silmek istediğiniz ürünü seçin.");
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
                LOGGER.info("Ürün silindi: " + selectedProduct.getName());
                loadStockData();
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Ürün başarıyla silindi!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Hata", "Ürün silinirken bir hata oluştu!");
            }
        }
    }

    private void editSelectedProduct() {
        Product selectedProduct = stockTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen düzenlemek istediğiniz ürünü seçin.");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Ürün Düzenle");
        dialog.setHeaderText("Ürün Bilgilerini Güncelle");

        ButtonType saveButtonType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedProduct.getName());
        TextField priceField = new TextField(String.valueOf(selectedProduct.getPrice()));

        TextField categoryField = new TextField(selectedProduct.getCategory());

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("adet", "porsiyon", "fincan", "bardak", "tabak", "dilim", "kg", "gr");
        unitCombo.setValue(selectedProduct.getUnit());

        grid.add(new Label("Ürün Adı:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Kategori:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Fiyat (TL):"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Birim:"), 0, 3);
        grid.add(unitCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String category = categoryField.getText().trim();
                    String unit = unitCombo.getValue();
                    double price = Double.parseDouble(priceField.getText().trim());

                    if (name.isEmpty() || category.isEmpty() || unit == null) {
                        throw new IllegalArgumentException("Alanlar boş bırakılamaz!");
                    }

                    // Veritabanı güncelleme işlemi
                    boolean success = productDAO.updateProductDetails(selectedProduct.getId(), name, category, price, unit);

                    if (success) {
                        loadStockData(); // Tabloyu yenile
                        showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Ürün bilgileri güncellendi!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Hata", "Güncelleme başarısız oldu.");
                    }

                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Fiyat geçerli bir sayı olmalıdır!");
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Hata", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateProductStock(Product product) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(product.getStockPackage()));

        dialog.setTitle("Paket Sayısı Güncelle");
        dialog.setHeaderText(product.getName() + " - Stok Durumu");
        dialog.setContentText("Mevcut Paket Sayısı:\n(Dikkat: Girdiğiniz sayı paket sayısı olarak işlenecektir!)");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStock -> {
            try {
                int stockPackageAmount = Integer.parseInt(newStock.trim());
                if (stockPackageAmount < 0) {
                    throw new IllegalArgumentException("Paket sayısı negatif olamaz!");
                }

                boolean success = productDAO.updateProductStock(product.getId(), stockPackageAmount);

                if (success) {
                    int totalUnits = stockPackageAmount * product.getPortionsPerPackage();
                    LOGGER.info("Stok güncellendi: " + stockPackageAmount + " paket (" + totalUnits + " adet)");
                    loadStockData();

                    showAlert(Alert.AlertType.INFORMATION, "Stok Güncellendi",
                            "Yeni Stok Durumu:\n" +
                            stockPackageAmount + " Paket\n" +
                            "Toplam: " + totalUnits + " " + product.getUnit());
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Hata", "Lütfen geçerli bir sayı girin!");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Hata", e.getMessage());
            }
        });
    }

    private void loadStockData() {
        try {
            LOGGER.info("Stok verileri yükleniyor...");

            productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

            List<Product> productList = productDAO.getAllProducts();

            if (productList != null) {
                LOGGER.info(productList.size() + " ürün bulundu");
                ObservableList<Product> products = FXCollections.observableArrayList(productList);
                stockTable.setItems(products);
                LOGGER.info("Stok tablosu başarıyla dolduruldu");
            } else {
                LOGGER.warning("Ürün listesi boş");
                stockTable.setItems(FXCollections.observableArrayList());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Stok verileri yüklenirken hata", e);
            showAlert(Alert.AlertType.ERROR, "Hata", "Stok verileri yüklenirken bir hata oluştu.");
        }
    }

    @FXML
    public void goBackToMenu() {
        LOGGER.info("Ana menüye gidiliyor...");
        HelloApplication.changeScene("main-menu.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

