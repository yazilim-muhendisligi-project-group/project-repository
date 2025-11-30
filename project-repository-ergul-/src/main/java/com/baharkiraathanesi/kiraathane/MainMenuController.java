package com.baharkiraathanesi.kiraathane;

import javafx.fxml.FXML;

public class MainMenuController {

    @FXML
    private void goToStock() {
        System.out.println("Stok ekranına gidiliyor...");
        HelloApplication.changeScene("stock-view.fxml");
    }

    @FXML
    private void goToTables() {
        System.out.println("Masalar ekranına gidiliyor...");
        HelloApplication.changeScene("tables-view.fxml");
    }

    @FXML
    private void goToReport() {
        System.out.println("Z Raporu ekranına gidiliyor...");
        HelloApplication.changeScene("report-view.fxml");
    }
}
