module erciyes.edu.tr.bahar19 {
    requires javafx.controls;
    requires javafx.fxml;


    opens erciyes.edu.tr.bahar19 to javafx.fxml;
    opens erciyes.edu.tr.bahar19.Model to javafx.base;
    exports erciyes.edu.tr.bahar19;
    exports erciyes.edu.tr.bahar19.View;
    opens erciyes.edu.tr.bahar19.View to javafx.fxml;
}