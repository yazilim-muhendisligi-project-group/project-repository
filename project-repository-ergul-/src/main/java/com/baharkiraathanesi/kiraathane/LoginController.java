package com.baharkiraathanesi.kiraathane;

import com.baharkiraathanesi.kiraathane.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;

import java.util.logging.Logger;

public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

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
            LOGGER.info("Giris basarili: " + username);
            HelloApplication.changeScene("main-menu.fxml");
        } else {
            LOGGER.warning("Basarisiz giris denemesi: " + username);
            showAlert(AlertType.ERROR, "Hata", "Kullanici adi veya sifre yanlis!");
            passwordField.clear();
        }
    }

    private boolean isLoginSuccessful(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Uyari", "Lutfen kullanici adi ve sifre giriniz!");
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

