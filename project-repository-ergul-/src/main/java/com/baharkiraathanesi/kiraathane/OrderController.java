package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.OrderDAO;
import com.baharkiraathanesi.kiraathane.dao.ProductDAO;
import com.baharkiraathanesi.kiraathane.model.Product;
import com.baharkiraathanesi.kiraathane.model.OrderItem;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class OrderController {

    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    @FXML
    private Label tableLabel;

    @FXML
    private FlowPane productsContainer;

    @FXML
    private ListView<String> orderListView;

    @FXML
    private Label totalLabel;

    private int currentTableId;
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final Map<String, OrderItem> orderItemMap = new HashMap<>();

    public void setTableInfo(int tableId, String tableName) {
        this.currentTableId = tableId;
        this.tableLabel.setText(tableName + " Siparis Ekrani");

        loadProducts();
        refreshOrderList();
    }

    private void loadProducts() {
        productsContainer.getChildren().clear();
        List<Product> products = productDAO.getAllProducts();

        for (Product p : products) {
            boolean isOutOfStock = p.getStockQty() <= 0;

            String btnText = p.getName() + "\n" + p.getPrice() + " TL";
            if (isOutOfStock) {
                btnText = p.getName() + "\n(TUKENDI)";
            }

            Button btn = new Button(btnText);
            btn.setPrefSize(120, 80);
            btn.setTextAlignment(TextAlignment.CENTER);
            btn.setAlignment(Pos.CENTER);
            btn.setWrapText(true);

            if (isOutOfStock) {
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: #333333; -fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-font-weight: bold;");
            } else {
                btn.setStyle("-fx-background-radius: 8; -fx-cursor: hand;");

                btn.setOnAction(e -> {
                    boolean success = orderDAO.addProductToOrder(currentTableId, p.getId(), 1);

                    if (success) {
                        refreshOrderList();
                        if (p.getStockQty() - 1 <= 0) {
                            loadProducts();
                        }
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Stok Yetersiz", p.getName() + " tukenmistir!");
                        loadProducts();
                    }
                });
            }

            productsContainer.getChildren().add(btn);
        }
    }

    private void refreshOrderList() {
        orderListView.getItems().clear();
        orderItemMap.clear();

        List<OrderItem> items = orderDAO.getOrderItems(currentTableId);
        double total = 0;

        for (OrderItem item : items) {
            String displayText = item.getProductName() + " x" + item.getQuantity() +
                    " - " + String.format("%.2f", item.getSubtotal()) + " TL";
            orderListView.getItems().add(displayText);
            orderItemMap.put(displayText, item);
            total += item.getSubtotal();
        }

        totalLabel.setText("Toplam: " + String.format("%.2f", total) + " TL");
    }

    @FXML
    public void closeCheck() {
        List<OrderItem> items = orderDAO.getOrderItems(currentTableId);

        if (items.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyari", "Masada siparis bulunmuyor!");
            return;
        }

        orderDAO.closeOrder(currentTableId);

        showAlert(Alert.AlertType.INFORMATION, "Odeme", "Hesap kapatildi ve tahsil edildi!");
        LOGGER.info("Hesap kapatildi: Masa ID=" + currentTableId);

        goBack();
    }

    @FXML
    public void goBack() {
        HelloApplication.changeScene("tables-view.fxml");
    }

    @FXML
    public void deleteSelectedItem() {
        String selectedText = orderListView.getSelectionModel().getSelectedItem();
        if (selectedText != null) {
            OrderItem selectedItem = orderItemMap.get(selectedText);
            if (selectedItem != null) {
                orderDAO.removeOrderItem(selectedItem.getId(), selectedItem.getProductId(), selectedItem.getQuantity());
                refreshOrderList();
                loadProducts();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Uyari", "Lutfen silmek icin bir urun secin!");
        }
    }

    @FXML
    public void clearAllItems() {
        List<OrderItem> items = orderDAO.getOrderItems(currentTableId);

        if (items.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyari", "Masada zaten siparis bulunmuyor!");
            return;
        }

        for (OrderItem item : items) {
            orderDAO.removeOrderItem(item.getId(), item.getProductId(), item.getQuantity());
        }

        refreshOrderList();
        loadProducts();

        showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Tum siparisler temizlendi!");
        LOGGER.info("Tum siparisler temizlendi: Masa ID=" + currentTableId);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

