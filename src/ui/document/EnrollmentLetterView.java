package ui.document;

import business.document.EnrollmentLetterRequest;
import business.document.DocumentRequestController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EnrollmentLetterView {
    private final String username;

    public EnrollmentLetterView(String username) { this.username = username; }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new EnrollmentLetterView(username).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        ComboBox<String> typeBox = ViewHelper.styledCombo();
        typeBox.getItems().addAll("Enrollment Confirmation", "Student Status", "Bonafide", "Bank Letter", "Visa Support");
        typeBox.setPromptText("Select letter type...");
        ViewHelper.addRow(grid, 0, "Letter Type", typeBox);

        // Purpose
        ComboBox<String> purposeBox = ViewHelper.styledCombo();
        purposeBox.getItems().addAll("Internship Application", "Bank Account Opening", "Visa Application", "Discount/Concession", "Others");
        purposeBox.setPromptText("Select purpose...");
        ViewHelper.addRow(grid, 1, "Purpose", purposeBox);

        TextField specifyPurpose = ViewHelper.styledField("Specify purpose...");
        Label specifyLabel = ViewHelper.fieldLabel("Specify Purpose");
        specifyLabel.setVisible(false); specifyLabel.setManaged(false);
        specifyPurpose.setVisible(false); specifyPurpose.setManaged(false);
        grid.add(specifyLabel, 0, 2); grid.add(specifyPurpose, 1, 2);

        TextField institutionField = ViewHelper.styledField("e.g. HBL Bank, Embassy of France");
        ViewHelper.addRow(grid, 3, "To (Institution)", institutionField);

        ComboBox<String> langBox = ViewHelper.styledCombo();
        langBox.getItems().addAll("English", "Urdu");
        langBox.setValue("English");
        ViewHelper.addRow(grid, 4, "Language", langBox);

        purposeBox.valueProperty().addListener((o, old, v) -> {
            boolean isOther = "Others".equals(v);
            specifyLabel.setVisible(isOther); specifyLabel.setManaged(isOther);
            specifyPurpose.setVisible(isOther); specifyPurpose.setManaged(isOther);
            stage.sizeToScene();
        });

        Button submitBtn = ViewHelper.solidBtn("Submit Request", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (typeBox.getValue() == null || purposeBox.getValue() == null
                    || institutionField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Required Fields",
                        "Please fill Type, Purpose, and Institution.");
                return;
            }

            String finalPurpose = "Others".equals(purposeBox.getValue())
                    ? specifyPurpose.getText() : purposeBox.getValue();

            if (finalPurpose.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Detail Required", "Please specify the 'Other' purpose.");
                return;
            }

            try {
                DocumentRequestController controller = new DocumentRequestController();

                String backendLetterType = typeBox.getValue().toUpperCase().replace(" ", "_");
                String backendLang = langBox.getValue().toUpperCase();

                int sid = new db.document.UserDAO().getStudentIdByUsername(username);
                if (sid <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Account Error",
                            "Could not resolve your student record. Please contact admin.");
                    return;
                }
                EnrollmentLetterRequest res = controller.handleEnrollmentLetterRequest(
                        sid, backendLetterType, institutionField.getText(),
                        backendLang, finalPurpose, 1);

                if (res != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Request Submitted",
                            "Your request has been submitted.\nTracking ID: " + res.getTrackingId());
                    new StudentDashboard(username).show(stage);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Submission Failed",
                            "We could not submit your request. Please try again.");
                }
            } catch (business.shared.exceptions.EligibilityException ex) {
                showAlert(Alert.AlertType.WARNING, "Not Eligible", ex.getMessage());
            }
        });

        VBox formCardWrapper = ViewHelper.createFormCard(grid, submitBtn, "Enrollment Letter");

        ScrollPane scroll = new ScrollPane(formCardWrapper);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Enrollment Letter");
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