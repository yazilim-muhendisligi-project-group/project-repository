package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.OrderDAO;
import com.baharkiraathanesi.kiraathane.dao.ReportDAO;
import com.baharkiraathanesi.kiraathane.model.Order;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final ReportDAO reportDAO = new ReportDAO();
    private List<Order> currentOrders = new ArrayList<>();
    private double currentRevenue = 0.0;

    @FXML
    public void initialize() {
        System.out.println("=== ReportController Başlatılıyor ===");

        // Bugünün tarihini göster
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        dateLabel.setText("Tarih: " + today.format(formatter));

        Platform.runLater(this::loadReportData);
    }

    @FXML
    private void printZReport() {
        // Allow creating Z report even if there are no sales (user wanted 0 TL report as well)
        // Onay al
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Z Raporu Onayı");
        confirmAlert.setHeaderText("Z Raporu almak istediğinize emin misiniz?");

        String content = "Toplam İşlem: " + (currentOrders == null ? 0 : currentOrders.size()) + "\n" +
            "Toplam Ciro: " + String.format("%.2f TL", currentRevenue) + "\n\n" +
            "⚠️ Bu işlem geri alınamaz! Bugünkü veriler PDF'e kaydedilip sıfırlanacak.";

        confirmAlert.setContentText(content);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            generatePDFReport();
        }
    }

    private void generatePDFReport() {
        try {
            // Dosya kaydetme dialogu
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Z Raporu Kaydet");

            LocalDateTime now = LocalDateTime.now();
            String defaultFileName = "Z_Raporu_" +
                now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")) + ".pdf";

            fileChooser.setInitialFileName(defaultFileName);
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Dosyası", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());
            if (file == null) {
                return; // Kullanıcı iptal etti
            }

            // PDF oluştur
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Fontları önceden tanımla (sistem fontlarını taramayı önler)
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Başlık
            contentStream.setFont(boldFont, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("BAHAR KIRAATHANESI");
            contentStream.endText();

            contentStream.setFont(boldFont, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 730);
            contentStream.showText("GUN SONU RAPORU (Z RAPORU)");
            contentStream.endText();

            // Tarih ve saat
            contentStream.setFont(normalFont, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 710);
            contentStream.showText("Tarih: " + now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            contentStream.endText();

            // Çizgi
            contentStream.moveTo(50, 700);
            contentStream.lineTo(550, 700);
            contentStream.stroke();

            // Özet bilgiler
            float yPosition = 680;
            contentStream.setFont(boldFont, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("OZET BILGILER");
            contentStream.endText();

            yPosition -= 25;
            contentStream.setFont(normalFont, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Toplam Islem Sayisi: " + (currentOrders == null ? 0 : currentOrders.size()));
            contentStream.endText();

            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(String.format("Toplam Ciro: %.2f TL", currentRevenue));
            contentStream.endText();

            // Çizgi
            yPosition -= 10;
            contentStream.moveTo(50, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();

            // Satış detayları
            yPosition -= 25;
            contentStream.setFont(boldFont, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("SATIS DETAYLARI");
            contentStream.endText();

            yPosition -= 20;
            contentStream.setFont(boldFont, 9);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("ID");
            contentStream.endText();
            contentStream.beginText();
            contentStream.newLineAtOffset(100, yPosition);
            contentStream.showText("Masa");
            contentStream.endText();
            contentStream.beginText();
            contentStream.newLineAtOffset(200, yPosition);
            contentStream.showText("Saat");
            contentStream.endText();
            contentStream.beginText();
            contentStream.newLineAtOffset(300, yPosition);
            contentStream.showText("Tutar");
            contentStream.endText();
            contentStream.beginText();
            contentStream.newLineAtOffset(400, yPosition);
            contentStream.showText("Odeme");
            contentStream.endText();

            yPosition -= 15;
            contentStream.setFont(normalFont, 9);

            if (currentOrders != null) {
                for (Order order : currentOrders) {
                    if (yPosition < 50) {
                        // Yeni sayfa gerekiyor
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = 750;
                        contentStream.setFont(normalFont, 9);
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(String.valueOf(order.getId()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, yPosition);
                    contentStream.showText(order.getTableName());
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(200, yPosition);
                    contentStream.showText(order.getOrderTime());
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(300, yPosition);
                    contentStream.showText(String.format("%.2f TL", order.getTotal()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(400, yPosition);
                    contentStream.showText(order.getPaymentType());
                    contentStream.endText();

                    yPosition -= 15;
                }
            }

            contentStream.close();
            document.save(file);
            document.close();

            System.out.println("✅ PDF başarıyla oluşturuldu: " + file.getAbsolutePath());

            // Veritabanını sıfırla
            boolean resetSuccess = reportDAO.resetDailyData();

            if (resetSuccess) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Başarılı");
                successAlert.setHeaderText("Z Raporu Başarıyla Oluşturuldu!");
                successAlert.setContentText(
                    "PDF Kaydedildi: " + file.getName() + "\n\n" +
                    "Bugünkü veriler sıfırlandı.\n" +
                    "Yeni güne hazırsınız!"
                );
                successAlert.showAndWait();

                // Ekranı yenile
                loadReportData();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Hata");
                errorAlert.setContentText("PDF oluşturuldu ancak veriler sıfırlanırken hata oluştu!");
                errorAlert.showAndWait();
            }

        } catch (IOException e) {
            System.err.println("❌ PDF oluşturma hatası: " + e.getMessage());
            e.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Hata");
            errorAlert.setHeaderText("PDF Oluşturma Hatası");
            errorAlert.setContentText("PDF dosyası oluşturulurken bir hata oluştu:\n" + e.getMessage());
            errorAlert.showAndWait();
        }
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

            currentOrders = todayOrders == null ? new ArrayList<>() : todayOrders; // Mevcut siparişleri güncelle

            if (todayOrders != null && !todayOrders.isEmpty()) {
                System.out.println("✅ " + todayOrders.size() + " adet bugünkü işlem bulundu");

                // Toplam ciro ve işlem sayısını hesapla
                double totalRevenue = 0.0;
                for (Order order : todayOrders) {
                    totalRevenue += order.getTotal();
                }
                currentRevenue = totalRevenue; // Mevcut ciroyu güncelle

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
                currentRevenue = 0.0;
            }
        } catch (Exception e) {
            System.err.println("❌ ReportController Hatası: " + e.getMessage());
            e.printStackTrace();
            reportTable.setItems(FXCollections.observableArrayList());
            totalRevenueLabel.setText("0.00 TL");
            totalOrdersLabel.setText("0");
            currentOrders = new ArrayList<>();
            currentRevenue = 0.0;
        }
    }

    @FXML
    public void goBackToMenu() {
        System.out.println("Ana menüye gidiliyor...");
        HelloApplication.changeScene("main-menu.fxml");
    }
}
