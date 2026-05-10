package ui.support;

import business.shared.exceptions.EligibilityException;
import business.support.*;
import ui.document.StudentDashboard;
import ui.document.ViewHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;



public class ScholarshipScreen {

    private final String username;
    private final int    studentId;

    public ScholarshipScreen(String username, int studentId) {
        this.username  = username;
        this.studentId = studentId;
    }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new ScholarshipScreen(username, studentId).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        ComboBox<String> cbType = new ComboBox<>();
        for (ScholarshipType t : ScholarshipType.values()) cbType.getItems().add(t.name());
        cbType.setPromptText("Select scholarship type...");
        cbType.setMaxWidth(Double.MAX_VALUE);
        ViewHelper.addRow(grid, 0, "Scholarship Type", cbType);

        TextField tfCgpa = ViewHelper.styledField("e.g. 3.75  (0.0 – 4.0)");
        ViewHelper.addRow(grid, 1, "CGPA", tfCgpa);

        TextField tfIncome = ViewHelper.styledField("Monthly family income in PKR");
        ViewHelper.addRow(grid, 2, "Family Income / mo", tfIncome);

        // Live eligibility hint
        Label lblHint = new Label("Select a type to see eligibility criteria.");
        lblHint.setStyle("-fx-font-size:13px; -fx-text-fill:" + ViewHelper.MUTED() + "; -fx-font-style:italic;");
        lblHint.setWrapText(true);
        ViewHelper.addRow(grid, 3, "Eligibility", lblHint);

        cbType.valueProperty().addListener((o, old, v) -> updateHint(lblHint, cbType, tfCgpa, tfIncome));
        tfCgpa.textProperty().addListener((o, old, v)   -> updateHint(lblHint, cbType, tfCgpa, tfIncome));
        tfIncome.textProperty().addListener((o, old, v) -> updateHint(lblHint, cbType, tfCgpa, tfIncome));

        TextArea taQuery = new TextArea();
        taQuery.setPromptText("Describe your query in detail...");
        taQuery.setPrefRowCount(4); taQuery.setWrapText(true);
        taQuery.setStyle(ViewHelper.FIELD_STYLE());
        ViewHelper.addRow(grid, 4, "Query", taQuery);

        // Structured attachment via dialog instead of free-text input
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
        ViewHelper.addRow(grid, 5, "Supporting Documents", docRow);

        Button submitBtn = ViewHelper.solidBtn("Submit Query", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            if (cbType.getValue() == null || tfCgpa.getText().trim().isEmpty()
                    || tfIncome.getText().trim().isEmpty() || taQuery.getText().trim().isEmpty()) {
                alert(Alert.AlertType.WARNING, "Required Fields",
                        "Type, CGPA, family income and query are all required.");
                return;
            }
            double cgpa, income;
            try {
                cgpa   = Double.parseDouble(tfCgpa.getText().trim());
                income = Double.parseDouble(tfIncome.getText().trim());
            } catch (NumberFormatException ex) {
                alert(Alert.AlertType.ERROR, "Invalid Input", "CGPA and income must be valid numbers.");
                return;
            }
            try {
                SupportController ctrl = new SupportController();
                ScholarshipQuery result = ctrl.submitScholarshipQuery(
                        studentId,
                        ScholarshipType.valueOf(cbType.getValue()),
                        cgpa, income,
                        taQuery.getText().trim(),
                        docAttached[0]);
                alert(Alert.AlertType.INFORMATION, "Query Submitted",
                        "Your scholarship query has been submitted.\nTracking ID: " + result.getRequestId());
                new StudentDashboard(username).show(stage);
            } catch (EligibilityException ex) {
                alert(Alert.AlertType.WARNING, "Not Eligible", ex.getMessage());
            } catch (Exception ex) {
                alert(Alert.AlertType.ERROR, "Submission Failed", ex.getMessage());
            }
        });

        VBox formCard = ViewHelper.createFormCard(grid, submitBtn,
                "Scholarship / Financial Aid Query");

        ScrollPane scroll = new ScrollPane(formCard);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Scholarship Query");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void updateHint(Label hint, ComboBox<String> cbType,
                            TextField tfCgpa, TextField tfIncome) {
        if (cbType.getValue() == null) return;
        try {
            double cgpa   = tfCgpa.getText().trim().isEmpty()   ? 0 : Double.parseDouble(tfCgpa.getText().trim());
            double income = tfIncome.getText().trim().isEmpty()  ? 0 : Double.parseDouble(tfIncome.getText().trim());
            ScholarshipQuery tmp = new ScholarshipQuery(0);
            tmp.setScholarshipType(ScholarshipType.valueOf(cbType.getValue()));
            tmp.setCgpa(cgpa); tmp.setFamilyIncome(income);
            boolean ok = tmp.checkEligibility();
            hint.setText((ok ? "✔ Eligible — " : "✘ Not eligible — ") + tmp.getEligibilityCriteria());
            hint.setStyle(ok
                    ? "-fx-font-size:13px; -fx-text-fill:#059669; -fx-font-weight:bold;"
                    : "-fx-font-size:13px; -fx-text-fill:#DC2626; -fx-font-weight:bold;");
        } catch (NumberFormatException ignored) {}
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}