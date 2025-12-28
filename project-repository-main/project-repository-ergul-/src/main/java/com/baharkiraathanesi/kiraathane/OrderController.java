package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.OrderDAO;
import com.baharkiraathanesi.kiraathane.dao.ProductDAO;
import com.baharkiraathanesi.kiraathane.model.Product;
import com.baharkiraathanesi.kiraathane.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.logging.Logger;

public class OrderController {

    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    @FXML
    private Label tableLabel;

    @FXML
    private TabPane categoryTabPane;

    @FXML
    private VBox orderItemsContainer;

    @FXML
    private Label totalLabel;

    private int currentTableId;
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();

    public void setTableInfo(int tableId, String tableName) {
        this.currentTableId = tableId;
        this.tableLabel.setText(tableName + " Sipariş Ekranı");

        loadProducts();
        refreshOrderList();
    }

    private void loadProducts() {
        if (categoryTabPane == null) return;

        categoryTabPane.getTabs().clear();
        List<Product> products = productDAO.getAllProducts();

        Map<String, List<Product>> categoryMap = new LinkedHashMap<>();

        for (Product p : products) {
            String fullCategory = p.getCategory();
            String tabName = "Diğer";

            if (fullCategory != null && !fullCategory.isEmpty()) {
                String[] parts = fullCategory.split(">");
                for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();

                if (parts.length > 1) {
                    String root = parts[0];
                    if (root.equalsIgnoreCase("Icecek") || root.equalsIgnoreCase("İçecek") ||
                            root.equalsIgnoreCase("Yiyecek") ||
                            root.equalsIgnoreCase("Tatli") || root.equalsIgnoreCase("Tatlı")) {

                        tabName = parts[1];
                    } else {
                        tabName = parts[0];
                    }
                } else {
                    tabName = parts[0];
                }
            }
            categoryMap.computeIfAbsent(tabName, k -> new ArrayList<>()).add(p);
        }

        for (Map.Entry<String, List<Product>> entry : categoryMap.entrySet()) {
            String categoryName = entry.getKey();
            List<Product> productList = entry.getValue();

            Tab tab = new Tab(categoryName);
            FlowPane tabContent = new FlowPane();
            tabContent.setHgap(10); tabContent.setVgap(10); tabContent.setPadding(new Insets(10));
            tabContent.setAlignment(Pos.TOP_LEFT);
            tabContent.setStyle("-fx-background-color: #ffffff;");

            ScrollPane scrollPane = new ScrollPane(tabContent);
            scrollPane.setFitToWidth(true); scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent;");

            for (Product p : productList) {
                tabContent.getChildren().add(createProductButton(p));
            }
            tab.setContent(scrollPane);
            categoryTabPane.getTabs().add(tab);
        }
    }

