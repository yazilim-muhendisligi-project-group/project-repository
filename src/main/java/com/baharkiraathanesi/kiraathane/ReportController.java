package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.OrderDAO;
import com.baharkiraathanesi.kiraathane.model.Order;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportController {

    @FXML
    private TableView<Order> reportTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, String> tableNameColumn;

    @FXML
    private TableColumn<Order, String> timeColumn;

    @FXML
    private TableColumn<Order, Double> totalColumn;

    @FXML
    private TableColumn<Order, String> paymentTypeColumn;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label dateLabel;

    private final OrderDAO orderDAO = new OrderDAO();

    @FXML
    public void initialize() {
        System.out.println("=== ReportController Başlatılıyor ===");

        // Bugünün tarihini göster
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        dateLabel.setText("Tarih: " + today.format(formatter));

        // Veri yüklemeyi UI thread'inde yap
        Platform.runLater(this::loadReportData);
    }

    private void loadReportData() {
        try {
            System.out.println("Bugünün raporu yükleniyor...");

            // Sütunları konfigüre et
            orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
            totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
            paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));

            // Bugünün tamamlanmış siparişlerini çek
            List<Order> todayOrders = orderDAO.getTodayCompletedOrders();

            if (todayOrders != null && !todayOrders.isEmpty()) {
                System.out.println("✅ " + todayOrders.size() + " adet bugünkü işlem bulundu");

                // Toplam ciro ve işlem sayısını hesapla
                double totalRevenue = 0.0;
                for (Order order : todayOrders) {
                    totalRevenue += order.getTotal();
                }

                // ObservableList'e dönüştür
                ObservableList<Order> orders = FXCollections.observableArrayList(todayOrders);
                reportTable.setItems(orders);

                // Özet bilgileri güncelle
                totalRevenueLabel.setText(String.format("%.2f TL", totalRevenue));
                totalOrdersLabel.setText(String.valueOf(todayOrders.size()));

                System.out.println("✅ Toplam ciro: " + totalRevenue + " TL");
            } else {
                System.out.println("⚠️ Bugün hiç satış yapılmamış");
                reportTable.setItems(FXCollections.observableArrayList());
                totalRevenueLabel.setText("0.00 TL");
                totalOrdersLabel.setText("0");
            }
        } catch (Exception e) {
            System.err.println("❌ ReportController Hatası: " + e.getMessage());
            e.printStackTrace();
            reportTable.setItems(FXCollections.observableArrayList());
            totalRevenueLabel.setText("0.00 TL");
            totalOrdersLabel.setText("0");
        }
    }

    @FXML
    public void goBackToMenu() {
        System.out.println("Ana menüye gidiliyor...");
        HelloApplication.changeScene("main-menu.fxml");
    }
}
