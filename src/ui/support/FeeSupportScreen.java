package ui.support;

import business.shared.exceptions.InvalidAmountException;
import business.support.*;
import ui.document.StudentDashboard;
import ui.document.ViewHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;



public class FeeSupportScreen {

    private final String username;
    private final int    studentId;

    public FeeSupportScreen(String username, int studentId) {
        this.username  = username;
        this.studentId = studentId;
    }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new FeeSupportScreen(username, studentId).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        ComboBox<String> cbFeeType = new ComboBox<>();
        cbFeeType.getItems().addAll("TUITION", "HOSTEL", "OTHER");
        cbFeeType.setPromptText("Select fee type...");
        cbFeeType.setMaxWidth(Double.MAX_VALUE);
        ViewHelper.addRow(grid, 0, "Fee Type", cbFeeType);

        TextField tfAmount = ViewHelper.styledField("e.g. 45000");
        ViewHelper.addRow(grid, 1, "Outstanding Amount (PKR)", tfAmount);

        ComboBox<String> cbRequestType = new ComboBox<>();
        cbRequestType.getItems().addAll("WAIVER", "INSTALLMENT", "EXTENSION");
        cbRequestType.setPromptText("Select request type...");
        cbRequestType.setMaxWidth(Double.MAX_VALUE);
        ViewHelper.addRow(grid, 2, "Request Type", cbRequestType);

        TextArea taJustification = new TextArea();
        taJustification.setPromptText("Explain the reason for your request...");
        taJustification.setPrefRowCount(4);
        taJustification.setWrapText(true);
        taJustification.setStyle(ViewHelper.FIELD_STYLE());
        ViewHelper.addRow(grid, 3, "Justification", taJustification);

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
        ViewHelper.addRow(grid, 4, "Supporting Document", docRow);

        Button submitBtn = ViewHelper.solidBtn("Submit Request", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            if (cbFeeType.getValue() == null || cbRequestType.getValue() == null
                    || tfAmount.getText().trim().isEmpty()
                    || taJustification.getText().trim().isEmpty()) {
                alert(Alert.AlertType.WARNING, "Required Fields",
                        "Fee type, amount, request type and justification are required.");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(tfAmount.getText().trim());
            } catch (NumberFormatException ex) {
                alert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid number.");
                return;
            }
            try {
                SupportController ctrl = new SupportController();
                FeeSupportRequest result = ctrl.createFeeRequest(
                        studentId,
                        FeeType.valueOf(cbFeeType.getValue()),
                        amount,
                        FeeRequestType.valueOf(cbRequestType.getValue()),
                        taJustification.getText().trim(),
                        docAttached[0]);
                alert(Alert.AlertType.INFORMATION, "Request Submitted",
                        "Your fee support request has been submitted.\nTracking ID: " + result.getRequestId());
                new StudentDashboard(username).show(stage);
            } catch (InvalidAmountException ex) {
                alert(Alert.AlertType.ERROR, "Invalid Amount", ex.getMessage());
            } catch (business.shared.exceptions.EligibilityException eex) {
                alert(Alert.AlertType.WARNING, "Not Eligible", eex.getMessage());
            } catch (Exception ex) {
                alert(Alert.AlertType.ERROR, "Submission Failed", ex.getMessage());
            }
        });

        VBox formCard = ViewHelper.createFormCard(grid, submitBtn, "Manage Fee Support Request");

        ScrollPane scroll = new ScrollPane(formCard);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Fee Support Request");
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