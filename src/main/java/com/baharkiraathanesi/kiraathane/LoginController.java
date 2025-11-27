package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;

/**
 * Login ekranı kontrolcüsü
 * Kullanıcı girişini veritabanı üzerinden kontrol eder
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final UserDAO userDAO;

    /**
     * Constructor - UserDAO instance oluşturur
     */
    public LoginController() {
        this.userDAO = new UserDAO();
    }

    /**
     * Login butonuna tıklandığında çalışır
     * Kullanıcı adı ve şifreyi veritabanından kontrol eder
     */
    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (isLoginSuccessful(username, password)) {
            System.out.println("✅ Giriş başarılı: " + username);
            HelloApplication.changeScene("main-menu.fxml");
        } else {
            System.out.println("❌ Başarısız giriş denemesi: " + username);
            showAlert(AlertType.ERROR, "Hata", "Kullanıcı adı veya şifre yanlış!");
            passwordField.clear();
        }
    }

    private boolean isLoginSuccessful(String username, String password) {
        // Boş alan kontrolü
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Uyarı", "Lütfen kullanıcı adı ve şifre giriniz!");
            return false;
        }

        // Veritabanı kontrolü
        return userDAO.authenticate(username, password);
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}