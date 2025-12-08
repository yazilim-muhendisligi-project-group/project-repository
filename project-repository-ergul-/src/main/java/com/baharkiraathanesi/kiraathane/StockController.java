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

        Platform.runLater(this::loadStockData);
    }

    @FXML
    private void addNewProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Yeni Urun Ekle");
        dialog.setHeaderText("Yeni Urun Bilgilerini Girin");

        ButtonType addButtonType = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Orn: Turk Kahvesi");

        ComboBox<String> mainCategoryCombo = new ComboBox<>();
        mainCategoryCombo.getItems().addAll("Icecek", "Yiyecek", "Tatli", "Diger");
        mainCategoryCombo.setPromptText("Ana Kategori Secin");

        ComboBox<String> subCategoryCombo = new ComboBox<>();
        subCategoryCombo.setPromptText("Once ana kategori secin");
        subCategoryCombo.setDisable(true);

        mainCategoryCombo.setOnAction(e -> {
            subCategoryCombo.getItems().clear();
            String selected = mainCategoryCombo.getValue();

            if ("Icecek".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sicak Icecek", "Soguk Icecek", "Alkollu Icecek", "Alkolsuz Icecek");
            } else if ("Yiyecek".equals(selected)) {
                subCategoryCombo.getItems().addAll("Ana Yemek", "Ara Sicak", "Aperatif", "Salata");
            } else if ("Tatli".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sutlu Tatli", "Serbetli Tatli", "Pasta", "Dondurma");
            } else if ("Diger".equals(selected)) {
                subCategoryCombo.getItems().addAll("Sigara", "Nargile", "Diger");
            }

            subCategoryCombo.setDisable(false);
            subCategoryCombo.setPromptText("Alt Kategori Secin");
        });

        ComboBox<String> detailCategoryCombo = new ComboBox<>();
        detailCategoryCombo.setPromptText("Detay kategori (opsiyonel)");
        detailCategoryCombo.setDisable(true);

        subCategoryCombo.setOnAction(e -> {
            detailCategoryCombo.getItems().clear();
            String mainCat = mainCategoryCombo.getValue();
            String subCat = subCategoryCombo.getValue();

            if ("Icecek".equals(mainCat) && "Sicak Icecek".equals(subCat)) {
                detailCategoryCombo.getItems().addAll("Kahve", "Cay", "Bitki Cayi", "Sicak Cikolata");
                detailCategoryCombo.setDisable(false);
            } else if ("Icecek".equals(mainCat) && "Soguk Icecek".equals(subCat)) {
                detailCategoryCombo.getItems().addAll("Mesrubat", "Meyve Suyu", "Smoothie", "Soguk Kahve");
                detailCategoryCombo.setDisable(false);
            } else if ("Yiyecek".equals(mainCat) && "Ana Yemek".equals(subCat)) {
                detailCategoryCombo.getItems().addAll("Et Yemekleri", "Tavuk Yemekleri", "Balik", "Vejeteryan");
                detailCategoryCombo.setDisable(false);
            } else {
                detailCategoryCombo.setDisable(true);
                detailCategoryCombo.setPromptText("Detay kategori yok");
            }
        });

        TextField priceField = new TextField();
        priceField.setPromptText("Fiyat (TL)");

        TextField packageField = new TextField();
        packageField.setPromptText("Orn: 5 (paket)");
        packageField.setText("1");

        TextField portionField = new TextField();
        portionField.setPromptText("Orn: 200 (bardak/paket)");
        portionField.setText("1");

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("adet", "porsiyon", "fincan", "bardak", "tabak", "dilim", "kg", "gr");
        unitCombo.setPromptText("Birim Secin");
        unitCombo.setValue("adet");

        grid.add(new Label("Urun Adi:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Ana Kategori:"), 0, 1);
        grid.add(mainCategoryCombo, 1, 1);
        grid.add(new Label("Alt Kategori:"), 0, 2);
        grid.add(subCategoryCombo, 1, 2);
        grid.add(new Label("Detay Kategori:"), 0, 3);
        grid.add(detailCategoryCombo, 1, 3);
        grid.add(new Label("Fiyat (TL):"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(new Label("Paket Sayisi:"), 0, 5);
        grid.add(packageField, 1, 5);
        grid.add(new Label("Paket Basina Porsiyon:"), 0, 6);
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
                        throw new IllegalArgumentException("Zorunlu alanlar doldurulmalidir!");
                    }

                    if (stockPackage <= 0 || portionsPerPackage <= 0) {
                        throw new IllegalArgumentException("Paket ve porsiyon sayisi pozitif olmalidir!");
                    }

                    String fullCategory = mainCat + " > " + subCat;
                    if (detailCat != null && !detailCat.isEmpty()) {
                        fullCategory += " > " + detailCat;
                    }

                    boolean success = productDAO.addProduct(name, fullCategory, price, stockPackage, unit, portionsPerPackage);

                    if (success) {
                        int totalStock = stockPackage * portionsPerPackage;
                        LOGGER.info("Yeni urun eklendi: " + name + " (" + fullCategory + ") - "
                                + stockPackage + " paket x " + portionsPerPackage + " = " + totalStock + " " + unit);
                        loadStockData();

                        showAlert(Alert.AlertType.INFORMATION, "Basarili",
                                "Urun basariyla eklendi!\n\n"
                                + "Urun: " + name + "\n"
                                + "Kategori: " + fullCategory + "\n"
                                + "Stok: " + stockPackage + " paket x " + portionsPerPackage
                                + " = " + totalStock + " " + unit);
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Fiyat, paket sayisi ve porsiyon sayisi sayisal deger olmalidir!");
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Hata", e.getMessage());
                    LOGGER.log(Level.WARNING, "Urun eklenirken hata", e);
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
            showAlert(Alert.AlertType.WARNING, "Uyari", "Lutfen silmek istediginiz urunu secin.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Urun Sil");
        confirmAlert.setHeaderText("Urunu silmek istediginize emin misiniz?");
        confirmAlert.setContentText(selectedProduct.getName() + " silinecek. Bu islem geri alinamaz!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = productDAO.deleteProduct(selectedProduct.getId());

            if (success) {
                LOGGER.info("Urun silindi: " + selectedProduct.getName());
                loadStockData();
                showAlert(Alert.AlertType.INFORMATION, "Basarili", "Urun basariyla silindi!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Hata", "Urun silinirken bir hata olustu!");
            }
        }
    }

    private void updateProductStock(Product product) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(product.getStockPackage()));

        dialog.setTitle("Paket Sayisi Guncelle");
        dialog.setHeaderText(product.getName() + " - Stok Durumu");
        dialog.setContentText("Mevcut Paket Sayisi:\n(Dikkat: Girdiginiz sayi paket sayisi olarak islenecektir!)");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStock -> {
            try {
                int stockPackageAmount = Integer.parseInt(newStock.trim());
                if (stockPackageAmount < 0) {
                    throw new IllegalArgumentException("Paket sayisi negatif olamaz!");
                }

                boolean success = productDAO.updateProductStock(product.getId(), stockPackageAmount);

                if (success) {
                    int totalUnits = stockPackageAmount * product.getPortionsPerPackage();
                    LOGGER.info("Stok guncellendi: " + stockPackageAmount + " paket (" + totalUnits + " adet)");
                    loadStockData();

                    showAlert(Alert.AlertType.INFORMATION, "Stok Guncellendi",
                            "Yeni Stok Durumu:\n" +
                            stockPackageAmount + " Paket\n" +
                            "Toplam: " + totalUnits + " " + product.getUnit());
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Hata", "Lutfen gecerli bir sayi girin!");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Hata", e.getMessage());
            }
        });
    }

    private void loadStockData() {
        try {
            LOGGER.info("Stok verileri yukleniyor...");

            productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

            List<Product> productList = productDAO.getAllProducts();

            if (productList != null) {
                LOGGER.info(productList.size() + " urun bulundu");
                ObservableList<Product> products = FXCollections.observableArrayList(productList);
                stockTable.setItems(products);
                LOGGER.info("Stok tablosu basariyla dolduruldu");
            } else {
                LOGGER.warning("Urun listesi bos");
                stockTable.setItems(FXCollections.observableArrayList());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Stok verileri yuklenirken hata", e);
            showAlert(Alert.AlertType.ERROR, "Hata", "Stok verileri yuklenirken bir hata olustu.");
        }
    }

    @FXML
    public void goBackToMenu() {
        LOGGER.info("Ana menuye gidiliyor...");
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

