package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.CustomerDAO;
import com.baharkiraathanesi.kiraathane.model.Customer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class VeresiyeController {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, Double> balanceColumn;

    private final CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        balanceColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (empty || balance == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f TL", balance));
                    if (balance > 0) setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: green;");
                }
            }
        });

        // --- YENİ: Sağ Tık Menüsü ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem paymentItem = new MenuItem("Ödeme Al");
        paymentItem.setOnAction(e -> receivePayment());

        MenuItem editItem = new MenuItem("Bilgileri Düzenle");
        editItem.setOnAction(e -> editCustomer());

        MenuItem deleteItem = new MenuItem("Müşteriyi Sil");
        deleteItem.setOnAction(e -> deleteCustomer());

        contextMenu.getItems().addAll(paymentItem, new SeparatorMenuItem(), editItem, deleteItem);
        customerTable.setContextMenu(contextMenu);
        // ---------------------------

        loadCustomers();
    }

    private void loadCustomers() {
        customerTable.setItems(FXCollections.observableArrayList(customerDAO.getAllCustomers()));
    }

    @FXML
    private void addCustomer() {
        showCustomerDialog(null); // Null gönderince "Ekleme" modu çalışır
    }

    @FXML
    private void editCustomer() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Lütfen düzenlenecek müşteriyi seçin.");
            return;
        }
        showCustomerDialog(selected); // Müşteri gönderince "Düzenleme" modu çalışır
    }

    // Hem Ekleme Hem Düzenleme İçin Ortak Metot
    private void showCustomerDialog(Customer customerToEdit) {
        boolean isEditMode = (customerToEdit != null);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(isEditMode ? "Müşteri Düzenle" : "Yeni Müşteri");
        dialog.setHeaderText(isEditMode ? "Bilgileri Güncelle" : "Yeni Müşteri Bilgileri");

        ButtonType saveBtnType = new ButtonType(isEditMode ? "Güncelle" : "Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Ad Soyad");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Telefon (Zorunlu)");

        if (isEditMode) {
            nameField.setText(customerToEdit.getFullName());
            phoneField.setText(customerToEdit.getPhone());
        }

        grid.add(new Label("Ad Soyad:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Telefon:"), 0, 1); grid.add(phoneField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.WARNING, "Lütfen İsim ve Telefon numarasını giriniz!");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                if (isEditMode) {
                    return customerDAO.updateCustomer(customerToEdit.getId(), nameField.getText(), phoneField.getText());
                } else {
                    return customerDAO.addCustomer(nameField.getText(), phoneField.getText());
                }
            }
            return null;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            loadCustomers();
            if(isEditMode) showAlert(Alert.AlertType.INFORMATION, "Müşteri bilgileri güncellendi.");
        }
    }

    @FXML
    private void deleteCustomer() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Lütfen silinecek müşteriyi seçin.");
            return;
        }

        // Borç Kontrolü
        if (selected.getBalance() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Borç Uyarısı");
            alert.setHeaderText("Bu müşterinin " + selected.getBalance() + " TL borcu var!");
            alert.setContentText("Yine de silmek istiyor musunuz? Bu işlem geri alınamaz.");

            Optional<ButtonType> result = alert.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return;
            }
        } else {
            // Borcu yoksa normal onay sor
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Silme Onayı");
            alert.setHeaderText(selected.getFullName() + " silinecek.");
            alert.setContentText("Emin misiniz?");
            Optional<ButtonType> result = alert.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) return;
        }

        if (customerDAO.deleteCustomer(selected.getId())) {
            loadCustomers();
            showAlert(Alert.AlertType.INFORMATION, "Müşteri başarıyla silindi.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Silme işlemi sırasında hata oluştu.");
        }
    }

    @FXML
    private void receivePayment() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Lütfen ödeme alınacak müşteriyi seçin.");
            return;
        }

        if (selected.getBalance() <= 0) {
            showAlert(Alert.AlertType.INFORMATION, "Müşterinin borcu bulunmamaktadır.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ödeme Al");
        dialog.setHeaderText(selected.getFullName() + " - Borç: " + String.format("%.2f TL", selected.getBalance()));
        dialog.setContentText("Tahsil edilen tutar:");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);

                if (amount <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Tutar pozitif olmalıdır!");
                    return;
                }

                if (amount > selected.getBalance()) {
                    showAlert(Alert.AlertType.ERROR, "HATA: Girilen tutar borçtan fazla olamaz!");
                    return;
                }

                if (customerDAO.makePayment(selected.getId(), amount)) {
                    loadCustomers();
                    showAlert(Alert.AlertType.INFORMATION, "Ödeme başarıyla alındı.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Geçersiz tutar! Lütfen sayı giriniz.");
            }
        });
    }

    @FXML public void goBack() { HelloApplication.changeScene("main-menu.fxml"); }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).showAndWait();
    }
}