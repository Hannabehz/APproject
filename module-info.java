module org.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires java.net.http;
    requires com.google.gson;

    opens org.example.demo1 to javafx.fxml;
    exports org.example.demo1;
    opens model to com.google.gson;

    // اگه MainViewController توی بسته controller هست، این خط رو اضافه کن
    opens controller to javafx.fxml; // برای دسترسی به کنترلر
    exports controller;
}