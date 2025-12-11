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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.logging.Logger;

public class OrderController {

    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    @FXML
    private Label tableLabel;

    @FXML
    private FlowPane productsContainer;

    @FXML
    private VBox orderItemsContainer;

    @FXML
    private Label totalLabel;

    private int currentTableId;
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();

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
        orderItemsContainer.getChildren().clear();

        List<OrderItem> items = orderDAO.getOrderItems(currentTableId);
        double total = 0;

        for (OrderItem item : items) {
            HBox card = createOrderItemCard(item);
            orderItemsContainer.getChildren().add(card);
            total += item.getSubtotal();
        }

        totalLabel.setText("Toplam: " + String.format("%.2f", total) + " TL");
    }

    /**
     * Her sipariş kalemi için kompakt görsel kart oluşturur.
     * Sabit genişlikli kolonlar ile stabil tasarım.
     */
    private HBox createOrderItemCard(OrderItem item) {
        HBox card = new HBox(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 6; -fx-padding: 8 10;");

        // Ürün adı - genişletilmiş alan
        Label nameLabel = new Label(item.getProductName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        nameLabel.setPrefWidth(115);
        nameLabel.setMinWidth(115);
        nameLabel.setMaxWidth(115);
        nameLabel.setEllipsisString("...");
        nameLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);

        // Buton grubu - kompakt
        HBox buttonGroup = new HBox(3);
        buttonGroup.setAlignment(Pos.CENTER);
        buttonGroup.setPrefWidth(80);
        buttonGroup.setMinWidth(80);
        buttonGroup.setMaxWidth(80);

        // Azalt butonu
        Button minusBtn = new Button("−");
        minusBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-min-width: 26; -fx-max-width: 26; -fx-min-height: 26; -fx-max-height: 26; " +
                "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 0;");
        minusBtn.setOnAction(e -> {
            orderDAO.decreaseItemQuantity(item.getId(), item.getProductId());
            refreshOrderList();
            loadProducts();
        });

        // Miktar - sabit genişlik
        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        qtyLabel.setPrefWidth(24);
        qtyLabel.setMinWidth(24);
        qtyLabel.setMaxWidth(24);
        qtyLabel.setAlignment(Pos.CENTER);

        // Artır butonu
        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-min-width: 26; -fx-max-width: 26; -fx-min-height: 26; -fx-max-height: 26; " +
                "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 0;");
        plusBtn.setOnAction(e -> {
            boolean success = orderDAO.addProductToOrder(currentTableId, item.getProductId(), 1);
            if (success) {
                refreshOrderList();
                loadProducts();
            } else {
                showAlert(Alert.AlertType.WARNING, "Stok Yetersiz", item.getProductName() + " için yeterli stok yok!");
            }
        });

        buttonGroup.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        // Fiyat - daraltılmış, sağa hizalı
        Label priceLabel = new Label(String.format("%.2f ₺", item.getSubtotal()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
        priceLabel.setPrefWidth(60);
        priceLabel.setMinWidth(60);
        priceLabel.setMaxWidth(60);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(nameLabel, buttonGroup, priceLabel);
        return card;
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

