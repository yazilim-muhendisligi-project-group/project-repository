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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Masa Ekle");
        dialog.setHeaderText("Yeni Masa Oluştur");
        dialog.setContentText("Masa adını girin:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(tableName -> {
            if (!tableName.trim().isEmpty()) {
                TableDAO tableDAO = new TableDAO();
                boolean success = tableDAO.addTable(tableName.trim());

                if (success) {
                    System.out.println("✅ Yeni masa eklendi: " + tableName);
                    refreshTables();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Başarılı");
                    alert.setHeaderText(null);
                    alert.setContentText("Masa başarıyla eklendi!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setHeaderText(null);
                    alert.setContentText("Masa eklenirken bir hata oluştu!");
                    alert.showAndWait();
                }
            }
        });
    }

    private void refreshTables() {
        tablesContainer.getChildren().clear();
        TableDAO tableDAO = new TableDAO();
        List<Table> tables = tableDAO.getAllTables();

        for (Table t : tables) {
            Button btn = new Button(t.getName());
            btn.setPrefSize(120, 80);
            btn.setStyle("-fx-font-size: 16px; -fx-background-color: " +
                    (t.isOccupied() ? "#ff4444;" : "#4CAF50;") +
                    "-fx-text-fill: white;");

            // Sol tık: Masaya git
            btn.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                    openOrderScreen(t.getId(), t.getName());
                }
                // Sağ tık: Masayı sil
                else if (e.getButton() == MouseButton.SECONDARY) {
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

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Başarılı");
                alert.setHeaderText(null);
                alert.setContentText("Masa başarıyla silindi!");
                alert.showAndWait();
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