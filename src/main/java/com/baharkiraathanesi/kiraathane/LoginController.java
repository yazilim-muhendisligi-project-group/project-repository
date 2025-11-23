package com.baharkiraathanesi.kiraathane;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Veritabanı kontrolü burada yapılacak
        if ("yonetici".equals(username) && "1234".equals(password)) {
            // dashboard-view.fxml does not exist. Open the tables view instead.
            HelloApplication.changeScene("main-menu.fxml");
        } else {
            System.out.println("Kullanıcı adı veya şifre yanlış!");
        }
    }
}