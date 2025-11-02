module io.github.platovd.triangulator {
    requires javafx.controls;
    requires javafx.fxml;


    opens io.github.platovd.triangulator to javafx.fxml;
    exports io.github.platovd.triangulator;
}