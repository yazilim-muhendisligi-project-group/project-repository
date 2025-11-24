package erciyes.edu.tr.bahar19.View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GirisEkrani extends StackPane {
    public GirisEkrani(){
        initView();
    }

    private void initView(){
        this.setStyle("-fx-background-color: #1565C0;");

        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(350);
        loginCard.setPadding(new Insets(40));

        loginCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;"+
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);"
        );

        Label lblTitle = new Label("BAHAR KIRAATHANESİ");
        lblTitle.setTextFill(Color.web("#1565C0"));
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Label lblSubtitle = new Label("Dijital Dönüşüm Paneli");
        lblSubtitle.setTextFill(Color.GRAY);
        lblSubtitle.setFont(Font.font("Segoe UI", 12));

        GridPane formGrid = new GridPane();
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        Label lblUser = new Label("Kullanıcı:");
        TextField txtUser = new TextField();
        txtUser.setPromptText("admin");
        txtUser.setStyle("-fx-padding: 8px; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label lblPass = new Label("Şifre:");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("••••••");
        txtPass.setStyle("-fx-padding: 8px; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        formGrid.add(lblUser, 0, 0);
        formGrid.add(txtUser, 0, 1);
        formGrid.add(lblPass, 0, 2);
        formGrid.add(txtPass, 0, 3);

        GridPane.setHgrow(txtUser, Priority.ALWAYS);
        GridPane.setHgrow(txtPass, Priority.ALWAYS);

        Button btnLogin = new Button("GİRİŞ YAP");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPadding(new Insets(12));
        btnLogin.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnLogin.setStyle(
                "-fx-background-color: #EF6C00;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );

        btnLogin.setOnAction(e -> {
            System.out.println("Giriş Yapılıyor... Kullanıcı: " + txtUser.getText());
        });

        loginCard.getChildren().addAll(lblTitle, lblSubtitle, formGrid, btnLogin);

        this.getChildren().add(loginCard);
        }
}
