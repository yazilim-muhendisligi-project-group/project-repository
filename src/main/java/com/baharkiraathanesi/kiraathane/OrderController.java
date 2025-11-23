package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.OrderDAO;
import com.baharkiraathanesi.kiraathane.dao.ProductDAO;
import com.baharkiraathanesi.kiraathane.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class OrderController {
    @FXML private Label tableLabel;
    @FXML private FlowPane productsContainer;
    @FXML private ListView<String> orderListView;
    @FXML private Label totalLabel;

    private int currentTableId;
    private OrderDAO orderDAO = new OrderDAO();
    private ProductDAO productDAO = new ProductDAO();

    // Basitlik olsun diye toplam tutarı burada tutuyoruz
    private double currentTotal = 0;

    // Masalar ekranından buraya gelince çağrılır
    public void setTableInfo(int tableId, String tableName) {
        this.currentTableId = tableId;
        this.tableLabel.setText(tableName + " Sipariş Ekranı");

        loadProducts();
        refreshOrderList(); // Var olan siparişi yükle (Şimdilik boş gelecek)
    }

    private void loadProducts() {
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            Button btn = new Button(p.getName() + "\n" + p.getPrice() + " TL");
            btn.setPrefSize(100, 60);

            // Ürüne tıklayınca siparişe ekle
            btn.setOnAction(e -> {
                orderDAO.addProductToOrder(currentTableId, p.getId(), 1);
                // Listeye görsel olarak ekle (Normalde veritabanından tekrar çekmek daha doğru ama hızlı çözüm)
                orderListView.getItems().add(p.getName() + " - " + p.getPrice() + " TL");
                updateTotal(p.getPrice());
            });

            productsContainer.getChildren().add(btn);
        }
    }

    private void updateTotal(double priceToAdd) {
        currentTotal += priceToAdd;
        totalLabel.setText("Toplam: " + currentTotal + " TL");
    }

    // Geçici çözüm: Sayfa ilk açıldığında veritabanından eski siparişi çekme kodu
    // Şimdilik boş bırakıyoruz, eklediğin an görünür.
    private void refreshOrderList() {
        orderListView.getItems().clear();
        currentTotal = 0;
        totalLabel.setText("Toplam: 0.00 TL");
        // Burayı geliştirebilirsin: orderDAO.getOrderItems(tableId) ile dolabilir.
    }

    @FXML
    public void closeCheck() {
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
        // changeScene expects a resource filename relative to / (resources root).
        // Pass only the FXML filename that exists under src/main/resources.
        HelloApplication.changeScene("tables-view.fxml");
    }

    @FXML
    public void deleteSelectedItem() {
        String selectedItem = orderListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            orderListView.getItems().remove(selectedItem);
            // Extract price from the selected item and update the total
            double price = Double.parseDouble(selectedItem.split(" - ")[1].replace(" TL", ""));
            updateTotal(-price);
        }
    }

    @FXML
    public void clearAllItems() {
        orderListView.getItems().clear();
        currentTotal = 0;
        totalLabel.setText("Toplam: 0.00 TL");
    }
}