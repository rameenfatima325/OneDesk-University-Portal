package ui.academic;

import business.academic.AcademicRequestController;
import business.academic.AttendanceCorrectionRequest;
import ui.document.StudentDashboard;
import ui.document.ViewHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AttendanceCorrectionView {

    private final String username;
    private final int    studentId;

    public AttendanceCorrectionView(String username, int studentId) {
        this.username  = username;
        this.studentId = studentId;
    }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new AttendanceCorrectionView(username, studentId).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        TextField courseField = ViewHelper.styledField("e.g. Software Design");
        ViewHelper.addRow(grid, 0, "Course Name", courseField);

        ComboBox<String> semesterBox = ViewHelper.styledCombo();
        semesterBox.getItems().addAll("Fall 2025", "Spring 2026", "Summer 2026", "Fall 2026");
        semesterBox.setPromptText("Select semester...");
        ViewHelper.addRow(grid, 1, "Semester", semesterBox);

        DatePicker datePicker = new DatePicker();
        datePicker.getStyleClass().add("custom-date-picker");
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setPromptText("Date of missed class");
        // forbid future dates in the picker itself
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
            }
        });
        ViewHelper.addRow(grid, 2, "Class Date", datePicker);

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Explain why you missed the class...");
        reasonArea.setPrefRowCount(4);
        reasonArea.setWrapText(true);
        reasonArea.setStyle(ViewHelper.FIELD_STYLE());
        ViewHelper.addRow(grid, 3, "Reason", reasonArea);

        final String[] docAttached = { "" };
        Label docDisplay = new Label("No document attached");
        docDisplay.setStyle("-fx-font-style:italic; -fx-text-fill:" + ViewHelper.MUTED() + ";");
        Button docBtn = new Button("➕  Attach Document");
        docBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:" + ViewHelper.ACCENT
                + "; -fx-border-color:" + ViewHelper.ACCENT + "; -fx-border-radius:6; "
                + "-fx-padding:6 14; -fx-cursor:hand;");
        docBtn.setOnAction(ev -> {
            String picked = ui.common.SupportingDocDialog.show(stage);
            if (picked != null) {
                docAttached[0] = picked;
                docDisplay.setText("📎 " + picked);
                docDisplay.setStyle("-fx-text-fill:" + ViewHelper.TEXT_DARK() + ";");
            }
        });
        HBox docRow = new HBox(12, docBtn, docDisplay);
        docRow.setAlignment(Pos.CENTER_LEFT);
        ViewHelper.addRow(grid, 4, "Supporting Document", docRow);

        Button submitBtn = ViewHelper.solidBtn("Submit Request", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            if (courseField.getText().trim().isEmpty()
                    || semesterBox.getValue() == null
                    || datePicker.getValue() == null
                    || reasonArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Required Fields",
                        "Course name, semester, class date and reason are required.");
                return;
            }
            if (datePicker.getValue().isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.WARNING, "Invalid Date",
                        "Class date cannot be in the future.");
                return;
            }

            try {
                AcademicRequestController controller = new AcademicRequestController();
                AttendanceCorrectionRequest result = controller.handleAttendanceCorrection(
                        studentId,
                        courseField.getText().trim(),
                        semesterBox.getValue(),
                        datePicker.getValue(),
                        reasonArea.getText().trim(),
                        docAttached[0]);

                if (result != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Request Submitted",
                            "Your attendance correction request has been submitted.\n"
                                    + "Tracking ID: " + result.getTrackingId());
                    new StudentDashboard(username).show(stage);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Submission Failed",
                            "We could not submit your request. Please try again.");
                }
            } catch (business.shared.exceptions.EligibilityException ex) {
                showAlert(Alert.AlertType.WARNING, "Not Eligible", ex.getMessage());
            }
        });

        VBox formCardWrapper = ViewHelper.createFormCard(grid, submitBtn,
                "Submit Attendance Correction Request");

        ScrollPane scroll = new ScrollPane(formCardWrapper);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Attendance Correction");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}