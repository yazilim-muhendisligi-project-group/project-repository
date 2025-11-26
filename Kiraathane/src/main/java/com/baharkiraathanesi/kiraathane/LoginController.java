package com.baharkiraathanesi.kiraathane;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import com.baharkiraathanesi.kiraathane.dao.UserDAO;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        UserDAO userDAO = new UserDAO();
        boolean isLoginSuccessful = userDAO.login(username, password);
        if (isLoginSuccessful) {
            // dashboard-view.fxml does not exist. Open the tables view instead.
            HelloApplication.changeScene("main-menu.fxml");
        } else {
            System.out.println("Kullanıcı adı veya şifre yanlış!");
        }
    }
}