    private Button createProductButton(Product p) {

        String btnText = p.getName() + "\n" + p.getPrice() + " TL";
        Button btn = new Button(btnText);
        btn.setPrefSize(120, 80);
        btn.setTextAlignment(TextAlignment.CENTER);
        btn.setAlignment(Pos.CENTER);
        btn.setWrapText(true);
        btn.setStyle("-fx-background-radius: 8; -fx-cursor: hand; -fx-base: #f0f0f0;");

        btn.setOnAction(e -> {
            boolean success = orderDAO.addProductToOrder(currentTableId, p.getId(), 1);
            if (success) {
                refreshOrderList();
            } else {
                showAlert(Alert.AlertType.WARNING, "Hata", "Ürün eklenemedi!");
            }
        });

        return btn;
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

    private HBox createOrderItemCard(OrderItem item) {
        HBox card = new HBox(4);
        card.setAlignment(Pos.CENTER_LEFT);
        String bgColor = (item.getPrice() == 0) ? "#fff9c4" : "#f5f5f5";
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 6; -fx-padding: 8 10;");

        Label nameLabel = new Label(item.getProductName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        nameLabel.setPrefWidth(115);
        nameLabel.setMinWidth(115);
        nameLabel.setMaxWidth(115);
        nameLabel.setEllipsisString("...");
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

        if (item.getPrice() == 0) {
            nameLabel.setText(item.getProductName() + " (İKRAM)");
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #f39c12;");
        }

        HBox buttonGroup = new HBox(3);
        buttonGroup.setAlignment(Pos.CENTER);
        buttonGroup.setPrefWidth(90);
        buttonGroup.setMinWidth(90);

        Button minusBtn = new Button("−");
        minusBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-min-width: 24; -fx-max-width: 24; -fx-min-height: 24; -fx-max-height: 24; " +
                "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 0;");
        minusBtn.setOnAction(e -> {
            orderDAO.decreaseItemQuantity(item.getId(), item.getProductId());
            refreshOrderList();
        });

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        qtyLabel.setPrefWidth(20);
        qtyLabel.setAlignment(Pos.CENTER);

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-min-width: 24; -fx-max-width: 24; -fx-min-height: 24; -fx-max-height: 24; " +
                "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 0;");
        plusBtn.setOnAction(e -> {
            orderDAO.addProductToOrder(currentTableId, item.getProductId(), 1);
            refreshOrderList();
        });

        Button treatBtn = new Button("★");
        treatBtn.setTooltip(new Tooltip("İkram Yap (Ücretsiz)"));
        treatBtn.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-min-width: 24; -fx-max-width: 24; -fx-min-height: 24; -fx-max-height: 24; " +
                "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 10px;");

        if (item.getPrice() == 0) {
            treatBtn.setDisable(true);
            treatBtn.setStyle("-fx-background-color: #bdc3c7; -fx-min-width: 24; -fx-min-height: 24;");
        }

        treatBtn.setOnAction(e -> {
            boolean success = orderDAO.makeItemTreat(item.getId());
            if (success) {
                refreshOrderList();
            }
        });

        buttonGroup.getChildren().addAll(minusBtn, qtyLabel, plusBtn, treatBtn);

        Label priceLabel = new Label(String.format("%.2f ₺", item.getSubtotal()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
        priceLabel.setPrefWidth(50);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(nameLabel, buttonGroup, priceLabel);
        return card;
    }

    @FXML
    public void closeCheck() {
        List<OrderItem> items = orderDAO.getOrderItems(currentTableId);
        if (items.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Masada sipariş yok!");
            return;
        }

        double total = items.stream().mapToDouble(OrderItem::getSubtotal).sum();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hesap Kapatma");
        alert.setHeaderText("Toplam Tutar: " + String.format("%.2f TL", total));
        alert.setContentText("Ödeme yöntemini seçiniz:");

        ButtonType cashBtn = new ButtonType("Nakit / Kart");
        ButtonType veresiyeBtn = new ButtonType("Veresiye Yaz");
        ButtonType cancelBtn = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(cashBtn, veresiyeBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == cashBtn) {
                orderDAO.closeOrder(currentTableId);
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Hesap nakit/kart olarak kapatıldı.");
                goBack();
            } else if (result.get() == veresiyeBtn) {
                processVeresiyeClosing(total);
            }
        }
    }

