module com.audiovisualizer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.audiovisualizer to javafx.fxml;
    exports com.audiovisualizer;
}