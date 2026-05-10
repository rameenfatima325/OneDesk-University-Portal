package ui.document;

import db.document.UserDAO;
import ui.common.Theme;
import ui.academic.AttendanceCorrectionView;
import ui.academic.CourseRegistrationView;
import ui.academic.AddDropView;
import ui.academic.RecordCorrectionView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class StudentDashboard {

    private final String username;
    private final int    studentId;

    public StudentDashboard(String username) {
        this.username  = username;
        int resolved   = new UserDAO().getStudentIdByUsername(username);
        this.studentId = (resolved > 0) ? resolved : -1;
    }

    public void show(Stage stage) {
        Theme.applyWindowIcon(stage);
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(0, 30, 0, 30));
        topBar.setPrefHeight(64); topBar.setMinHeight(64);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(
                "-fx-background-color:" + ViewHelper.SURFACE() + "; " +
                        "-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-width:0 0 1 0;");

        Label appName = new Label("OneDesk");
        appName.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.ACCENT + ";");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userBadge = new Label("● " + username);
        userBadge.setStyle("-fx-font-size:13px; -fx-text-fill:" + ViewHelper.TEXT_BODY() + ";");

        Button logoutBtn = outlineBtn("Logout");
        logoutBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Logout");
            confirm.setHeaderText("Are you sure you want to logout?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) new LoginView().start(stage);
            });
        });

        Button modeBtn = new Button(Theme.isDark() ? "☀" : "🌙");
        modeBtn.getStyleClass().add("mode-toggle");
        modeBtn.setStyle("-fx-font-size:14px; -fx-padding:6 12; -fx-cursor:hand;");
        modeBtn.setOnAction(e -> { Theme.toggleDark(); show(stage); });

        topBar.getChildren().addAll(appName, spacer, userBadge, gap(12), modeBtn, gap(8), logoutBtn);

        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 0, 32, 0));
        content.setMaxWidth(600);

        Label pageTitle = new Label("Welcome, " + username);
        pageTitle.setStyle("-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.TEXT_DARK() + ";");
        Label subtitle = new Label("What would you like to do today?");
        subtitle.setStyle("-fx-font-size:14px; -fx-text-fill:" + ViewHelper.MUTED() + ";");
        VBox heading = new VBox(4, pageTitle, subtitle);

        /* ============= Academic Requests (UC1–UC4) — Zahra ============= */
        Label sec1 = sectionLabel("Academic Requests");

        Button uc1Btn = menuBtn("Submit Attendance Correction (UC1)");
        Button uc2Btn = menuBtn("Submit Course Registration Issue (UC2)");
        Button uc3Btn = menuBtn("Submit Add / Drop / Withdrawal (UC3)");
        Button uc4Btn = menuBtn("Submit Academic Record Correction (UC4)");

        uc1Btn.setOnAction(e -> new AttendanceCorrectionView(username, studentId).show(stage));
        uc2Btn.setOnAction(e -> new CourseRegistrationView (username, studentId).show(stage));
        uc3Btn.setOnAction(e -> new AddDropView            (username, studentId).show(stage));
        uc4Btn.setOnAction(e -> new RecordCorrectionView   (username, studentId).show(stage));

        VBox academicCard = card(sec1, uc1Btn, uc2Btn, uc3Btn, uc4Btn);

        /* ============= Support Requests (UC5–UC8) — Aizah ============= */
        Label sec_aizah = sectionLabel("Support Requests");

        Button uc5Btn = menuBtn("Manage Fee Support Request (UC5)");
        Button uc6Btn = menuBtn("Manage Lost and Found (UC6)");
        Button uc7Btn = menuBtn("Submit Scholarship / Financial Aid Query (UC7)");
        Button uc8Btn = menuBtn("Submit General Complaint (UC8)");

        uc5Btn.setOnAction(e -> new ui.support.FeeSupportScreen(username, studentId).show(stage));
        uc6Btn.setOnAction(e -> new ui.support.LostFoundScreen(username, studentId).show(stage));
        uc7Btn.setOnAction(e -> new ui.support.ScholarshipScreen(username, studentId).show(stage));
        uc8Btn.setOnAction(e -> new ui.support.ComplaintScreen(username, studentId).show(stage));

        VBox supportCard = card(sec_aizah, uc5Btn, uc6Btn, uc7Btn, uc8Btn);

        /* ============= Document Requests (UC9–UC12) — Rameen ============= */
        Label sec2 = sectionLabel("Document Requests");

        Button uc9Btn  = menuBtn("Request Transcript (UC9)");
        Button uc10Btn = menuBtn("Request Degree Verification (UC10)");
        Button uc11Btn = menuBtn("Request Enrollment Letter (UC11)");
        Button uc12Btn = menuBtn("Request ID Card (UC12)");

        uc9Btn .setOnAction(e -> new TranscriptView(username).show(stage));
        uc10Btn.setOnAction(e -> new DegreeVerificationView(username).show(stage));
        uc11Btn.setOnAction(e -> new EnrollmentLetterView(username).show(stage));
        uc12Btn.setOnAction(e -> new IDCardView(username).show(stage));

        VBox docsCard = card(sec2, uc9Btn, uc10Btn, uc11Btn, uc12Btn);

        /* ============= Tracking ============= */
        Label sec3 = sectionLabel("Track Your Requests");

        Button historyBtn = ViewHelper.solidBtn("View Request History & Remarks", ViewHelper.ACCENT);
        historyBtn.setMaxWidth(Double.MAX_VALUE);
        historyBtn.setOnAction(e -> new StudentHistoryView(username).show(stage));

        VBox trackCard = card(sec3, historyBtn);

        content.getChildren().addAll(heading, academicCard, supportCard, docsCard, trackCard);

        VBox contentWrapper = new VBox(content);
        contentWrapper.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(contentWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");

        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        stage.setTitle("OneDesk — Student Dashboard");
        Scene scene = new Scene(root, 900, 700);
        Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private VBox card(Label sectionLabel, Button... buttons) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(20));
        box.setStyle(
                "-fx-background-color:" + ViewHelper.SURFACE() + "; " +
                        "-fx-border-color:" + ViewHelper.BORDER() + "; " +
                        "-fx-border-radius:10; -fx-background-radius:10;");
        box.setEffect(new javafx.scene.effect.DropShadow(6, 0, 2, Color.rgb(0, 0, 0, 0.06)));
        box.getChildren().add(sectionLabel);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:" + ViewHelper.BORDER() + ";");
        box.getChildren().add(sep);

        for (Button b : buttons) box.getChildren().add(b);
        return box;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.MUTED() + ";");
        return l;
    }

    private Button menuBtn(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(
                "-fx-background-color:transparent; -fx-text-fill:" + ViewHelper.TEXT_BODY() + "; " +
                        "-fx-font-size:14px; -fx-padding:10 14; -fx-alignment:CENTER_LEFT; " +
                        "-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-radius:6; " +
                        "-fx-background-radius:6; -fx-cursor:hand;");

        String hoverBg = ui.common.Theme.isDark() ? "#1E293B" : "#F9FAFB";
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("transparent", hoverBg)));
        b.setOnMouseExited (e -> b.setStyle(b.getStyle().replace(hoverBg, "transparent")));
        return b;
    }

    private Button outlineBtn(String text) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color:transparent; -fx-text-fill:" + ViewHelper.TEXT_BODY() + "; " +
                        "-fx-font-size:13px; -fx-padding:8 18; -fx-border-radius:6; " +
                        "-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-width:1; -fx-cursor:hand;");
        return b;
    }

    private Region gap(double size) {
        Region r = new Region(); r.setPrefWidth(size); r.setPrefHeight(size); return r;
    }
}