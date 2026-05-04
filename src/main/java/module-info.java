module nckbill.turnbasedfinal {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires java.desktop;

    exports nckbill.turnbasedfinal;
    exports Unit;
    opens nckbill.turnbasedfinal to javafx.fxml;

    exports Controller;
    exports Action;
    exports Board;
}