package com.baharkiraathanesi.kiraathane;

import javafx.fxml.FXML;

import java.util.logging.Logger;

public class MainMenuController {

    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());

    @FXML
    private void goToStock() {
        LOGGER.info("Stok ekranına gidiliyor...");
        HelloApplication.changeScene("stock-view.fxml");
    }

    @FXML
    private void goToTables() {
        LOGGER.info("Masalar ekranına gidiliyor...");
        HelloApplication.changeScene("tables-view.fxml");
    }

    @FXML
    private void goToReport() {
        LOGGER.info("Z Raporu ekranına gidiliyor...");
        HelloApplication.changeScene("report-view.fxml");
    }

    @FXML
    private void goToVeresiye() {
        HelloApplication.changeScene("veresiye-view.fxml");
    }
}

