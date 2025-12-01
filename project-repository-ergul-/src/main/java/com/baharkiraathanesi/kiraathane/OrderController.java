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

public class OrderController {
    @FXML private Label tableLabel;
    @FXML private FlowPane productsContainer;
    @FXML private ListView<String> orderListView;
    @FXML private Label totalLabel;

    private int currentTableId;
    private OrderDAO orderDAO = new OrderDAO();
    private ProductDAO productDAO = new ProductDAO();

    // Sipariş kalemlerini String ile eşleştirmek için map
    private Map<String, OrderItem> orderItemMap = new HashMap<>();

    // Masalar ekranından buraya gelince çağrılır
    public void setTableInfo(int tableId, String tableName) {
        this.currentTableId = tableId;
        this.tableLabel.setText(tableName + " Sipariş Ekranı");

        loadProducts();
        refreshOrderList();
    }

    private void loadProducts() {
        productsContainer.getChildren().clear();
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            // --- STOK KONTROLÜ ---
            boolean isOutOfStock = p.getStockQty() <= 0;

            String btnText = p.getName() + "\n" + p.getPrice() + " TL";
            if (isOutOfStock) {
                btnText = p.getName() + "\n(TÜKENDİ)";
            }

            Button btn = new Button(btnText);
            btn.setPrefSize(120, 80);

            btn.setTextAlignment(TextAlignment.CENTER);
            btn.setAlignment(Pos.CENTER);
            btn.setWrapText(true);

            // Eğer stok bittiyse butonu pasif yap ve grileştir
            if (isOutOfStock) {
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: #333333; -fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-font-weight: bold;");
            } else {
                // Stok varsa normal görünüm
                btn.setStyle("-fx-background-radius: 8; -fx-cursor: hand;");

                btn.setOnAction(e -> {
                    // Veritabanı kontrolü ile ekleme yap (Çünkü o an başka masa son ürünü almış olabilir)
                    boolean success = orderDAO.addProductToOrder(currentTableId, p.getId(), 1);

                    if (success) {
                        refreshOrderList();
                        // Eğer bu işlemle stok bittiyse (son ürünü aldıysak) ekranı yenile ki buton pasif olsun
                        if (p.getStockQty() - 1 <= 0) {
                            loadProducts();
                        }
                    } else {
                        // Stok yetersizse (başka masa bizden önce almışsa) uyarı ver
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Stok Yetersiz");
                        alert.setHeaderText(null);
                        alert.setContentText(p.getName() + " tükenmiştir!");
                        alert.showAndWait();

                        // Ekranı yenile (Buton pasif olsun)
                        loadProducts();
                    }
                });
            }

            productsContainer.getChildren().add(btn);
        }
    }

    // Masanın mevcut siparişlerini veritabanından yükle
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText(null);
            alert.setContentText("Masada sipariş bulunmuyor!");
            alert.showAndWait();
            return;
        }

        orderDAO.closeOrder(currentTableId);

        // Bilgi ver
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ödeme");
        alert.setHeaderText(null);
        alert.setContentText("Hesap kapatıldı ve tahsil edildi!");
        alert.showAndWait();

        goBack(); // Masalara dön
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
                // Veritabanından sil
                orderDAO.removeOrderItem(selectedItem.getId(), selectedItem.getProductId(), selectedItem.getQuantity());
                // Listeyi güncelle
                refreshOrderList();
                // Ürün silinince stok geri geleceği için butonları yenile (Gri olan açılabilir)
                loadProducts();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText(null);
            alert.setContentText("Lütfen silmek için bir ürün seçin!");
            alert.showAndWait();
        }
    }

    @FXML
    public void clearAllItems() {
        List<OrderItem> items = orderDAO.getOrderItems(currentTableId);

        if (items.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText(null);
            alert.setContentText("Masada zaten sipariş bulunmuyor!");
            alert.showAndWait();
            return;
        }

        // Tüm kalemleri tek tek sil
        for (OrderItem item : items) {
            orderDAO.removeOrderItem(item.getId(), item.getProductId(), item.getQuantity());
        }

        refreshOrderList();
        // Stoklar geri geldiği için butonları yenile
        loadProducts();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText("Tüm siparişler temizlendi!");
        alert.showAndWait();
    }
}