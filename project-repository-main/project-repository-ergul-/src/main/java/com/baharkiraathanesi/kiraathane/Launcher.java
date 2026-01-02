package com.baharkiraathanesi.kiraathane;

import java.io.InputStream;
import java.util.logging.LogManager;

public class Launcher {
    public static void main(String[] args) {
        try {
            InputStream configFile = Launcher.class.getClassLoader()
                    .getResourceAsStream("logging.properties");
            if (configFile != null) {
                LogManager.getLogManager().readConfiguration(configFile);
                System.out.println("Logging yapılandırması yüklendi");
            }
        } catch (Exception e) {
            System.out.println("Logging yapılandırması yüklenemedi: " + e.getMessage());
        }

        HelloApplication.main(args);
    }
}