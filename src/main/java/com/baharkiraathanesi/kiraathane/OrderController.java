package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.OrderDAO;
import com.baharkiraathanesi.kiraathane.dao.ProductDAO;
import com.baharkiraathanesi.kiraathane.model.Product;
import com.baharkiraathanesi.kiraathane.model.OrderItem;
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
        refreshOrderList(); // Var olan siparişi veritabanından yükle
    }

    private void loadProducts() {
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            Button btn = new Button(p.getName() + "\n" + p.getPrice() + " TL");
            btn.setPrefSize(100, 60);

            // Ürüne tıklayınca siparişe ekle
            btn.setOnAction(e -> {
                orderDAO.addProductToOrder(currentTableId, p.getId(), 1);
                // Veritabanından tekrar yükle
                refreshOrderList();
            });

            productsContainer.getChildren().add(btn);
        }
    }

    private void updateTotal(double priceToAdd) {
        currentTotal += priceToAdd;
        totalLabel.setText("Toplam: " + String.format("%.2f", currentTotal) + " TL");
    }

    // Artık veritabanından masanın açık sipariş kalemlerini çekecek
    private void refreshOrderList() {
        orderListView.getItems().clear();
        currentTotal = 0;

        List<OrderItem> items = orderDAO.getOrderItemsForTable(currentTableId);
        if (items != null) {
            for (OrderItem it : items) {
                orderListView.getItems().add(it.toString());
                currentTotal += it.getPrice() * it.getQuantity();
            }
        }
        totalLabel.setText("Toplam: " + String.format("%.2f", currentTotal) + " TL");
    }

    @FXML
    public void closeCheck() {
        // Kapatmadan önce sipariş kalemi var mı kontrol et
        List<OrderItem> items = orderDAO.getOrderItemsForTable(currentTableId);
        if (items != null && !items.isEmpty()) {
            // Ödeme alınıp siparişler ödenmediği sürece kapatma engellenir
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Ödeme Onayı");
            alert.setHeaderText("Hesabı kapatmak üzeresiniz");
            alert.setContentText("Hesabı kapatmak istediğinize emin misiniz? İşlem masayı boşaltır.");

            java.util.Optional<javafx.scene.control.ButtonType> res = alert.showAndWait();
            if (res.isPresent() && res.get() == javafx.scene.control.ButtonType.OK) {
                orderDAO.closeOrder(currentTableId);
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Ödeme");
                info.setHeaderText(null);
                info.setContentText("Hesap kapatıldı ve tahsil edildi!");
                info.showAndWait();
                goBack();
            }
        } else {
            // Eğer zaten kalem yoksa doğrudan kapat
            orderDAO.closeOrder(currentTableId);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ödeme");
            alert.setHeaderText(null);
            alert.setContentText("Hesap kapatıldı ve tahsil edildi!");
            alert.showAndWait();
            goBack();
        }
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
            // Selected string format: "productName xQ (price TL)" as defined in OrderItem.toString()
            // We need a robust way: fetch items and match by string to find orderItemId
            List<OrderItem> items = orderDAO.getOrderItemsForTable(currentTableId);
            OrderItem target = null;
            for (OrderItem it : items) {
                if (it.toString().equals(selectedItem)) {
                    target = it;
                    break;
                }
            }

            if (target != null) {
                boolean ok = orderDAO.deleteOrderItem(target.getId());
                if (ok) {
                    refreshOrderList();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setHeaderText(null);
                    alert.setContentText("Öğe veritabanından silinemedi.");
                    alert.showAndWait();
                }
            }
        }
    }

    @FXML
    public void clearAllItems() {
        // Bu uygulamada tüm kalemleri tek tek silmek karmaşık olabilir; kullanıcıyı uyar
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hepsini Temizle");
        alert.setHeaderText("Tüm sipariş kalemlerini silmek üzeresiniz");
        alert.setContentText("Bu işlem geri alınamaz. Onaylıyor musunuz?");

        java.util.Optional<javafx.scene.control.ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == javafx.scene.control.ButtonType.OK) {
            // Basit yöntem: hesabı kapat ve dolaylı olarak kalemleri temizle
            orderDAO.closeOrder(currentTableId);
            refreshOrderList();
        }
    }
}