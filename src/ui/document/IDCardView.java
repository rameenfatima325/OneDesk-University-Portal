package ui.document;

import business.document.DocumentRequestController;
import business.document.IDCardRequest;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class IDCardView {
    private final String username;
    private double currentFee = 1000.0;

    public IDCardView(String username) { this.username = username; }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new IDCardView(username).show(stage));

        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        ComboBox<String> typeBox = ViewHelper.styledCombo();
        typeBox.getItems().addAll("New Issuance", "Replacement");
        typeBox.setPromptText("Select type...");
        ViewHelper.addRow(grid, 0, "Request Type", typeBox);

        ComboBox<String> reasonBox = ViewHelper.styledCombo();
        reasonBox.getItems().addAll("Lost", "Damaged", "Stolen");
        reasonBox.setPromptText("Select reason...");
        Label reasonLabel = ViewHelper.fieldLabel("Reason");
        reasonLabel.setVisible(false); reasonLabel.setManaged(false);
        reasonBox.setVisible(false); reasonBox.setManaged(false);
        grid.add(reasonLabel, 0, 1); grid.add(reasonBox, 1, 1);

        Label feeValue = new Label("Rs. 1,000");
        feeValue.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.SUCCESS + ";");
        ViewHelper.addRow(grid, 2, "Applicable Fee", feeValue);

        typeBox.valueProperty().addListener((o, old, v) -> {
            boolean isReplacement = "Replacement".equals(v);
            reasonLabel.setVisible(isReplacement); reasonLabel.setManaged(isReplacement);
            reasonBox.setVisible(isReplacement); reasonBox.setManaged(isReplacement);
            currentFee = isReplacement ? 5000.0 : 1000.0;
            feeValue.setText(isReplacement ? "Rs. 5,000" : "Rs. 1,000");
            stage.sizeToScene();
        });

        Label paymentNote = new Label("ⓘ  Payment will be processed on submission.");
        paymentNote.setStyle("-fx-font-size:12px; -fx-text-fill:" + ViewHelper.MUTED() + ";");
        grid.add(paymentNote, 0, 3, 2, 1);

        Button payBtn = ViewHelper.solidBtn("Proceed to Payment", ViewHelper.WARNING);
        payBtn.setMaxWidth(Double.MAX_VALUE);
        grid.add(payBtn, 0, 4, 2, 1);

        Button submitBtn = ViewHelper.solidBtn("Submit Request", ViewHelper.SUCCESS);
        submitBtn.setDisable(true);
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        payBtn.setOnAction(e -> {
            if (typeBox.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Required", "Please select a request type first.");
                return;
            }
            try {
                int sid = new db.document.UserDAO().getStudentIdByUsername(username);

                IDCardRequest checkRequest = new IDCardRequest(sid);
                checkRequest.setRequestType(typeBox.getValue().toUpperCase().replace(" ", "_"));


                checkRequest.calculateFee();

                showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                        "Payment of Rs. " + (int) currentFee + " has been processed successfully.");

                submitBtn.setDisable(false);
                payBtn.setDisable(true);
                typeBox.setDisable(true);
                reasonBox.setDisable(true);

            } catch (business.shared.exceptions.EligibilityException ex) {
                showAlert(Alert.AlertType.ERROR, "Ineligible", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + ex.getMessage());
            }
        });

        submitBtn.setOnAction(e -> {
            try {
                DocumentRequestController controller = new DocumentRequestController();

                String backendType = typeBox.getValue().toUpperCase().replace(" ", "_");
                String backendReason = reasonBox.getValue() != null ? reasonBox.getValue().toUpperCase() : "N/A";

                int sid = new db.document.UserDAO().getStudentIdByUsername(username);
                if (sid <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Account Error",
                            "Could not resolve your student record. Please contact admin.");
                    return;
                }
                IDCardRequest res = controller.handleIDCardRequest(
                        sid, backendType, backendReason, "N/A");

                if (res != null) {
                    String extra = (res.getStatus() == business.shared.RequestStatus.PENDING_VERIFICATION)
                            ? ""
                            : "";
                    showAlert(Alert.AlertType.INFORMATION, "Request Submitted",
                            "Your ID card request has been submitted.\nTracking ID: "
                                    + res.getTrackingId() + extra);
                    new StudentDashboard(username).show(stage);
                }
            } catch (business.shared.exceptions.EligibilityException ex) {
                showAlert(Alert.AlertType.WARNING, "Not Eligible", ex.getMessage());
            }
        });

        VBox formCardWrapper = ViewHelper.createFormCard(grid, submitBtn, "ID Card Request");

        ScrollPane scroll = new ScrollPane(formCardWrapper);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — ID Card Request");
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