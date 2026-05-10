package ui.academic;

import business.academic.AcademicRecordCorrection;
import business.academic.AcademicRequestController;
import business.shared.Priority;
import ui.document.StudentDashboard;
import ui.document.ViewHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class RecordCorrectionView {

    private final String username;
    private final int    studentId;

    public RecordCorrectionView(String username, int studentId) {
        this.username  = username;
        this.studentId = studentId;
    }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new RecordCorrectionView(username, studentId).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        ComboBox<String> typeBox = ViewHelper.styledCombo();
        typeBox.getItems().addAll("Grade", "CGPA", "Transcript Error", "Name Spelling", "Other");
        typeBox.setPromptText("Select record type...");
        ViewHelper.addRow(grid, 0, "Record Type", typeBox);

        /* Priority badge — updates live when the record type changes (Information Expert) */
        Label priorityBadge = new Label("—");
        priorityBadge.setPadding(new Insets(4, 12, 4, 12));
        priorityBadge.setStyle(
                "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:white; "
                        + "-fx-background-radius:20; -fx-background-color:" + ViewHelper.MUTED() + ";");
        HBox priBox = new HBox(priorityBadge);
        priBox.setAlignment(Pos.CENTER_LEFT);
        ViewHelper.addRow(grid, 1, "Priority (auto)", priBox);

        typeBox.valueProperty().addListener((o, old, v) -> {
            if (v == null) return;
            AcademicRecordCorrection tmp = new AcademicRecordCorrection(studentId);
            tmp.setRecordType(mapRecordType(v));
            Priority p = tmp.getPriorityLevel();
            String color = switch (p) {
                case HIGH   -> ViewHelper.DANGER;
                case MEDIUM -> ViewHelper.WARNING;
                case LOW    -> ViewHelper.SUCCESS;
            };
            priorityBadge.setText(p.name());
            priorityBadge.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:white; "
                    + "-fx-background-radius:20; -fx-background-color:" + color + ";");
        });

        TextField incorrectField = ViewHelper.styledField("Value currently on record");
        ViewHelper.addRow(grid, 2, "Incorrect Value", incorrectField);

        TextField correctField = ViewHelper.styledField("What the correct value should be");
        ViewHelper.addRow(grid, 3, "Correct Value", correctField);

        TextArea justArea = new TextArea();
        justArea.setPromptText("Explain why this correction is needed...");
        justArea.setPrefRowCount(4);
        justArea.setWrapText(true);
        justArea.setStyle(ViewHelper.FIELD_STYLE());
        ViewHelper.addRow(grid, 4, "Justification", justArea);

        // Structured evidence attachment via dialog
        final String[] docAttached = { "" };
        Label docDisplay = new Label("No document attached");
        docDisplay.setStyle("-fx-font-style:italic; -fx-text-fill:" + ViewHelper.MUTED() + ";");
        Button docBtn = new Button("➕  Attach Evidence");
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
        docRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ViewHelper.addRow(grid, 5, "Evidence", docRow);

        Button submitBtn = ViewHelper.solidBtn("Submit Correction", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            if (typeBox.getValue() == null
                    || incorrectField.getText().trim().isEmpty()
                    || correctField.getText().trim().isEmpty()
                    || justArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Required Fields",
                        "Record type, incorrect value, correct value and justification are required.");
                return;
            }

            try {
                AcademicRequestController controller = new AcademicRequestController();
                AcademicRecordCorrection result = controller.handleRecordCorrection(
                        studentId,
                        mapRecordType(typeBox.getValue()),
                        incorrectField.getText().trim(),
                        correctField.getText().trim(),
                        justArea.getText().trim(),
                        docAttached[0]);

                if (result != null) {
                    String extra = (result.getPriorityLevel() == Priority.HIGH)
                            ? "\n⚠  HIGH priority — admin has been notified."
                            : "";
                    showAlert(Alert.AlertType.INFORMATION, "Correction Submitted",
                            "Your record correction request has been submitted.\n"
                                    + "Tracking ID: " + result.getTrackingId()
                                    + "\nPriority: " + result.getPriorityLevel()
                                    + extra);
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
                "Submit Academic Record Correction");

        ScrollPane scroll = new ScrollPane(formCardWrapper);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);

        stage.setTitle("OneDesk — Record Correction");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private AcademicRecordCorrection.RecordType mapRecordType(String ui) {
        return switch (ui) {
            case "Grade"            -> AcademicRecordCorrection.RecordType.GRADE;
            case "CGPA"             -> AcademicRecordCorrection.RecordType.CGPA;
            case "Transcript Error" -> AcademicRecordCorrection.RecordType.TRANSCRIPT_ERROR;
            case "Name Spelling"    -> AcademicRecordCorrection.RecordType.NAME_SPELLING;
            default                 -> AcademicRecordCorrection.RecordType.OTHER;
        };
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}