package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.TableDAO;
import com.baharkiraathanesi.kiraathane.model.Table;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TablesController {

    private static final Logger LOGGER = Logger.getLogger(TablesController.class.getName());

    @FXML
    private FlowPane tablesContainer;

    @FXML
    public void initialize() {
        refreshTables();
    }

    @FXML
    private void goBack() {
        HelloApplication.changeScene("main-menu.fxml");
    }

    @FXML
    private void addNewTable() {
        TableDAO tableDAO = new TableDAO();
        List<Table> tables = tableDAO.getAllTables();

        int maxNumber = 0;
        for (Table t : tables) {
            String name = t.getName();
            if (name != null && name.startsWith("Masa ")) {
                try {
                    String numberPart = name.substring(5).trim();
                    int num = Integer.parseInt(numberPart);
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.fine("Masa numarası parse edilemedi: " + name);
                }
            }
        }
        String newTableName = "Masa " + (maxNumber + 1);
        boolean success = tableDAO.addTable(newTableName);

        if (success) {
            LOGGER.info("Otomatik masa eklendi: " + newTableName);
            refreshTables();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Başarılı");
            alert.setHeaderText(null);
            alert.setContentText(newTableName + " başarıyla oluşturuldu.");
            alert.showAndWait();
        } else {
            showErrorAlert("Masa eklenirken bir hata oluştu!");
        }
    }

    private void refreshTables() {
        tablesContainer.getChildren().clear();
        TableDAO tableDAO = new TableDAO();
        List<Table> tables = tableDAO.getAllTables();

        for (Table t : tables) {
            String color = t.isOccupied() ? "#D32F2F" : "#2196F3"; // Kırmızı : Mavi
            String statusText = t.isOccupied() ? "\n(DOLU)" : "\n(BOŞ)";

            Button btn = new Button(t.getName() + statusText);
            btn.setPrefSize(120, 80);
            btn.setTextAlignment(TextAlignment.CENTER);
            btn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");

            // Tıklama Olayı (Sol tık sipariş açar)
            btn.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    openOrderScreen(t.getId(), t.getName());
                }
            });

            // Sağ Tık Menüsü Oluştur
            ContextMenu contextMenu = new ContextMenu();

            // SADECE DOLU MASALAR TAŞINABİLİR
            if (t.isOccupied()) {
                MenuItem moveItem = new MenuItem("Masayı Taşı / Birleştir");
                moveItem.setOnAction(e -> showMoveTableDialog(t));
                contextMenu.getItems().add(moveItem);

                contextMenu.getItems().add(new SeparatorMenuItem());
            }

            // SADECE BOŞ MASALAR SİLİNEBİLİR (Veya yönetici ise hepsi)
            MenuItem deleteItem = new MenuItem("Masayı Sil");
            deleteItem.setOnAction(e -> deleteTable(t.getId(), t.getName(), t.isOccupied()));

            contextMenu.getItems().add(deleteItem);

            // Menüyü butona bağla
            btn.setContextMenu(contextMenu);

            tablesContainer.getChildren().add(btn);
        }
    }

    private void deleteTable(int tableId, String tableName, boolean isOccupied) {
        if (isOccupied) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText("Masa Dolu!");
            alert.setContentText("Açık siparişi olan masa silinemez. Önce hesabı kapatın.");
            alert.showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Masa Sil");
        confirmAlert.setHeaderText("Masayi silmek istediğinize emin misiniz?");
        confirmAlert.setContentText(tableName + " silinecek. Bu işlem geri alınamaz!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TableDAO tableDAO = new TableDAO();
            boolean success = tableDAO.deleteTable(tableId);

            if (success) {
                LOGGER.info("Masa silindi: " + tableName);
                refreshTables();
            } else {
                showErrorAlert("Masa silinirken bir hata oluştu!");
            }
        }
    }

    private void openOrderScreen(int tableId, String tableName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("order-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            OrderController controller = loader.getController();
            controller.setTableInfo(tableId, tableName);

            Stage stage = (Stage) tablesContainer.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Sipariş ekranı açılamadı", e);
            showErrorAlert("Sipariş ekranı açılamadı. Lütfen tekrar deneyin.");
        }
    }

    private void showMoveTableDialog(Table sourceTable) {
        TableDAO tableDAO = new TableDAO();
        List<Table> allTables = tableDAO.getAllTables();

        // Hedef listesi oluştur (Kendisi hariç diğer masalar)
        List<Table> targetOptions = new java.util.ArrayList<>();
        for (Table t : allTables) {
            if (t.getId() != sourceTable.getId()) {
                targetOptions.add(t);
            }
        }

        if (targetOptions.isEmpty()) {
            showErrorAlert("Taşınacak başka masa yok!");
            return;
        }

        // Diyalog Penceresi
        ChoiceDialog<Table> dialog = new ChoiceDialog<>(targetOptions.get(0), targetOptions);
        dialog.setTitle("Masa Taşıma");
        dialog.setHeaderText(sourceTable.getName() + " taşınıyor...");
        dialog.setContentText("Hedef Masayı Seçin:");

        // Tablo nesnesinin adını göstermek için (toString metodu zaten var ama garanti olsun)
        // Table sınıfındaki toString() metodunun "Masa Adı [DURUM]" döndürdüğünden emin ol.

        Optional<Table> result = dialog.showAndWait();

        result.ifPresent(targetTable -> {
            boolean confirm = true;

            // Eğer hedef doluysa uyarı ver (Birleştirme onayı)
            if (targetTable.isOccupied()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Masa Birleştirme");
                confirmAlert.setHeaderText("Dikkat: Hedef Masa Dolu!");
                confirmAlert.setContentText(targetTable.getName() + " zaten dolu.\n" +
                        sourceTable.getName() + " buraya birleştirilecek.\nOnaylıyor musunuz?");

                Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
                if (!confirmResult.isPresent() || confirmResult.get() != ButtonType.OK) {
                    confirm = false;
                }
            }

            if (confirm) {
                com.baharkiraathanesi.kiraathane.dao.OrderDAO orderDAO = new com.baharkiraathanesi.kiraathane.dao.OrderDAO();
                boolean success = orderDAO.moveOrderToTable(sourceTable.getId(), targetTable.getId());

                if (success) {
                    refreshTables(); // Ekranı yenile

                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Başarılı");
                    info.setHeaderText(null);
                    info.setContentText("Masa başarıyla taşındı.");
                    info.showAndWait();
                } else {
                    showErrorAlert("Taşıma işlemi sırasında hata oluştu!");
                }
            }
        });
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

