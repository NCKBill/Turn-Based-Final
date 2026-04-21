module nckbill.turnbasedfinal {
    requires javafx.controls;
    requires javafx.fxml;

    exports nckbill.turnbasedfinal;
    exports Unit;
    opens nckbill.turnbasedfinal to javafx.fxml;

    exports Controller;
    exports Action;
    exports Board;
}