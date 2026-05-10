package ui.document;

import business.document.TranscriptRequest;
import business.document.DocumentRequestController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TranscriptView {
    private final String username;

    public TranscriptView(String username) { this.username = username; }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new TranscriptView(username).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER); // Center align form contents

        ComboBox<String> typeBox = ViewHelper.styledCombo();
        typeBox.getItems().addAll("Official Sealed", "Unofficial");
        typeBox.setPromptText("Select type...");
        ViewHelper.addRow(grid, 0, "Transcript Type", typeBox);

        ComboBox<String> purposeBox = ViewHelper.styledCombo();
        purposeBox.getItems().addAll("Job Application", "Higher Studies", "Visa", "Other");
        purposeBox.setPromptText("Select purpose...");
        ViewHelper.addRow(grid, 1, "Purpose", purposeBox);

        TextField reasonField = ViewHelper.styledField("Specify reason...");
        Label reasonLabel = ViewHelper.fieldLabel("Specify Reason");
        reasonLabel.setVisible(false); reasonLabel.setManaged(false);
        reasonField.setVisible(false); reasonField.setManaged(false);
        grid.add(reasonLabel, 0, 2); grid.add(reasonField, 1, 2);

        ComboBox<String> modeBox = ViewHelper.styledCombo();
        modeBox.getItems().addAll("Pickup", "Postal");
        modeBox.setValue("Pickup");
        ViewHelper.addRow(grid, 3, "Delivery Mode", modeBox);

        TextField addressField = ViewHelper.styledField("Full mailing address...");
        Label addrLabel = ViewHelper.fieldLabel("Mailing Address");
        addrLabel.setVisible(false); addrLabel.setManaged(false);
        addressField.setVisible(false); addressField.setManaged(false);
        grid.add(addrLabel, 0, 4); grid.add(addressField, 1, 4);

        purposeBox.valueProperty().addListener((o, old, v) -> {
            boolean isOther = "Other".equals(v);
            reasonLabel.setVisible(isOther); reasonLabel.setManaged(isOther);
            reasonField.setVisible(isOther); reasonField.setManaged(isOther);
        });

        modeBox.valueProperty().addListener((o, old, v) -> {
            boolean isPostal = "Postal".equals(v);
            addrLabel.setVisible(isPostal); addrLabel.setManaged(isPostal);
            addressField.setVisible(isPostal); addressField.setManaged(isPostal);
        });

        Button submitBtn = ViewHelper.solidBtn("Submit Request", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (purposeBox.getValue() == null || typeBox.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Required Fields", "Please fill all required fields.");
                return;
            }
            if ("Other".equals(purposeBox.getValue()) && reasonField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Required", "Please specify reason.");
                return;
            }
            if ("Postal".equals(modeBox.getValue()) && addressField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Required", "Address is mandatory for Postal delivery.");
                return;
            }

            try {
                DocumentRequestController controller = new DocumentRequestController();

                String backendType = typeBox.getValue().toUpperCase().replace(" ", "_");
                String backendMode = modeBox.getValue().toUpperCase();

                String finalPurpose = "Other".equals(purposeBox.getValue())
                        ? reasonField.getText()
                        : purposeBox.getValue().toUpperCase().replace(" ", "_");

                int sid = new db.document.UserDAO().getStudentIdByUsername(username);
                if (sid <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Account Error",
                            "Could not resolve your student record. Please contact admin.");
                    return;
                }
                TranscriptRequest result = controller.handleTranscriptRequest(
                        sid, backendType, finalPurpose, 1, backendMode, addressField.getText());

                if (result != null) {
                    String extra = (result.getStatus() == business.shared.RequestStatus.PENDING_VERIFICATION)
                            ? "\n\nThis request requires fee verification. The Finance Department "
                            + "will verify your fee voucher before processing begins."
                            : "";
                    showAlert(Alert.AlertType.INFORMATION, "Request Submitted",
                            "Your transcript request has been submitted.\nTracking ID: "
                                    + result.getTrackingId() + extra);
                    new StudentDashboard(username).show(stage);
                }
            } catch (business.shared.exceptions.EligibilityException ex) {
                showAlert(Alert.AlertType.WARNING, "Not Eligible", ex.getMessage());
            }
        });

        VBox formCardWrapper = ViewHelper.createFormCard(grid, submitBtn, "Request Transcript");

        ScrollPane scroll = new ScrollPane(formCardWrapper);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Transcript Request");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}