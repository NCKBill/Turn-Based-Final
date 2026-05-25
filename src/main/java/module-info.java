module nckbill.turnbasedfinal {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.media;
    requires java.desktop;

    exports nckbill.turnbasedfinal;
    exports Unit;
    opens nckbill.turnbasedfinal to javafx.fxml;

    exports Controller;
    exports Action;
    exports Board;
    exports nckbill.turnbasedfinal.UI;
    opens nckbill.turnbasedfinal.UI to javafx.fxml;
    exports nckbill.turnbasedfinal.utils;
    opens nckbill.turnbasedfinal.utils to javafx.fxml;
}
