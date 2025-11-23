module com.baharkiraathanesi.kiraathane {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.baharkiraathanesi.kiraathane to javafx.fxml;
    exports com.baharkiraathanesi.kiraathane;
}