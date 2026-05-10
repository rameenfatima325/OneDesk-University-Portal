package ui.document;

import business.document.DegreeVerificationRequest;
import business.document.DocumentRequestController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DegreeVerificationView {
    private final String username;

    public DegreeVerificationView(String username) { this.username = username; }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new DegreeVerificationView(username).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        TextField yearField = ViewHelper.styledField("e.g. 2024");
        yearField.setText("2024");
        ViewHelper.addRow(grid, 0, "Graduation Year", yearField);

        ComboBox<String> degreeBox = ViewHelper.styledCombo();
        degreeBox.getItems().addAll("BS Computer Science", "BS Artificial Intelligence", "BS Cyber Security", "BS Data Science", "BS Software Engineering", "BS Electrical Engineering", "BS Civil Engineering", "BS Mechanical Engineering", "BBA", "MBA", "MS Computer Science", "MS Software Engineering", "PhD Computer Science", "Others");
        degreeBox.setPromptText("Select degree...");
        ViewHelper.addRow(grid, 1, "Degree Program", degreeBox);

        TextField specifyDegree = ViewHelper.styledField("Enter degree name...");
        Label specifyDegreeLabel = ViewHelper.fieldLabel("Specify Degree");
        specifyDegreeLabel.setVisible(false); specifyDegreeLabel.setManaged(false);
        specifyDegree.setVisible(false); specifyDegree.setManaged(false);
        grid.add(specifyDegreeLabel, 0, 2); grid.add(specifyDegree, 1, 2);

        ComboBox<String> purposeBox = ViewHelper.styledCombo();
        purposeBox.getItems().addAll(
                "Higher Studies (Local)", "Higher Studies (Abroad)", "Employment Verification",
                "Visa Processing", "HEC Attestation", "Others");
        purposeBox.setPromptText("Select purpose...");
        ViewHelper.addRow(grid, 3, "Purpose", purposeBox);

        TextField specifyPurpose = ViewHelper.styledField("Enter purpose details...");
        Label specifyPurposeLabel = ViewHelper.fieldLabel("Specify Purpose");
        specifyPurposeLabel.setVisible(false); specifyPurposeLabel.setManaged(false);
        specifyPurpose.setVisible(false); specifyPurpose.setManaged(false);
        grid.add(specifyPurposeLabel, 0, 4); grid.add(specifyPurpose, 1, 4);

        ComboBox<String> typeBox = ViewHelper.styledCombo();
        typeBox.getItems().addAll("HEC", "Embassy", "Employer", "IBCC");
        typeBox.setPromptText("Select authority...");
        ViewHelper.addRow(grid, 5, "Verification Authority", typeBox);

        degreeBox.valueProperty().addListener((o, old, v) -> {
            boolean isOther = "Others".equals(v);
            specifyDegreeLabel.setVisible(isOther); specifyDegreeLabel.setManaged(isOther);
            specifyDegree.setVisible(isOther); specifyDegree.setManaged(isOther);
            stage.sizeToScene();
        });
        purposeBox.valueProperty().addListener((o, old, v) -> {
            boolean isOther = "Others".equals(v);
            specifyPurposeLabel.setVisible(isOther); specifyPurposeLabel.setManaged(isOther);
            specifyPurpose.setVisible(isOther); specifyPurpose.setManaged(isOther);
            stage.sizeToScene();
        });

        Button submitBtn = ViewHelper.solidBtn("Submit Request", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (degreeBox.getValue() == null || purposeBox.getValue() == null || typeBox.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Required Fields", "Please fill all required fields.");
                return;
            }

            String finalDegree  = "Others".equals(degreeBox.getValue())  ? specifyDegree.getText()  : degreeBox.getValue();
            String finalPurpose = "Others".equals(purposeBox.getValue()) ? specifyPurpose.getText() : purposeBox.getValue();

            if (finalDegree.trim().isEmpty() || finalPurpose.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Detail Required", "Please specify details for 'Others' selection.");
                return;
            }

            // Validate graduation year — must be a 4-digit number and not in the future.
            int gradYear;
            try {
                gradYear = Integer.parseInt(yearField.getText().trim());
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Invalid Year",
                        "Graduation year must be a valid 4-digit number.");
                return;
            }
            int currentYear = java.time.Year.now().getValue();
            if (gradYear < 1980 || gradYear > currentYear) {
                showAlert(Alert.AlertType.WARNING, "Invalid Graduation Year",
                        "Graduation year must be between 1980 and " + currentYear + ".\n"
                                + "A degree cannot be verified before it has been awarded.");
                return;
            }

            try {
                DocumentRequestController controller = new DocumentRequestController();

                String authorityType = typeBox.getValue().toUpperCase();

                int studentId = new db.document.UserDAO().getStudentIdByUsername(username);
                if (studentId <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Account Error",
                            "Could not resolve your student record. Please contact admin.");
                    return;
                }

                DegreeVerificationRequest res = controller.handleDegreeVerification(
                        studentId, gradYear, finalDegree,
                        authorityType, finalPurpose, 1, false);
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

        VBox formCardWrapper = ViewHelper.createFormCard(grid, submitBtn, "Degree Verification");

        ScrollPane scroll = new ScrollPane(formCardWrapper);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Degree Verification");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene); // Updated to standard window size
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}