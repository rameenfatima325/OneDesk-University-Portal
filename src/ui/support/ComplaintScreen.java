package ui.support;

import business.support.*;
import ui.document.StudentDashboard;
import ui.document.ViewHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;



public class ComplaintScreen {

    private final String username;
    private final int    studentId;

    public ComplaintScreen(String username, int studentId) {
        this.username  = username;
        this.studentId = studentId;
    }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new ComplaintScreen(username, studentId).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        ComboBox<String> cbCategory = new ComboBox<>();
        for (ComplaintCategory c : ComplaintCategory.values()) cbCategory.getItems().add(c.name());
        cbCategory.setPromptText("Select category...");
        cbCategory.setMaxWidth(Double.MAX_VALUE);
        ViewHelper.addRow(grid, 0, "Category", cbCategory);

        ComboBox<String> cbSeverity = new ComboBox<>();
        for (Severity s : Severity.values()) cbSeverity.getItems().add(s.name());
        cbSeverity.setPromptText("Select severity...");
        cbSeverity.setMaxWidth(Double.MAX_VALUE);
        ViewHelper.addRow(grid, 1, "Severity", cbSeverity);

        TextField tfSubject = ViewHelper.styledField("Brief subject line");
        ViewHelper.addRow(grid, 2, "Subject", tfSubject);

        TextArea taDesc = new TextArea();
        taDesc.setPromptText("Provide a detailed description of your complaint...");
        taDesc.setPrefRowCount(5); taDesc.setWrapText(true);
        taDesc.setStyle(ViewHelper.FIELD_STYLE());
        ViewHelper.addRow(grid, 3, "Description", taDesc);

        // Structured supporting document via dialog (optional for complaints)
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
        ViewHelper.addRow(grid, 4, "Supporting Document", docRow);

        CheckBox chkAnon = new CheckBox("Submit Anonymously");
        chkAnon.setStyle("-fx-font-size:14px; -fx-text-fill:" + ViewHelper.TEXT_DARK() + ";");
        Tooltip.install(chkAnon, new Tooltip(
                "If checked, your Student ID will NOT be visible to admins.\n"
                        + "You cannot be contacted for follow-up."));

        Label lblAnonNote = new Label("⚠  Your identity will be hidden from admins.");
        lblAnonNote.setStyle("-fx-font-size:13px; -fx-text-fill:#D97706; -fx-font-style:italic;");
        lblAnonNote.setVisible(false);
        lblAnonNote.setManaged(false);
        chkAnon.selectedProperty().addListener((obs, ov, nv) -> {
            lblAnonNote.setVisible(nv);
            lblAnonNote.setManaged(nv);
        });

        VBox anonBox = new VBox(6, chkAnon, lblAnonNote);
        ViewHelper.addRow(grid, 5, "Anonymous", anonBox);

        Button submitBtn = ViewHelper.solidBtn("Submit Complaint", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            if (cbCategory.getValue() == null || cbSeverity.getValue() == null
                    || tfSubject.getText().trim().isEmpty()
                    || taDesc.getText().trim().isEmpty()) {
                alert(Alert.AlertType.WARNING, "Required Fields",
                        "Category, severity, subject and description are all required.");
                return;
            }
            try {
                SupportController ctrl = new SupportController();
                String fullDescription = taDesc.getText().trim();
                if (!docAttached[0].isBlank()) {
                    fullDescription += "\n\nAttached: " + docAttached[0];
                }
                GeneralComplaint result = ctrl.submitComplaint(
                        studentId,
                        ComplaintCategory.valueOf(cbCategory.getValue()),
                        Severity.valueOf(cbSeverity.getValue()),
                        tfSubject.getText().trim(),
                        fullDescription,
                        chkAnon.isSelected());
                String msg = chkAnon.isSelected()
                        ? "Complaint submitted anonymously.\nTracking ID: " + result.getRequestId()
                        : "Complaint submitted successfully.\nTracking ID: " + result.getRequestId();
                alert(Alert.AlertType.INFORMATION, "Complaint Submitted", msg);
                new StudentDashboard(username).show(stage);
            } catch (business.shared.exceptions.EligibilityException eex) {
                alert(Alert.AlertType.WARNING, "Not Eligible", eex.getMessage());
            } catch (Exception ex) {
                alert(Alert.AlertType.ERROR, "Submission Failed", ex.getMessage());
            }
        });

        VBox formCard = ViewHelper.createFormCard(grid, submitBtn,
                "Submit General Complaint");

        ScrollPane scroll = new ScrollPane(formCard);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — General Complaint");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}