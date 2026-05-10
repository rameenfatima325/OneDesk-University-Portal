package ui.common;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public final class SupportingDocDialog {

    private SupportingDocDialog() {}

    public static String show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Supporting Document");
        dialog.initStyle(StageStyle.UTILITY);

        boolean dark = Theme.isDark();
        String headingFill = dark ? "#F1F5F9" : "#0F172A";
        String subFill     = dark ? "#94A3B8" : "#64748B";
        String labelFill   = dark ? "#CBD5E1" : "#334155";

        Label header = new Label("Attach Supporting Document");
        header.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:" + headingFill + ";");
        Label sub = new Label("Provide the document type and a file name or short description.");
        sub.setStyle("-fx-font-size:12px; -fx-text-fill:" + subFill + ";");
        sub.setWrapText(true);

        Label typeLabel = new Label("Document Type");
        typeLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + labelFill + ";");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(
                "Medical Certificate",
                "Transcript / Result Sheet",
                "ID Card / NIC Copy",
                "Bank Statement",
                "Income Certificate",
                "Affidavit",
                "Email Screenshot",
                "Police Report",
                "Other");
        typeBox.setPromptText("Select document type");
        typeBox.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label("Document Name");
        nameLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + labelFill + ";");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g.  medical_certificate_april.pdf");

        Label err = new Label();
        err.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        err.setMinHeight(16);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-padding:8 18; -fx-cursor:hand;");
        Button addBtn = new Button("Attach");
        addBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; "
                + "-fx-font-weight:bold; -fx-padding:8 22; -fx-cursor:hand; -fx-background-radius:6;");
        addBtn.setDefaultButton(true);

        HBox actions = new HBox(10, cancelBtn, addBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Use a single-element array so the lambdas can write to it
        final String[] result = { null };

        cancelBtn.setOnAction(e -> dialog.close());
        addBtn.setOnAction(e -> {
            String type = typeBox.getValue();
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            if (type == null || type.isBlank()) { err.setText("Please choose a document type."); return; }
            if (name.isBlank())                 { err.setText("Please enter a document name.");  return; }
            result[0] = type.toUpperCase() + ": " + name;
            dialog.close();
        });

        VBox root = new VBox(14,
                header, sub,
                new VBox(6, typeLabel, typeBox),
                new VBox(6, nameLabel, nameField),
                err,
                actions);
        root.setPadding(new Insets(28));
        root.setMinWidth(440);

        Scene scene = new Scene(root);
        Theme.apply(scene);
        Theme.applyWindowIcon(dialog);
        dialog.setScene(scene);
        dialog.showAndWait();

        return result[0];
    }
}