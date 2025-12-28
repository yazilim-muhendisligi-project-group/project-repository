package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.OrderDAO;
import com.baharkiraathanesi.kiraathane.dao.ReportDAO;
import com.baharkiraathanesi.kiraathane.model.Order;
import com.baharkiraathanesi.kiraathane.model.Report;
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
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportController {

    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());

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
    private Label totalRevenueLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private AreaChart<String, Double> weeklyRevenueChart;

    @FXML
    private BarChart<String, Double> monthlyRevenueChart;

    private final OrderDAO orderDAO = new OrderDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private List<Order> currentOrders = new ArrayList<>();
    private double currentRevenue = 0.0;

    @FXML
    public void initialize() {
        LOGGER.info("ReportController baslatiliyor");

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        dateLabel.setText("Tarih: " + today.format(formatter));

        Platform.runLater(this::loadReportData);
        Platform.runLater(this::loadWeeklyChart);
        Platform.runLater(this::loadMonthlyChart);
    }

    @FXML
    private void printZReport() {
        if (orderDAO.hasOpenOrders()) {
            showAlert(Alert.AlertType.ERROR, "Z Raporu Alınamaz",
                    "Z raporu alabilmek için once tum masaların hesaplarını kapatmanız gerekmektedir.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Z Raporu Onayı");
        confirmAlert.setHeaderText("Z Raporu almak istediğinize emin misiniz?");

        String content = "Toplam İşlem: " + (currentOrders == null ? 0 : currentOrders.size()) + "\n" +
                "Toplam Ciro: " + String.format("%.2f TL", currentRevenue) + "\n\n" +
                "Bu işlem geri alınamaz! Bugünkü veriler PDF'e kaydedilip sıfırlanacak.";

        confirmAlert.setContentText(content);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            generatePDFReport();
        }
    }

    private void generatePDFReport() {
        PDDocument document = null;
        PDPageContentStream contentStream = null;

        try {
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
                return;
            }

            document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            contentStream = new PDPageContentStream(document, page);
            PDFont normalFont;
            PDFont boldFont;

            try {
                String os = System.getProperty("os.name").toLowerCase();
                String normalFontPath;
                String boldFontPath;

                if (os.contains("mac")) {
                    normalFontPath = "/System/Library/Fonts/Supplemental/Arial.ttf";
                    boldFontPath = "/System/Library/Fonts/Supplemental/Arial Bold.ttf";
                } else if (os.contains("win")) {
                    normalFontPath = "C:/Windows/Fonts/arial.ttf";
                    boldFontPath = "C:/Windows/Fonts/arialbd.ttf";
                } else {
                    normalFontPath = "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf";
                    boldFontPath = "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf";
                }

                File normalFontFile = new File(normalFontPath);
                File boldFontFile = new File(boldFontPath);

                if (normalFontFile.exists() && boldFontFile.exists()) {
                    normalFont = PDType0Font.load(document, normalFontFile);
                    boldFont = PDType0Font.load(document, boldFontFile);
                } else {
                    LOGGER.warning("Arial fontu bulunamadi, Helvetica kullaniliyor");
                    normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Font yuklenemedi, Helvetica kullaniliyor", e);
                normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            }

            contentStream.setFont(boldFont, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("BAHAR KIRAATHANESİ");
            contentStream.endText();

            contentStream.setFont(boldFont, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 730);
            contentStream.showText("GÜN SONU RAPORU (Z RAPORU)");
            contentStream.endText();

            contentStream.setFont(normalFont, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 710);
            contentStream.showText("Tarih: " + now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            contentStream.endText();

            contentStream.moveTo(50, 700);
            contentStream.lineTo(550, 700);
            contentStream.stroke();

            float yPosition = 680;
            contentStream.setFont(boldFont, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("ÖZET BİLGİLER");
            contentStream.endText();

            yPosition -= 25;
            contentStream.setFont(normalFont, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Toplam İşlem Sayısı: " + (currentOrders == null ? 0 : currentOrders.size()));
            contentStream.endText();

            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(String.format("Toplam Ciro: %.2f TL", currentRevenue));
            contentStream.endText();

            yPosition -= 10;
            contentStream.moveTo(50, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();

            yPosition -= 25;
            contentStream.setFont(boldFont, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("SATIŞ DETAYLARI");
            contentStream.endText();

            yPosition -= 25;
            contentStream.setFont(boldFont, 14);

            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("ID");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(100, yPosition);
            contentStream.showText("Masa");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(250, yPosition);
            contentStream.showText("Saat");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(400, yPosition);
            contentStream.showText("Tutar");
            contentStream.endText();

            yPosition -= 25;
            contentStream.setFont(normalFont, 14);

            if (currentOrders != null) {
                for (Order order : currentOrders) {
                    if (yPosition < 50) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = 750;
                        contentStream.setFont(normalFont, 14);
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
                    contentStream.newLineAtOffset(250, yPosition);
                    contentStream.showText(order.getOrderTime());
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(400, yPosition);
                    contentStream.showText(String.format("%.2f TL", order.getTotal()));
                    contentStream.endText();

                    yPosition -= 25;
                }
            }

            contentStream.close();
            contentStream = null;
            document.save(file);

            LOGGER.info("PDF basariyla olusturuldu: " + file.getAbsolutePath());

            boolean resetSuccess = reportDAO.resetDailyData();

            if (resetSuccess) {
                showAlert(Alert.AlertType.INFORMATION, "Z Raporu Başarıyla Oluşturuldu",
                        "PDF Kaydedildi: " + file.getName() + "\n\n" +
                        "Bugünkü veriler sıfırlandı.\n" +
                        "Yeni güne hazırsınız!");

                loadReportData();
                loadWeeklyChart();
                loadMonthlyChart();
            } else {
                showAlert(Alert.AlertType.ERROR, "Hata",
                        "PDF oluşturuldu ancak veriler sıfırlanırken hata oluştu!");
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "PDF oluşturma hatası", e);
            showAlert(Alert.AlertType.ERROR, "PDF Oluşturma Hatası",
                    "PDF dosyası oluşturulurken bir hata oluştu.");
        } finally {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
                if (document != null) {
                    document.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "PDF kaynakları kapatılırken hata", e);
            }
        }
    }

    private void loadReportData() {
        try {
            LOGGER.info("Bugünkü rapor yükleniyor...");

            orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
            totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

            List<Order> todayOrders = orderDAO.getTodayCompletedOrders();

            currentOrders = todayOrders == null ? new ArrayList<>() : todayOrders;

            if (todayOrders != null && !todayOrders.isEmpty()) {
                double totalRevenue = 0.0;
                for (Order order : todayOrders) {
                    totalRevenue += order.getTotal();
                }
                currentRevenue = totalRevenue;

                ObservableList<Order> orders = FXCollections.observableArrayList(todayOrders);
                reportTable.setItems(orders);

                totalRevenueLabel.setText(String.format("%.2f TL", totalRevenue));
                totalOrdersLabel.setText(String.valueOf(todayOrders.size()));
            } else {
                reportTable.setItems(FXCollections.observableArrayList());
                totalRevenueLabel.setText("0.00 TL");
                totalOrdersLabel.setText("0");
                currentRevenue = 0.0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Rapor verileri yüklenirken hata", e);
        }
    }

    private void loadWeeklyChart() {
        try {
            List<Report> weeklyReports = reportDAO.getWeeklyDailyReports();
            if (weeklyRevenueChart == null) return;

            if (weeklyReports.isEmpty()) {
                weeklyRevenueChart.setTitle("Son 7 Günlük Ciro Raporu (Veri Yok)");
                weeklyRevenueChart.getData().clear();
                return;
            }

            XYChart.Series<String, Double> series = new XYChart.Series<>();
            series.setName("Günlük Ciro (TL)");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
            Locale trLocale = new Locale("tr", "TR");

            for (Report report : weeklyReports) {
                DayOfWeek dayOfWeek = report.getDate().getDayOfWeek();
                String dayName = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, trLocale);
                series.getData().add(new XYChart.Data<>(report.getDate().format(dateFormatter) + "\n" + dayName, report.getTotalRevenue()));
            }

            weeklyRevenueChart.getData().clear();
            weeklyRevenueChart.getData().add(series);
            weeklyRevenueChart.setTitle("Son 7 Günlük Ciro Raporu");
            weeklyRevenueChart.setLegendVisible(false);
            weeklyRevenueChart.setCreateSymbols(true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Haftalık grafik yüklenirken hata", e);
        }
    }

    private void loadMonthlyChart() {
        try {
            List<Report> monthlyReports = reportDAO.getMonthlyReports();
            if (monthlyRevenueChart == null) return;

            if (monthlyReports.isEmpty()) {
                monthlyRevenueChart.setTitle("Aylik Ciro Raporu (Veri Yok)");
                monthlyRevenueChart.getData().clear();
                return;
            }

            XYChart.Series<String, Double> series = new XYChart.Series<>();
            series.setName("Aylık Toplam Ciro (TL)");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("tr", "TR"));

            for (int i = monthlyReports.size() - 1; i >= 0; i--) {
                Report report = monthlyReports.get(i);
                series.getData().add(new XYChart.Data<>(report.getDate().format(formatter), report.getTotalRevenue()));
            }

            monthlyRevenueChart.getData().clear();
            monthlyRevenueChart.getData().add(series);
            monthlyRevenueChart.setTitle("Aylık Ciro Dağılımı");
            monthlyRevenueChart.setLegendVisible(false);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Aylık grafik yüklenirken hata", e);
        }
    }

    @FXML
    public void goBackToMenu() {
        HelloApplication.changeScene("main-menu.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

