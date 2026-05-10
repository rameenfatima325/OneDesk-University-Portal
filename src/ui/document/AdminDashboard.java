package ui.document;

import business.admin.*;
import business.shared.RequestStatus;
import ui.common.Theme;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboard {
    private final String username;
    private final AdminController controller = new AdminController();
    private StackPane rootStack;
    private VBox mainDashboardView;
    private TableView<AdminRequestModel> mainTable;

    public static final String DEL_PROCESSING   = "PROCESSING";
    public static final String DEL_IN_TRANSIT   = "IN TRANSIT";
    public static final String DEL_DELIVERED    = "DELIVERED";
    public static final String DEL_READY        = "READY FOR PICKUP";
    public static final String DEL_PICKED_UP    = "PICKED UP";

    public AdminDashboard(String username) { this.username = username; }

    public void show(Stage stage) {
        Theme.applyWindowIcon(stage);
        rootStack = new StackPane();
        createMainDashboardView(stage);
        rootStack.getChildren().add(mainDashboardView);
        stage.setTitle("OneDesk — Admin Portal");
        Scene scene = new Scene(rootStack, 1150, 720);
        Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void createMainDashboardView(Stage stage) {
        mainDashboardView = new VBox();
        mainDashboardView.setStyle("-fx-background-color: " + ViewHelper.BG() + ";");

        HBox topBar = ViewHelper.topBar(stage, username, () -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Logout");
            confirm.setHeaderText("Are you sure you want to logout?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try {
                        new LoginView().start(stage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }, () -> new AdminDashboard(username).show(stage));
        Button logoutBtn = (Button) topBar.getChildren().get(topBar.getChildren().size() - 1);
        logoutBtn.setText("Logout");


        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 30, 24, 30));
        VBox.setVgrow(content, Priority.ALWAYS);

        Label pageTitle = new Label("All Requests");
        pageTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + ViewHelper.TEXT_DARK() + ";");
        Label subtitle = new Label("Review and manage incoming requests");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + ViewHelper.MUTED() + ";");

        Button refreshBtn = ViewHelper.outlineBtn("⟳  Refresh");
        Button auditBtn   = ViewHelper.solidBtn("Audit Log", "#4B5563");

        refreshBtn.setOnAction(e -> reloadRequestsAsync());
        auditBtn.setOnAction(e -> switchToAuditLogView());

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        HBox headingRow = new HBox(12, new VBox(2, pageTitle, subtitle), hSpacer, refreshBtn, auditBtn);
        headingRow.setAlignment(Pos.CENTER_LEFT);

        mainTable = buildRequestTable();
        reloadRequestsAsync();
        VBox.setVgrow(mainTable, Priority.ALWAYS);

        HBox tableFooter = new HBox(12);
        tableFooter.setPadding(new Insets(14, 18, 14, 18));
        tableFooter.setAlignment(Pos.CENTER_RIGHT);
        tableFooter.setStyle("-fx-border-color: " + ViewHelper.BORDER() + "; -fx-border-width: 1 0 0 0;");

        Button openBtn = ViewHelper.solidBtn("Open Selection", ViewHelper.ACCENT);
        openBtn.setOnAction(e -> {
            AdminRequestModel sel = mainTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                controller.enrichWithDetails(sel);
                switchToProcessView(sel);
            } else {
                showInfo("No Selection", "Please select a request from the table first.");
            }
        });
        tableFooter.getChildren().add(openBtn);

        VBox tableCard = new VBox(0, mainTable, tableFooter);
        tableCard.setStyle("-fx-background-color: " + ViewHelper.SURFACE() + "; " + "-fx-border-color: " + ViewHelper.BORDER() + "; " + "-fx-border-radius: 10; -fx-background-radius: 10;");
        tableCard.setEffect(shadow());
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        content.getChildren().addAll(headingRow, tableCard);
        mainDashboardView.getChildren().addAll(topBar, content);
    }

    private TableView<AdminRequestModel> buildRequestTable() {
        TableView<AdminRequestModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-border-width: 0;");
        table.setFixedCellSize(46);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<AdminRequestModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("displayId"));
        idCol.setMaxWidth(90); idCol.setMinWidth(90);

        TableColumn<AdminRequestModel, String> typeCol = new TableColumn<>("Request Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<AdminRequestModel, String> dateCol = new TableColumn<>("Submitted");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateOnly"));

        TableColumn<AdminRequestModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String s = item.toUpperCase();

                String color = ViewHelper.WARNING; // Default PENDING
                if (s.equals(RequestStatus.RESOLVED.name())) color = ViewHelper.SUCCESS;
                else if (s.equals(RequestStatus.REJECTED.name())) color = ViewHelper.DANGER;
                else if (s.equals(RequestStatus.IN_REVIEW.name())) color = ViewHelper.ACCENT;
                else if (s.equals(RequestStatus.PENDING_VERIFICATION.name())) color = "#7C3AED"; // purple — awaiting fee verification
                else if (s.equals(RequestStatus.FLAGGED_FOR_REVIEW.name()))  color = "#DC2626"; // red — special review

                setGraphic(badge(s, color)); setText(null);
            }
        });

        TableColumn<AdminRequestModel, String> deliveryCol = new TableColumn<>("Delivery Status");
        deliveryCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                AdminRequestModel row = getTableRow().getItem();
                if (empty || row == null) { setText(null); setGraphic(null); return; }

                String s = row.getStatus().toUpperCase();
                if (s.equals(RequestStatus.PENDING.name()) || s.equals(RequestStatus.IN_REVIEW.name())) {
                    setGraphic(badge(DEL_PROCESSING, ViewHelper.WARNING)); setText(null); return;
                }
                if (s.equals(RequestStatus.REJECTED.name())) {
                    setGraphic(badge(RequestStatus.REJECTED.name(), ViewHelper.DANGER));  setText(null); return;
                }

                int c = row.getCollected();
                String label; String color;

                String t = row.getType();
                boolean noDelivery = row.isAcademic()
                        || "FEE_SUPPORT".equalsIgnoreCase(t)
                        || "SCHOLARSHIP".equalsIgnoreCase(t)
                        || "COMPLAINT".equalsIgnoreCase(t)
                        || "LOST_FOUND".equalsIgnoreCase(t);
                if (noDelivery) {
                    setText("—"); setGraphic(null);
                    setStyle("-fx-text-fill:" + ViewHelper.MUTED() + "; -fx-font-size:13px;");
                    return;
                }

                if (row.isPostal()) {
                    label = c == 2 ? DEL_DELIVERED : c == 1 ? DEL_IN_TRANSIT : DEL_PROCESSING;
                    color = c == 2 ? ViewHelper.SUCCESS : c == 1 ? "#8B5CF6" : ViewHelper.ACCENT;
                } else {
                    label = c == 1 ? DEL_PICKED_UP : DEL_READY;
                    color = c == 1 ? ViewHelper.SUCCESS : ViewHelper.ACCENT;
                }
                setGraphic(badge(label, color)); setText(null);
            }
        });

        TableColumn<AdminRequestModel, String> remarksCol = new TableColumn<>("Remarks");
        remarksCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                AdminRequestModel row = getTableRow().getItem();
                if (empty || row == null || row.getStatus().equalsIgnoreCase(RequestStatus.PENDING.name())) {
                    setText("—"); setGraphic(null); return;
                }
                Label link = new Label("View →");
                link.setStyle("-fx-text-fill:" + ViewHelper.ACCENT + "; -fx-underline:true; -fx-cursor:hand;");
                link.setOnMouseClicked(e -> {
                    String r = row.getRemarks();   // cached — no DB call needed
                    showInfo("Remarks for Request " + row.getDisplayId(),
                            r != null && !r.isBlank() ? r : "No remarks recorded.");
                });
                setGraphic(link); setText(null);
            }
        });

        table.getColumns().addAll(idCol, typeCol, dateCol, statusCol, deliveryCol, remarksCol);
        return table;
    }

    private void switchToProcessView(AdminRequestModel req) {
        boolean isResolved = req.getStatus().equalsIgnoreCase(RequestStatus.RESOLVED.name());
        boolean isRejected = req.getStatus().equalsIgnoreCase(RequestStatus.REJECTED.name());
        boolean isPostal   = req.isPostal();
        boolean isAcademic = req.isAcademic();
        boolean noDelivery = isAcademic
                || "FEE_SUPPORT".equalsIgnoreCase(req.getType())
                || "SCHOLARSHIP".equalsIgnoreCase(req.getType())
                || "COMPLAINT".equalsIgnoreCase(req.getType())
                || "LOST_FOUND".equalsIgnoreCase(req.getType());

        VBox page = new VBox();
        page.setStyle("-fx-background-color: " + ViewHelper.BG() + ";");

        HBox topBar = ViewHelper.topBar((Stage) rootStack.getScene().getWindow(), username,
                this::returnToDashboard, () -> switchToProcessView(req));
        Button backBtn = (Button) topBar.getChildren().get(topBar.getChildren().size() - 1);
        backBtn.setText("← Back to Dashboard");

        Label reqLabel = new Label("Request " + req.getDisplayId() + "  ·  " + req.getType());
        reqLabel.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.TEXT_BODY() + ";");
        topBar.getChildren().add(1, reqLabel);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 30, 28, 30));
        content.setMaxWidth(880);

        HBox titleRow = new HBox(14);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Processing Request");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.TEXT_DARK() + ";");

        String sUpper = req.getStatus().toUpperCase();
        String badgeColor = ViewHelper.WARNING;
        if(sUpper.equals(RequestStatus.RESOLVED.name())) badgeColor = ViewHelper.SUCCESS;
        else if(sUpper.equals(RequestStatus.REJECTED.name())) badgeColor = ViewHelper.DANGER;
        else if(sUpper.equals(RequestStatus.IN_REVIEW.name())) badgeColor = ViewHelper.ACCENT;
        else if(sUpper.equals(RequestStatus.PENDING_VERIFICATION.name())) badgeColor = "#7C3AED";
        else if(sUpper.equals(RequestStatus.FLAGGED_FOR_REVIEW.name()))  badgeColor = "#DC2626";

        Label statusBadge = badge(sUpper, badgeColor);
        titleRow.getChildren().addAll(title, statusBadge);

        VBox detailsCard = card("Request Details");
        VBox detailsBody = cardBody(detailsCard);

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{ "Request ID",      req.getDisplayId() });
        rows.add(new String[]{ "Student ID",      String.valueOf(req.getStudentId()) });
        rows.add(new String[]{ "Request Type",    req.getType() });
        rows.add(new String[]{ "Submitted On",    req.getDateOnly() });
        rows.add(new String[]{ "Current Status",  req.getStatus().toUpperCase() });
        rows.add(new String[]{ "Delivery Mode",   req.getDeliveryMode().isBlank() ? "—" : req.getDeliveryMode() });

        if (isPostal) {
            String addr = req.getAddress();
            rows.add(new String[]{ "Delivery Address", addr.isBlank() ? "Not provided" : addr });
        }
        if (!req.getPurpose().isBlank())
            rows.add(new String[]{ "Purpose",        req.getPurpose() });
        if (!req.getCopies().isBlank())
            rows.add(new String[]{ "No. of Copies",  req.getCopies() });

        if (!noDelivery) {
            String deliveryLabel = isPostal
                    ? (req.getCollected() == 2 ? DEL_DELIVERED : req.getCollected() == 1 ? DEL_IN_TRANSIT : DEL_PROCESSING)
                    : (req.getCollected() == 1 ? DEL_PICKED_UP : DEL_READY);
            rows.add(new String[]{ "Delivery Status", deliveryLabel });
        }

        for (java.util.Map.Entry<String, String> e : req.getExtras().entrySet()) {
            rows.add(new String[]{ e.getKey(), e.getValue() });
        }

        String existingRemarks = req.getRemarks();
        if (!existingRemarks.isBlank())
            rows.add(new String[]{ "Admin Remarks", existingRemarks });

        detailsBody.getChildren().add(buildDetailGrid(rows));

        VBox formCard = card("Update Request");
        VBox formBody = cardBody(formCard);

        Label dLabel = ViewHelper.fieldLabel("Delivery Status");
        ComboBox<String> deliveryBox = new ComboBox<>();

        if (isPostal) {
            deliveryBox.getItems().addAll(DEL_PROCESSING, DEL_IN_TRANSIT, DEL_DELIVERED);
            deliveryBox.setValue(req.getCollected() == 2 ? DEL_DELIVERED : req.getCollected() == 1 ? DEL_IN_TRANSIT : DEL_PROCESSING);
        } else {
            deliveryBox.getItems().addAll(DEL_READY, DEL_PICKED_UP);
            deliveryBox.setValue(req.getCollected() == 1 ? DEL_PICKED_UP : DEL_READY);
        }
        deliveryBox.setMaxWidth(Double.MAX_VALUE);
        deliveryBox.setPrefHeight(38);
        deliveryBox.setStyle("-fx-font-size:13px;");
        deliveryBox.setDisable(isRejected);
        if (noDelivery) {
            dLabel.setVisible(false); dLabel.setManaged(false);
            deliveryBox.setVisible(false); deliveryBox.setManaged(false);
        }

        Label rLabel = ViewHelper.fieldLabel("Admin Remarks");
        TextArea remarksInput = new TextArea(existingRemarks);
        remarksInput.setPromptText("Enter remarks here...");
        remarksInput.setPrefRowCount(4);
        remarksInput.setStyle("-fx-font-size:13px;");
        remarksInput.setDisable(isRejected);

        formBody.getChildren().addAll(dLabel, deliveryBox, ViewHelper.gap(4), rLabel, remarksInput, ViewHelper.gap(8));

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = ViewHelper.outlineBtn("Cancel & Back");
        cancelBtn.setOnAction(e -> returnToDashboard());

        if (!isRejected) {
            boolean isPendingFee = req.getStatus().equalsIgnoreCase(RequestStatus.PENDING_VERIFICATION.name());

            Button rejectBtn = ViewHelper.solidBtn("Reject Request", ViewHelper.DANGER);
            rejectBtn.setVisible(!isResolved);
            rejectBtn.setManaged(!isResolved);
            rejectBtn.setOnAction(e -> runUpdateAsync(req,
                    RequestStatus.REJECTED.name(), remarksInput.getText(), null, 0));

            Button reviewBtn = ViewHelper.solidBtn("Mark In Review", ViewHelper.ACCENT);
            reviewBtn.setVisible(!isResolved);
            reviewBtn.setManaged(!isResolved);
            reviewBtn.setOnAction(e -> runUpdateAsync(req,
                    RequestStatus.IN_REVIEW.name(), remarksInput.getText(), null, 0));

            Button resolveBtn = ViewHelper.solidBtn(isResolved ? "Save Changes" : "Resolve Request", ViewHelper.SUCCESS);
            resolveBtn.setOnAction(e -> {
                int flag = collectedFlag(deliveryBox.getValue(), isPostal);
                runUpdateAsync(req, RequestStatus.RESOLVED.name(),
                        remarksInput.getText(), LocalDate.now(), flag);
            });

            if (isPendingFee) {
                Button verifyFeeBtn = ViewHelper.solidBtn("Verify Fee Voucher", "#7C3AED");
                verifyFeeBtn.setOnAction(e -> runUpdateAsync(req,
                        RequestStatus.IN_REVIEW.name(),
                        (remarksInput.getText().isBlank()
                                ? "Fee voucher verified by Finance Department."
                                : remarksInput.getText() + " (Fee verified.)"),
                        null, 0));
                actions.getChildren().addAll(cancelBtn, rejectBtn, verifyFeeBtn);
            } else {
                actions.getChildren().addAll(cancelBtn, rejectBtn, reviewBtn, resolveBtn);
            }
        } else {
            actions.getChildren().add(cancelBtn);
        }

        formBody.getChildren().add(actions);
        content.getChildren().addAll(titleRow, detailsCard, formCard);

        HBox centered = new HBox(content);
        centered.setAlignment(Pos.TOP_CENTER);
        scroll.setContent(centered);
        page.getChildren().addAll(topBar, scroll);
        rootStack.getChildren().setAll(page);
    }

    private int collectedFlag(String value, boolean isPostal) {
        if (isPostal) {
            return switch (value) {
                case DEL_IN_TRANSIT -> 1;
                case DEL_DELIVERED  -> 2;
                default             -> 0;
            };
        } else {
            return DEL_PICKED_UP.equals(value) ? 1 : 0;
        }
    }


    private void switchToAuditLogView() {
        VBox page = new VBox();
        page.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");

        HBox topBar = ViewHelper.topBar((Stage) rootStack.getScene().getWindow(), username,
                this::returnToDashboard, this::switchToAuditLogView);
        Button backBtn = (Button) topBar.getChildren().get(topBar.getChildren().size() - 1);
        backBtn.setText("← Back to Dashboard");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 30, 28, 30));
        VBox.setVgrow(content, Priority.ALWAYS);

        Label heading    = new Label("System Audit Log");
        heading.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.TEXT_DARK() + ";");
        Label subheading = new Label("A record of all admin actions taken");
        subheading.setStyle("-fx-font-size:13px; -fx-text-fill:" + ViewHelper.MUTED() + ";");

        TableView<AuditLogModel> auditTable = new TableView<>();
        auditTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        auditTable.setStyle("-fx-border-width:0;");
        auditTable.setFixedCellSize(46);
        VBox.setVgrow(auditTable, Priority.ALWAYS);

        TableColumn<AuditLogModel, String> adminCol = new TableColumn<>("Admin");
        adminCol.setCellValueFactory(new PropertyValueFactory<>("adminUsername"));
        TableColumn<AuditLogModel, String> ridCol = new TableColumn<>("Req ID");
        ridCol.setCellValueFactory(new PropertyValueFactory<>("displayId"));
        ridCol.setMaxWidth(100); ridCol.setMinWidth(100);
        TableColumn<AuditLogModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<AuditLogModel, String> newStatCol = new TableColumn<>("New Status");
        newStatCol.setCellValueFactory(new PropertyValueFactory<>("newStatus"));
        newStatCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String s = item.toUpperCase();

                String color = ViewHelper.WARNING;
                if(s.equals(RequestStatus.RESOLVED.name())) color = ViewHelper.SUCCESS;
                else if(s.equals(RequestStatus.REJECTED.name())) color = ViewHelper.DANGER;
                else if(s.equals(RequestStatus.IN_REVIEW.name())) color = ViewHelper.ACCENT;

                Label b = badge(s, color);
                setGraphic(b); setText(null);
            }
        });

        TableColumn<AuditLogModel, String> timeCol = new TableColumn<>("Date");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("dateOnly"));

        auditTable.getColumns().addAll(adminCol, ridCol, typeCol, newStatCol, timeCol);
        auditTable.setItems(controller.fetchAuditLogs());

        VBox tableCard = new VBox(0, auditTable);
        tableCard.setStyle("-fx-background-color:" + ViewHelper.SURFACE() + "; -fx-border-color:" + ViewHelper.BORDER()
                + "; -fx-border-radius:10; -fx-background-radius:10;");
        tableCard.setEffect(shadow());
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        content.getChildren().addAll(new VBox(4, heading, subheading), tableCard);
        page.getChildren().addAll(topBar, content);
        rootStack.getChildren().setAll(page);
    }

    private void returnToDashboard() {
        Stage stage = (Stage) rootStack.getScene().getWindow();
        new AdminDashboard(username).show(stage);
    }


    private void runUpdateAsync(AdminRequestModel req, String newStatus,
                                String remarks, LocalDate date, int collected) {
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() {
                return controller.updateRequestStatus(
                        req.getRequestId(), req.getType(),
                        newStatus, remarks, date, username, collected);
            }
        };
        task.setOnSucceeded(e -> returnToDashboard());
        task.setOnFailed(e -> {
            if (task.getException() != null) task.getException().printStackTrace();
            returnToDashboard();
        });
        Thread t = new Thread(task, "admin-update-status");
        t.setDaemon(true);
        t.start();
    }

    private void reloadRequestsAsync() {
        if (mainTable == null) return;
        Task<ObservableList<AdminRequestModel>> task = new Task<>() {
            @Override protected ObservableList<AdminRequestModel> call() {
                return controller.fetchAllRequests();
            }
        };
        task.setOnSucceeded(e -> mainTable.setItems(task.getValue()));
        task.setOnFailed(e -> {
            if (task.getException() != null) task.getException().printStackTrace();
        });
        Thread t = new Thread(task, "admin-fetch-requests");
        t.setDaemon(true);
        t.start();
    }


    private GridPane buildDetailGrid(List<String[]> rows) {
        GridPane grid = new GridPane();
        grid.setHgap(0); grid.setVgap(0);
        grid.setStyle("-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-radius:6; "
                + "-fx-background-radius:6; -fx-background-color:" + ViewHelper.SURFACE() + ";");

        String keyBg = ui.common.Theme.isDark() ? "#0F172A" : "#F9FAFB";

        for (int i = 0; i < rows.size(); i++) {
            boolean last = (i == rows.size() - 1);
            String borderBottom = last ? "" : "-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-width:0 0 1 0;";

            Label key = new Label(rows.get(i)[0]);
            key.setPadding(new Insets(11, 16, 11, 16));
            key.setPrefWidth(200); key.setMinWidth(200);
            key.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.MUTED() + "; "
                    + "-fx-background-color:" + keyBg + "; " + borderBottom);

            Label val = new Label(rows.get(i)[1]);
            val.setWrapText(true);
            val.setPadding(new Insets(11, 16, 11, 16));
            val.setStyle("-fx-font-size:13px; -fx-text-fill:" + ViewHelper.TEXT_DARK() + "; "
                    + "-fx-background-color:" + ViewHelper.SURFACE() + "; "
                    + "-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-width:0 0 0 1; " + borderBottom);
            GridPane.setHgrow(val, Priority.ALWAYS);

            grid.add(key, 0, i);
            grid.add(val, 1, i);
        }
        return grid;
    }

    private VBox card(String heading) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:" + ViewHelper.SURFACE() + "; -fx-border-color:" + ViewHelper.BORDER()
                + "; -fx-border-radius:10; -fx-background-radius:10;");
        card.setEffect(shadow());

        String headerBg = ui.common.Theme.isDark() ? "#0F172A" : "#FAFAFA";

        Label label = new Label(heading);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(13, 18, 13, 18));
        label.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.TEXT_DARK() + "; "
                + "-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-width:0 0 1 0; "
                + "-fx-background-color:" + headerBg + "; -fx-background-radius:10 10 0 0;");

        VBox body = new VBox(12);
        body.setPadding(new Insets(18));
        card.getChildren().addAll(label, body);
        return card;
    }

    private VBox cardBody(VBox card) { return (VBox) card.getChildren().get(1); }

    private Label badge(String text, String color) {
        Label b = new Label(text);
        b.setPadding(new Insets(3, 10, 3, 10));
        b.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:20; "
                + "-fx-text-fill:white; -fx-background-color:" + color + ";");
        return b;
    }

    private javafx.scene.effect.DropShadow shadow() {
        return new javafx.scene.effect.DropShadow(6, 0, 2, Color.rgb(0, 0, 0, 0.06));
    }

    private void showInfo(String header, String body) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, body);
        a.setHeaderText(header);
        a.setTitle("OneDesk");
        a.show();
    }
}