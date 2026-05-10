package ui.academic;

import business.academic.AcademicRequestController;
import business.academic.AddDropRequest;
import business.shared.exceptions.DeadlineExceededException;
import ui.document.StudentDashboard;
import ui.document.ViewHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;


public class AddDropView {

    private final String username;
    private final int    studentId;

    private AddDropRequest.RequestType selectedType = AddDropRequest.RequestType.WITHDRAW;

    public AddDropView(String username, int studentId) {
        this.username  = username;
        this.studentId = studentId;
    }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new AddDropView(username, studentId).show(stage));

        LocalDate deadline = AddDropRequest.getDeadline();
        boolean within = !LocalDate.now().isAfter(deadline);

        Label deadlineBanner = new Label(within
                ? "⏱   Add/Drop window is OPEN — closes on " + deadline
                : "⚠   Add/Drop window has CLOSED (" + deadline + ") — only Withdrawal is allowed.");
        deadlineBanner.setMaxWidth(Double.MAX_VALUE);
        deadlineBanner.setPadding(new Insets(12, 18, 12, 18));
        deadlineBanner.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:white; "
                + "-fx-background-radius:8; -fx-background-color:"
                + (within ? ViewHelper.ACCENT : ViewHelper.WARNING) + ";");

        Label typeSectionLabel = new Label("What would you like to do?");
        typeSectionLabel.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"
                + ViewHelper.TEXT_DARK() + ";");

        VBox addCard      = buildOptionCard("ADD",      "Add a Course",
                "Register for an additional course this semester", !within);
        VBox dropCard     = buildOptionCard("DROP",     "Drop a Course",
                "Remove a course from your current semester", !within);
        VBox withdrawCard = buildOptionCard("WITHDRAW", "Withdraw from Semester",
                "Fully withdraw from all courses this semester", false);

        VBox optionsColumn = new VBox(10, addCard, dropCard, withdrawCard);
        optionsColumn.setFillWidth(true);

        Label courseLabel = new Label("Course Code");
        courseLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"
                + ViewHelper.TEXT_DARK() + ";");
        TextField codeField = ViewHelper.styledField("e.g. CS2009  (not needed for Withdraw)");

        Label reasonLabel = new Label("Reason / Justification");
        reasonLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"
                + ViewHelper.TEXT_DARK() + ";");
        TextArea reasonArea = new TextArea();
        reasonArea.setPrefRowCount(4);
        reasonArea.setWrapText(true);
        reasonArea.setStyle(ViewHelper.FIELD_STYLE());

        Label docLabel = new Label("Supporting Document");
        docLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"
                + ViewHelper.TEXT_DARK() + ";");
        final String[] docAttached = { "" };           // updated by the dialog
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
        docRow.setAlignment(Pos.CENTER_LEFT);

        Runnable refreshHints = () -> {
            switch (selectedType) {
                case ADD -> {
                    codeField.setDisable(false);
                    codeField.setPromptText("e.g. CS2009");
                    reasonArea.setPromptText("Why do you need to add this course? (min. 1 character)");
                }
                case DROP -> {
                    codeField.setDisable(false);
                    codeField.setPromptText("e.g. CS2009");
                    reasonArea.setPromptText("Explain why you need to drop this course (min. 10 characters)");
                }
                case WITHDRAW -> {
                    codeField.setDisable(true);
                    codeField.clear();
                    codeField.setPromptText("Not required for Withdrawal");
                    reasonArea.setPromptText("Provide formal justification for withdrawing (min. 20 characters)");
                }
            }
        };

        wireCardClick(addCard,      AddDropRequest.RequestType.ADD,      refreshHints, addCard, dropCard, withdrawCard);
        wireCardClick(dropCard,     AddDropRequest.RequestType.DROP,     refreshHints, addCard, dropCard, withdrawCard);
        wireCardClick(withdrawCard, AddDropRequest.RequestType.WITHDRAW, refreshHints, addCard, dropCard, withdrawCard);

        paintSelected(withdrawCard);
        paintUnselected(addCard, !within);
        paintUnselected(dropCard, !within);
        refreshHints.run();

        Button submitBtn = ViewHelper.solidBtn("Submit Request", ViewHelper.ACCENT);
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setPrefHeight(44);

        submitBtn.setOnAction(e -> {
            AddDropRequest.RequestType type = selectedType;

            if (type != AddDropRequest.RequestType.WITHDRAW
                    && codeField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Required",
                        "Course code is required for Add / Drop.");
                return;
            }
            if (reasonArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Required",
                        "Reason / Justification is required.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Submission");
            confirm.setHeaderText("Submit " + prettyName(type) + " request?");
            confirm.setContentText("This action will be logged and routed to the Academic Office.");
            var choice = confirm.showAndWait();
            if (choice.isEmpty() || choice.get() != ButtonType.OK) return;

            AcademicRequestController controller = new AcademicRequestController();
            try {
                AddDropRequest result = controller.handleAddDropWithdrawal(
                        studentId, type,
                        codeField.getText().trim(),
                        reasonArea.getText().trim(),
                        docAttached[0]);

                if (result != null) {
                    String extra = "";
                    if (result.getStatus() == business.shared.RequestStatus.FLAGGED_FOR_REVIEW) {
                        extra = "\n\nNote: this request was submitted after the add/drop window "
                                + "and has been flagged for special administrative review.";
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Request Submitted",
                            prettyName(type) + " request submitted successfully.\n"
                                    + "Tracking ID: " + result.getTrackingId() + extra);
                    new StudentDashboard(username).show(stage);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Validation Failed",
                            "Please review your inputs — each request type has specific requirements.");
                }
            } catch (business.shared.exceptions.EligibilityException eex) {
                showAlert(Alert.AlertType.WARNING, "Not Eligible", eex.getMessage());
            }
        });

        Label formTitle = new Label("Submit Add / Drop / Withdrawal Request");
        formTitle.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:"
                + ViewHelper.TEXT_DARK() + ";");

        VBox card = new VBox(18,
                formTitle,
                deadlineBanner,
                typeSectionLabel, optionsColumn,
                courseLabel,      codeField,
                reasonLabel,      reasonArea,
                docLabel,         docRow,
                submitBtn);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(32));
        card.setStyle("-fx-background-color:" + ViewHelper.SURFACE() + "; "
                + "-fx-border-color:" + ViewHelper.BORDER() + "; "
                + "-fx-border-radius:12; -fx-background-radius:12;");
        card.setEffect(new javafx.scene.effect.DropShadow(10, 0, 4,
                javafx.scene.paint.Color.rgb(0, 0, 0, 0.05)));
        card.setMaxWidth(640);

        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(30));
        wrapper.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Add / Drop / Withdrawal");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }


    private VBox buildOptionCard(String code, String title, String subtitle, boolean disabled) {
        // Radio indicator (drawn as a labeled circle via CSS)
        Label dot = new Label();
        dot.setPrefSize(18, 18); dot.setMinSize(18, 18); dot.setMaxSize(18, 18);
        dot.setId("dot-" + code); // so we can find it later to re-style
        dot.setStyle("-fx-background-radius:9; -fx-background-color:" + ViewHelper.SURFACE() + "; "
                + "-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-width:2; -fx-border-radius:9;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"
                + ViewHelper.TEXT_DARK() + ";");

        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + ViewHelper.MUTED() + ";");
        subLbl.setWrapText(true);

        VBox textCol = new VBox(2, titleLbl, subLbl);
        textCol.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(14, dot, textCol);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox cardBox = new VBox(row);
        cardBox.setId("card-" + code);
        cardBox.setPadding(new Insets(14, 18, 14, 18));
        cardBox.setStyle(cardBaseStyle(false));
        cardBox.setMaxWidth(Double.MAX_VALUE);
        cardBox.setDisable(disabled);

        if (disabled) cardBox.setOpacity(0.45);

        return cardBox;
    }

    private void wireCardClick(VBox card, AddDropRequest.RequestType type, Runnable onChange,
                               VBox... allCards) {
        card.setOnMouseClicked(e -> {
            if (card.isDisabled()) return;
            selectedType = type;
            for (VBox c : allCards) paintUnselected(c, c.isDisabled());
            paintSelected(card);
            onChange.run();
        });

        // Nice hover effect
        card.setOnMouseEntered(e -> {
            if (!card.isDisabled() && !isSelected(card))
                card.setStyle(cardHoverStyle());
        });
        card.setOnMouseExited(e -> {
            if (!card.isDisabled() && !isSelected(card))
                card.setStyle(cardBaseStyle(false));
        });
    }

    private void paintSelected(VBox card) {
        card.setStyle(cardBaseStyle(true));
        // fill the dot
        Label dot = (Label) card.lookup("#dot-" + stripPrefix(card.getId()));
        if (dot != null) {
            dot.setStyle("-fx-background-radius:9; -fx-background-color:" + ViewHelper.ACCENT + "; "
                    + "-fx-border-color:" + ViewHelper.ACCENT + "; -fx-border-width:2; -fx-border-radius:9;");
        }
    }

    private void paintUnselected(VBox card, boolean disabled) {
        card.setStyle(cardBaseStyle(false));
        card.setOpacity(disabled ? 0.45 : 1.0);
        Label dot = (Label) card.lookup("#dot-" + stripPrefix(card.getId()));
        if (dot != null) {
            dot.setStyle("-fx-background-radius:9; -fx-background-color:" + ViewHelper.SURFACE() + "; "
                    + "-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-width:2; -fx-border-radius:9;");
        }
    }

    private boolean isSelected(VBox card) {
        return card.getStyle().contains(ViewHelper.ACCENT.toLowerCase())
                || card.getStyle().contains(ViewHelper.ACCENT.toUpperCase());
    }

    private String cardBaseStyle(boolean selected) {
        String border = selected ? ViewHelper.ACCENT : ViewHelper.BORDER();
        String bg;
        if (selected) bg = ui.common.Theme.isDark() ? "#172554" : "#EFF6FF";
        else          bg = ViewHelper.SURFACE();
        int width     = selected ? 2 : 1;
        return "-fx-background-color:" + bg + "; "
                + "-fx-border-color:" + border + "; "
                + "-fx-border-width:" + width + "; "
                + "-fx-border-radius:8; -fx-background-radius:8; "
                + "-fx-cursor:hand;";
    }

    private String cardHoverStyle() {
        String hoverBg = ui.common.Theme.isDark() ? "#1E293B" : "#F9FAFB";
        return "-fx-background-color:" + hoverBg + "; "
                + "-fx-border-color:" + ViewHelper.ACCENT + "; "
                + "-fx-border-width:1; "
                + "-fx-border-radius:8; -fx-background-radius:8; "
                + "-fx-cursor:hand;";
    }

    private String stripPrefix(String id) {
        return id == null ? "" : id.replace("card-", "");
    }

    private String prettyName(AddDropRequest.RequestType t) {
        return switch (t) {
            case ADD      -> "Add Course";
            case DROP     -> "Drop Course";
            case WITHDRAW -> "Withdraw Semester";
        };
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}