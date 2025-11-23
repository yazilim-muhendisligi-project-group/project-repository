module com.baharkiraathanesi.kiraathane {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.baharkiraathanesi.kiraathane to javafx.fxml;
    opens com.baharkiraathanesi.kiraathane.dao to javafx.fxml;
    opens com.baharkiraathanesi.kiraathane.model to javafx.fxml;
    opens com.baharkiraathanesi.kiraathane.database to javafx.fxml;

    exports com.baharkiraathanesi.kiraathane;
    exports com.baharkiraathanesi.kiraathane.dao;
    exports com.baharkiraathanesi.kiraathane.model;
    exports com.baharkiraathanesi.kiraathane.database;
}