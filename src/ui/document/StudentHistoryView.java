package ui.document;

import business.admin.AdminController;
import business.admin.AdminRequestModel;
import business.shared.RequestStatus; // ◄ Imports the shared Enum
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import db.document.UserDAO;
import ui.common.Theme;


public class StudentHistoryView {
    private final String username;
    private final AdminController controller = new AdminController();
    public static final String DEL_PROCESSING   = "PROCESSING";
    public static final String DEL_IN_TRANSIT   = "IN TRANSIT";
    public static final String DEL_DELIVERED    = "DELIVERED";
    public static final String DEL_READY        = "READY FOR PICKUP";
    public static final String DEL_PICKED_UP    = "PICKED UP";

    public StudentHistoryView(String username) { this.username = username; }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new StudentHistoryView(username).show(stage));

        Label pageTitle = new Label("My Request History");
        pageTitle.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + ViewHelper.TEXT_DARK() + ";");
        Label subtitle = new Label("All document requests submitted under your account");
        subtitle.setStyle("-fx-font-size:13px; -fx-text-fill:" + ViewHelper.MUTED() + ";");

        Button refreshBtn = ViewHelper.outlineBtn("⟳  Refresh");
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        HBox headingRow = new HBox(12, new VBox(2, pageTitle, subtitle), hSpacer, refreshBtn);
        headingRow.setAlignment(Pos.CENTER_LEFT);

        TableView<AdminRequestModel> table = buildTable();

        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-size:12px; -fx-text-fill:" + ViewHelper.MUTED() + ";");
        HBox tableFooter = new HBox(countLabel);
        tableFooter.setPadding(new Insets(10, 18, 10, 18));
        tableFooter.setAlignment(Pos.CENTER_LEFT);
        tableFooter.setStyle("-fx-border-color:" + ViewHelper.BORDER() + "; -fx-border-width:1 0 0 0;");

        VBox tableCard = new VBox(0, table, tableFooter);
        tableCard.setStyle(
                "-fx-background-color:" + ViewHelper.SURFACE() + "; " + "-fx-border-color:" + ViewHelper.BORDER() + "; " + "-fx-border-radius:10; -fx-background-radius:10;");
        tableCard.setEffect(new javafx.scene.effect.DropShadow(6, 0, 2, Color.rgb(0, 0, 0, 0.06)));
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        Runnable loadData = () -> {
            ObservableList<AdminRequestModel> items = loadHistory();
            table.setItems(items);
            countLabel.setText(items.size() + " record(s)");
        };

        loadData.run();
        refreshBtn.setOnAction(e -> loadData.run());

        VBox content = new VBox(20, headingRow, tableCard);
        content.setPadding(new Insets(28, 30, 24, 30));
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox root = new VBox(topBar, content);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(content, Priority.ALWAYS);

        stage.setTitle("OneDesk — My Request History");
        Scene scene = new Scene(root, 900, 700);
        Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<AdminRequestModel> loadHistory() {
        int studentId = new UserDAO().getStudentIdByUsername(username);
        if (studentId <= 0) return FXCollections.observableArrayList();

        ObservableList<AdminRequestModel> all = controller.fetchAllRequests();
        ObservableList<AdminRequestModel> mine = FXCollections.observableArrayList();
        for (AdminRequestModel m : all) {
            if (m.getStudentId() == studentId) {
                mine.add(m);
            }
        }
        return mine;
    }


    private TableView<AdminRequestModel> buildTable() {
        TableView<AdminRequestModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color:transparent; -fx-border-width:0;");
        table.setFixedCellSize(46);
        table.setPlaceholder(new Label("No requests found for your account."));
        VBox.setVgrow(table, Priority.ALWAYS);

        // ID
        TableColumn<AdminRequestModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("displayId"));
        idCol.setMaxWidth(70); idCol.setMinWidth(70);

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

                String color = ViewHelper.WARNING;
                if (s.equals(RequestStatus.RESOLVED.name())) color = ViewHelper.SUCCESS;
                else if (s.equals(RequestStatus.REJECTED.name())) color = ViewHelper.DANGER;
                else if (s.equals(RequestStatus.IN_REVIEW.name())) color = ViewHelper.ACCENT;
                else if (s.equals(RequestStatus.PENDING_VERIFICATION.name())) color = "#7C3AED";
                else if (s.equals(RequestStatus.FLAGGED_FOR_REVIEW.name()))  color = "#DC2626";

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
                    setGraphic(badge(RequestStatus.REJECTED.name(), ViewHelper.DANGER)); setText(null); return;
                }

                boolean postal = row.isPostal();
                int c = row.getCollected();
                String label, color;

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

                if (postal) {
                    label = c == 2 ? DEL_DELIVERED : c == 1 ? DEL_IN_TRANSIT : DEL_PROCESSING;
                    color = c == 2 ? ViewHelper.SUCCESS : c == 1 ? "#8B5CF6" : ViewHelper.ACCENT;
                } else {
                    label = c == 1 ? DEL_PICKED_UP : DEL_READY;
                    color = c == 1 ? ViewHelper.SUCCESS : ViewHelper.ACCENT;
                }
                setGraphic(badge(label, color)); setText(null);
            }
        });

        TableColumn<AdminRequestModel, String> remarksCol = new TableColumn<>("Admin Remarks");
        remarksCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                AdminRequestModel row = getTableRow().getItem();
                if (empty || row == null) { setText(null); return; }

                // Cached value — loaded once by fetchAllRequests(). No per-cell DB call.
                String remarks = row.getRemarks();
                setText((remarks == null || remarks.isBlank()) ? "—" : remarks);
                setStyle("-fx-text-fill:" + ViewHelper.TEXT_BODY() + "; -fx-font-size:12px;");
            }
        });

        table.getColumns().addAll(idCol, typeCol, dateCol, statusCol, deliveryCol, remarksCol);
        return table;
    }

    private Label badge(String text, String color) {
        Label b = new Label(text);
        b.setPadding(new Insets(3, 10, 3, 10));
        b.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:20; " +
                "-fx-text-fill:white; -fx-background-color:" + color + ";");
        return b;
    }
}