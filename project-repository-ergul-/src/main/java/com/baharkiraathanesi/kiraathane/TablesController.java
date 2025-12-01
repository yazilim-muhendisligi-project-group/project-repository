package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.TableDAO;
import com.baharkiraathanesi.kiraathane.model.Table;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TablesController {

    @FXML private FlowPane tablesContainer;

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
                    // "Masa Özel" gibi sayı içermeyen isimleri yoksay
                }
            }
        }
        String newTableName = "Masa " + (maxNumber + 1);
        boolean success = tableDAO.addTable(newTableName);

        if (success) {
            System.out.println("✅ Otomatik masa eklendi: " + newTableName);
            refreshTables();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Başarılı");
            alert.setHeaderText(null);
            alert.setContentText(newTableName + " başarıyla oluşturuldu.");

            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText(null);
            alert.setContentText("Masa eklenirken bir hata oluştu!");
            alert.showAndWait();
        }
    }

    private void refreshTables() {
        tablesContainer.getChildren().clear();
        TableDAO tableDAO = new TableDAO();
        List<Table> tables = tableDAO.getAllTables();

        for (Table t : tables) {
            String color = t.isOccupied() ? "#D32F2F" : "#2196F3";

            String statusText = t.isOccupied() ? "\n(DOLU)" : "\n(BOŞ)";

            Button btn = new Button(t.getName() + statusText);
            btn.setPrefSize(120, 80);
            btn.setTextAlignment(TextAlignment.CENTER);
            btn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8;");

            btn.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                    openOrderScreen(t.getId(), t.getName());
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    deleteTable(t.getId(), t.getName(), t.isOccupied());
                }
            });

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
        confirmAlert.setHeaderText("Masayı silmek istediğinize emin misiniz?");
        confirmAlert.setContentText(tableName + " silinecek. Bu işlem geri alınamaz!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TableDAO tableDAO = new TableDAO();
            boolean success = tableDAO.deleteTable(tableId);

            if (success) {
                System.out.println("✅ Masa silindi: " + tableName);
                refreshTables();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hata");
                alert.setHeaderText(null);
                alert.setContentText("Masa silinirken bir hata oluştu!");
                alert.showAndWait();
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
            e.printStackTrace();
        }
    }
}