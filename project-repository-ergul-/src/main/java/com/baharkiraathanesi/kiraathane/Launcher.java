package com.baharkiraathanesi.kiraathane;

import javafx.application.Application;
import java.io.InputStream;
import java.util.logging.LogManager;

public class Launcher {
    public static void main(String[] args) {
        // PDFBox uyarılarını susturmak için logging yapılandırması
        try {
            InputStream configFile = Launcher.class.getClassLoader()
                .getResourceAsStream("logging.properties");
            if (configFile != null) {
                LogManager.getLogManager().readConfiguration(configFile);
                System.out.println("✅ Logging yapılandırması yüklendi - PDFBox uyarıları gizlendi");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Logging yapılandırması yüklenemedi: " + e.getMessage());
        }

        Application.launch(HelloApplication.class, args);
    }
}