    private void processVeresiyeClosing(double totalAmount) {
        com.baharkiraathanesi.kiraathane.dao.CustomerDAO customerDAO = new com.baharkiraathanesi.kiraathane.dao.CustomerDAO();
        java.util.List<com.baharkiraathanesi.kiraathane.model.Customer> customers = customerDAO.getAllCustomers();

        Dialog<com.baharkiraathanesi.kiraathane.model.Customer> dialog = new Dialog<>();
        dialog.setTitle("Veresiye Müşteri Seçimi");
        dialog.setHeaderText("Hesap kime yazılacak?");

        ButtonType okBtn = new ButtonType("Seç ve Kapat", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<com.baharkiraathanesi.kiraathane.model.Customer> customerBox = new ComboBox<>();
        customerBox.setItems(FXCollections.observableArrayList(customers));
        customerBox.setPromptText("Müşteri Seçin...");

        Button addBtn = new Button("Yeni Kayıt");
        addBtn.setOnAction(e -> {
            Dialog<Boolean> newCustDialog = new Dialog<>();
            newCustDialog.setTitle("Hızlı Müşteri Ekle");
            newCustDialog.setHeaderText("Yeni Müşteri Bilgileri");

            ButtonType saveType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
            newCustDialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            GridPane newGrid = new GridPane();
            newGrid.setHgap(10); newGrid.setVgap(10); newGrid.setPadding(new Insets(10));

            TextField nameF = new TextField(); nameF.setPromptText("Ad Soyad");
            TextField phoneF = new TextField(); phoneF.setPromptText("Telefon");

            newGrid.add(new Label("Ad Soyad:"), 0, 0); newGrid.add(nameF, 1, 0);
            newGrid.add(new Label("Telefon:"), 0, 1); newGrid.add(phoneF, 1, 1);

            newCustDialog.getDialogPane().setContent(newGrid);

            // Boş alan kontrolü
            final Button okButton = (Button) newCustDialog.getDialogPane().lookupButton(saveType);
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, ae -> {
                if (nameF.getText().trim().isEmpty() || phoneF.getText().trim().isEmpty()) {
                    ae.consume();
                    Alert warn = new Alert(Alert.AlertType.WARNING, "İsim ve Telefon zorunludur!");
                    warn.showAndWait();
                }
            });

            newCustDialog.setResultConverter(b -> {
                if (b == saveType) {
                    return customerDAO.addCustomer(nameF.getText(), phoneF.getText());
                }
                return false;
            });

            newCustDialog.showAndWait().ifPresent(success -> {
                if(success) {
                    customerBox.setItems(FXCollections.observableArrayList(customerDAO.getAllCustomers()));
                    customerBox.getSelectionModel().selectLast();
                }
            });
        });

        grid.add(new Label("Müşteri:"), 0, 0);
        grid.add(customerBox, 1, 0);
        grid.add(addBtn, 2, 0);

        dialog.getDialogPane().setContent(grid);

        final Button selectButton = (Button) dialog.getDialogPane().lookupButton(okBtn);
        selectButton.addEventFilter(javafx.event.ActionEvent.ACTION, ae -> {
            if (customerBox.getValue() == null) {
                ae.consume();
                Alert warn = new Alert(Alert.AlertType.WARNING, "Lütfen bir müşteri seçiniz!");
                warn.showAndWait();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == okBtn) return customerBox.getValue();
            return null;
        });

        Optional<com.baharkiraathanesi.kiraathane.model.Customer> customerOpt = dialog.showAndWait();

        if (customerOpt.isPresent()) {
            com.baharkiraathanesi.kiraathane.model.Customer selectedCustomer = customerOpt.get();

            boolean debtAdded = customerDAO.addDebtToCustomer(selectedCustomer.getId(), totalAmount);

            if (debtAdded) {
                orderDAO.closeOrder(currentTableId);
                showAlert(Alert.AlertType.INFORMATION, "Tamamlandı",
                        "Hesap " + selectedCustomer.getFullName() + " hesabına veresiye olarak işlendi.");
                goBack();
            } else {
                showAlert(Alert.AlertType.ERROR, "Hata", "Veresiye işlenirken hata oluştu!");
            }
        }
    }

    @FXML
    public void goBack() {
        HelloApplication.changeScene("tables-view.fxml");
    }

    @FXML
    public void clearAllItems() {
        List<OrderItem> items = orderDAO.getOrderItems(currentTableId);
        if (items.isEmpty()) return;

        for (OrderItem item : items) {
            orderDAO.removeOrderItem(item.getId(), item.getProductId(), item.getQuantity());
        }
        refreshOrderList();
        showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Tüm siparişler temizlendi!");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}