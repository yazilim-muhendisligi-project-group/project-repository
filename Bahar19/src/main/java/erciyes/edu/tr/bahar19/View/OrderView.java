// erciyes.edu.tr.bahar19.View.OrderView.java

package erciyes.edu.tr.bahar19.View;

import erciyes.edu.tr.bahar19.MockDataUtility;
import erciyes.edu.tr.bahar19.Model.Order;
import erciyes.edu.tr.bahar19.Model.OrderItem;
import erciyes.edu.tr.bahar19.Model.Product;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

public class OrderView extends BorderPane {

    private final int tableNumber;
    private Order currentOrder;

    // UI Bileşenleri - Yenileme için bu alanları tutmalıyız
    private VBox detailList;
    private Label lblTotal;

    public OrderView(int tableNumber) {
        this.tableNumber = tableNumber;
        this.currentOrder = MockDataUtility.getMockOrder(tableNumber);
        initView();
    }

    private void initView() {
        this.setStyle("-fx-background-color: #ECEFF1;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #B71C1C;");

        Label lblTitle = new Label("Sipariş Takip");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        header.getChildren().add(lblTitle);
        this.setTop(header);

        HBox centerContent = new HBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setAlignment(Pos.TOP_CENTER);

        VBox orderDetailCard = createOrderDetailCard(currentOrder);
        HBox.setHgrow(orderDetailCard, Priority.ALWAYS);

        VBox productSelectionArea = createProductSelectionArea();
        productSelectionArea.setPrefWidth(350);

        centerContent.getChildren().addAll(orderDetailCard, productSelectionArea);
        this.setCenter(centerContent);

        HBox footer = createFooter();
        this.setBottom(footer);
    }

    private VBox createOrderDetailCard(Order order) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        Label lblMasa = new Label("Masa-" + order.getTableNumber());
        lblMasa.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        lblMasa.setTextFill(Color.web("#B71C1C"));

        detailList = new VBox(5);
        detailList.setMinHeight(200);

        for (OrderItem item : order.getItems()) {
            detailList.getChildren().add(createOrderItemRow(item));
        }

        Label separator = new Label("***********************************");
        separator.setTextFill(Color.web("#999999"));

        lblTotal = new Label(String.format("Toplam ................... %.2f TL", order.getTotalAmount()));
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTotal.setTextFill(Color.web("#2E7D32"));

        card.getChildren().addAll(lblMasa, detailList, separator, lblTotal);
        return card;
    }

    private HBox createOrderItemRow(OrderItem item) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        // UYUM: product.getname() kullanıldı
        Label lblItem = new Label(item.getProduct().getname() + " x" + item.getQuantity());
        lblItem.setFont(Font.font("Segoe UI", 16));
        lblItem.setPrefWidth(200);

        Label lblDots = new Label("......................");
        lblDots.setTextFill(Color.web("#999999"));
        HBox.setHgrow(lblDots, Priority.ALWAYS);

        Label lblAmount = new Label(String.format("%.0f TL", item.getTotal()));
        lblAmount.setFont(Font.font("Segoe UI", 16));

        row.getChildren().addAll(lblItem, lblDots, lblAmount);
        return row;
    }

    private VBox createProductSelectionArea() {
        VBox area = new VBox(15);
        area.setPadding(new Insets(20));
        area.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        VBox productList = new VBox(15);
        productList.setPadding(new Insets(10));

        List<Product> products = MockDataUtility.getMockProductsList();
        for (Product product : products) {
            productList.getChildren().add(createProductRow(product));
        }

        scrollPane.setContent(productList);
        area.getChildren().add(scrollPane);
        return area;
    }

    private HBox createProductRow(Product product) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        // UYUM: product.getname() kullanıldı
        Label lblProduct = new Label(product.getname());
        lblProduct.setFont(Font.font("Segoe UI", 16));
        lblProduct.setPrefWidth(150);

        TextField txtQuantity = new TextField("0");
        txtQuantity.setPrefWidth(40);
        txtQuantity.setAlignment(Pos.CENTER_RIGHT);
        txtQuantity.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");

        Button btnEkle = new Button("EKLE");
        btnEkle.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        // EKLE butonu işlevi (Simülasyon)
        btnEkle.setOnAction(e -> {
            try {
                int quantity = Integer.parseInt(txtQuantity.getText());
                if (quantity > 0) {
                    // 1. Mock Controller Çağrısı: MockDataUtility'de siparişi güncelle
                    MockDataUtility.addOrderItemToMockOrder(tableNumber, product, quantity);

                    // 2. Arayüz Güncelleme: Sol paneli yenile
                    refreshOrderDetails();

                    System.out.println(tableNumber + ". Masaya " + quantity + " adet " + product.getname() + " eklendi ve ekran güncellendi.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Geçersiz miktar girişi.");
            }
            txtQuantity.setText("0");
        });

        row.getChildren().addAll(lblProduct, txtQuantity, btnEkle);
        return row;
    }

    public void refreshOrderDetails() {
        this.currentOrder = MockDataUtility.getMockOrder(tableNumber);

        detailList.getChildren().clear();

        for (OrderItem item : currentOrder.getItems()) {
            detailList.getChildren().add(createOrderItemRow(item));
        }

        lblTotal.setText(String.format("Toplam ................... %.2f TL", currentOrder.getTotalAmount()));
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button btnMasalar = new Button("⬅ Masalar");
        btnMasalar.setStyle("-fx-background-color: #455A64; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        btnMasalar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Button btnHesapAl = new Button("Hesap Al");
        btnHesapAl.setPrefWidth(150);
        btnHesapAl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnHesapAl.setStyle("-fx-background-color: #EF6C00; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        btnMasalar.setOnAction(e -> {
            Scene scene = this.getScene();
            scene.setRoot(new TablesView());
        });

        btnHesapAl.setOnAction(e -> {
            System.out.println(tableNumber + ". Masanın hesabı kesiliyor. Toplam: " + currentOrder.getTotalAmount() + " TL");
            Scene scene = this.getScene();
            if (scene != null) {
                // PaymentView'a masa numarasını ve toplam tutarı gönder
                scene.setRoot(new PaymentView(tableNumber, currentOrder.getTotalAmount()));
            }
        });

        HBox leftContainer = new HBox(btnMasalar);
        HBox rightContainer = new HBox(btnHesapAl);
        rightContainer.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightContainer, Priority.ALWAYS);

        footer.getChildren().addAll(leftContainer, rightContainer);
        return footer;
    }
}