module com.audiovisualizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.fasterxml.jackson.databind;
    opens com.audiovisualizer.controller to javafx.fxml;
    opens com.audiovisualizer to javafx.fxml, javafx.graphics;
    opens com.audiovisualizer.model to com.fasterxml.jackson.databind;
    exports com.audiovisualizer;
    opens com.audiovisualizer.view to javafx.fxml;
}