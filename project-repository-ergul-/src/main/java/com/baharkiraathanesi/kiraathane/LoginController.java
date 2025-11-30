package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final UserDAO userDAO;

    public LoginController() {
        this.userDAO = new UserDAO();
    }

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (isLoginSuccessful(username, password)) {
            System.out.println("Giriş başarılı: " + username);
            HelloApplication.changeScene("main-menu.fxml");
        } else {
            System.out.println("Başarısız giriş denemesi: " + username);
            showAlert(AlertType.ERROR, "Hata", "Kullanıcı adı veya şifre yanlış!");
            passwordField.clear();
        }
    }

    private boolean isLoginSuccessful(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Uyarı", "Lütfen kullanıcı adı ve şifre giriniz!");
            return false;
        }

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