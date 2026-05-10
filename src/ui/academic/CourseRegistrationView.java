package ui.academic;

import business.academic.AcademicRequestController;
import business.academic.CourseRegistrationIssue;
import ui.document.StudentDashboard;
import ui.document.ViewHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class CourseRegistrationView {

    private final String username;
    private final int    studentId;

    public CourseRegistrationView(String username, int studentId) {
        this.username  = username;
        this.studentId = studentId;
    }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new CourseRegistrationView(username, studentId).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        TextField codeField = ViewHelper.styledField("e.g. CS2009");
        ViewHelper.addRow(grid, 0, "Course Code", codeField);

        TextField sectionField = ViewHelper.styledField("e.g. A  (optional)");
        ViewHelper.addRow(grid, 1, "Section", sectionField);

        ComboBox<String> semesterBox = ViewHelper.styledCombo();
        semesterBox.getItems().addAll("Fall 2025", "Spring 2026", "Summer 2026", "Fall 2026");
        semesterBox.setPromptText("Select semester...");
        ViewHelper.addRow(grid, 2, "Semester", semesterBox);

        ComboBox<String> typeBox = ViewHelper.styledCombo();
        typeBox.getItems().addAll("Wrong Section", "Not Enrolled", "Duplicate", "Other");
        typeBox.setPromptText("Select issue type...");
        ViewHelper.addRow(grid, 3, "Issue Type", typeBox);

        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the issue in detail...");
        descArea.setPrefRowCount(4);
        descArea.setWrapText(true);
        descArea.setStyle(ViewHelper.FIELD_STYLE());
        ViewHelper.addRow(grid, 4, "Description", descArea);

        ComboBox<String> urgencyBox = ViewHelper.styledCombo();
        urgencyBox.getItems().addAll("Low", "Medium", "High");
        urgencyBox.setValue("Medium");
        ViewHelper.addRow(grid, 5, "Urgency Level", urgencyBox);

        // Structured attachment via dialog
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
        docRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ViewHelper.addRow(grid, 6, "Supporting Document", docRow);

        Button submitBtn = ViewHelper.solidBtn("Submit Issue", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            if (codeField.getText().trim().isEmpty()
                    || semesterBox.getValue() == null
                    || typeBox.getValue() == null
                    || descArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Required Fields",
                        "Course code, semester, issue type and description are required.");
                return;
            }

            CourseRegistrationIssue.IssueType type =
                    mapIssueType(typeBox.getValue());
            CourseRegistrationIssue.Urgency urg =
                    CourseRegistrationIssue.Urgency.valueOf(urgencyBox.getValue().toUpperCase());

            try {
                AcademicRequestController controller = new AcademicRequestController();
                String fullDesc = descArea.getText().trim();
                if (!docAttached[0].isBlank()) fullDesc += "\n\nAttached: " + docAttached[0];
                CourseRegistrationIssue result = controller.handleCourseRegistrationIssue(
                        studentId,
                        codeField.getText().trim(),
                        sectionField.getText().trim(),
                        semesterBox.getValue(),
                        type,
                        fullDesc,
                        urg);

                if (result != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Issue Submitted",
                            "Your registration issue has been recorded.\n"
                                    + "Tracking ID: " + result.getTrackingId() + "\n"
                                    + "Routed to: " + result.categorizeIssue());
                    new StudentDashboard(username).show(stage);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Submission Failed",
                            "We could not submit your issue. Please try again.");
                }
            } catch (business.shared.exceptions.EligibilityException ex) {
                showAlert(Alert.AlertType.WARNING, "Not Eligible", ex.getMessage());
            }
        });

        VBox formCardWrapper = ViewHelper.createFormCard(grid, submitBtn,
                "Submit Course Registration Issue");

        ScrollPane scroll = new ScrollPane(formCardWrapper);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Course Registration Issue");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private CourseRegistrationIssue.IssueType mapIssueType(String ui) {
        return switch (ui) {
            case "Wrong Section" -> CourseRegistrationIssue.IssueType.WRONG_SECTION;
            case "Not Enrolled"  -> CourseRegistrationIssue.IssueType.NOT_ENROLLED;
            case "Duplicate"     -> CourseRegistrationIssue.IssueType.DUPLICATE;
            default              -> CourseRegistrationIssue.IssueType.OTHER;
        };
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}