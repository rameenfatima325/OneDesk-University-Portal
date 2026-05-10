package ui.support;

import business.support.*;
import ui.document.StudentDashboard;
import ui.document.ViewHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Date;



public class LostFoundScreen {

    private final String username;
    private final int    studentId;

    public LostFoundScreen(String username, int studentId) {
        this.username  = username;
        this.studentId = studentId;
    }

    public void show(Stage stage) {
        HBox topBar = ViewHelper.topBar(stage, username,
                () -> new StudentDashboard(username).show(stage),
                () -> new LostFoundScreen(username, studentId).show(stage));

        TabPane tabs = new TabPane(buildLostTab(stage), buildFoundTab(stage));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        VBox root = new VBox(topBar, tabs);
        root.setStyle("-fx-background-color:" + ViewHelper.BG() + ";");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        stage.setTitle("OneDesk — Lost and Found");
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        ui.common.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private Tab buildLostTab(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        TextField tfItem = ViewHelper.styledField("e.g. Scientific Calculator");
        ViewHelper.addRow(grid, 0, "Item Name", tfItem);

        ComboBox<String> cbCat = categoryCombo();
        ViewHelper.addRow(grid, 1, "Category", cbCat);

        TextField tfLocation = ViewHelper.styledField("e.g. CS Block Room 301");
        ViewHelper.addRow(grid, 2, "Location Last Seen", tfLocation);

        TextField tfContact = ViewHelper.styledField("e.g. 0300-1234567");
        ViewHelper.addRow(grid, 3, "Your Contact", tfContact);

        TextArea taDesc = new TextArea();
        taDesc.setPromptText("Description (optional)");
        taDesc.setPrefRowCount(3); taDesc.setWrapText(true);
        taDesc.setStyle(ViewHelper.FIELD_STYLE());
        ViewHelper.addRow(grid, 4, "Description", taDesc);

        Button submitBtn = ViewHelper.solidBtn("Report Lost Item", ViewHelper.DANGER);
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (tfItem.getText().trim().isEmpty() || cbCat.getValue() == null
                    || tfLocation.getText().trim().isEmpty() || tfContact.getText().trim().isEmpty()) {
                alert(Alert.AlertType.WARNING, "Required Fields", "Item name, category, location and contact are required.");
                return;
            }
            try {
                new SupportController().reportLostItem(studentId,
                        tfItem.getText().trim(),
                        ItemCategory.valueOf(cbCat.getValue()),
                        tfLocation.getText().trim(),
                        new Date(),
                        taDesc.getText().trim(),
                        tfContact.getText().trim());
                alert(Alert.AlertType.INFORMATION, "Reported", "Lost item report submitted successfully.");
                new StudentDashboard(username).show(stage);
            } catch (Exception ex) { alert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        VBox card = ViewHelper.createFormCard(grid, submitBtn, "Report a Lost Item");
        ScrollPane sp = new ScrollPane(card);
        sp.setFitToWidth(true); sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");
        return new Tab("Report Lost Item", sp);
    }

    private Tab buildFoundTab(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(16);
        grid.setAlignment(Pos.CENTER);

        TextField tfItem = ViewHelper.styledField("e.g. Water Bottle");
        ViewHelper.addRow(grid, 0, "Item Name", tfItem);

        ComboBox<String> cbCat = categoryCombo();
        ViewHelper.addRow(grid, 1, "Category", cbCat);

        TextField tfLocation = ViewHelper.styledField("e.g. Library Reading Room");
        ViewHelper.addRow(grid, 2, "Location Found", tfLocation);

        TextField tfStorage = ViewHelper.styledField("e.g. Security Guard Room, Main Gate");
        ViewHelper.addRow(grid, 3, "Storage Location", tfStorage);

        TextField tfContact = ViewHelper.styledField("e.g. 0311-9876543");
        ViewHelper.addRow(grid, 4, "Your Contact", tfContact);

        Button submitBtn = ViewHelper.solidBtn("Report Found Item", ViewHelper.SUCCESS);
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (tfItem.getText().trim().isEmpty() || cbCat.getValue() == null
                    || tfLocation.getText().trim().isEmpty() || tfContact.getText().trim().isEmpty()) {
                alert(Alert.AlertType.WARNING, "Required Fields", "Item name, category, location and contact are required.");
                return;
            }
            try {
                new SupportController().reportFoundItem(studentId,
                        tfItem.getText().trim(),
                        ItemCategory.valueOf(cbCat.getValue()),
                        tfLocation.getText().trim(),
                        new Date(),
                        tfStorage.getText().trim(),
                        tfContact.getText().trim());
                alert(Alert.AlertType.INFORMATION, "Reported", "Found item report submitted successfully.");
                new StudentDashboard(username).show(stage);
            } catch (Exception ex) { alert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        VBox card = ViewHelper.createFormCard(grid, submitBtn, "Report a Found Item");
        ScrollPane sp = new ScrollPane(card);
        sp.setFitToWidth(true); sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-width:0;");
        return new Tab("Report Found Item", sp);
    }

    private ComboBox<String> categoryCombo() {
        ComboBox<String> cb = new ComboBox<>();
        for (ItemCategory c : ItemCategory.values()) cb.getItems().add(c.name());
        cb.setPromptText("Select category...");
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}