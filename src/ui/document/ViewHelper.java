package ui.document;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ui.common.Theme;


public class ViewHelper {

    public static final String ACCENT    = "#3B82F6";
    public static final String SUCCESS   = "#10B981";
    public static final String DANGER    = "#EF4444";
    public static final String WARNING   = "#F59E0B";

    public static String BG()        { return Theme.isDark() ? "#0B1220" : "#F9FAFB"; }
    public static String SURFACE()   { return Theme.isDark() ? "#111827" : "#FFFFFF"; }
    public static String BORDER()    { return Theme.isDark() ? "#1F2937" : "#E5E7EB"; }
    public static String TEXT_DARK() { return Theme.isDark() ? "#F1F5F9" : "#1F2937"; }
    public static String TEXT_BODY() { return Theme.isDark() ? "#CBD5E1" : "#4B5563"; }
    public static String MUTED()     { return Theme.isDark() ? "#94A3B8" : "#6B7280"; }
    public static String FIELD_BG()  { return Theme.isDark() ? "#0F172A" : "#FFFFFF"; }

    public static String FIELD_STYLE() {
        return "-fx-background-color:" + FIELD_BG() + "; -fx-border-color:" + BORDER() + "; "
                + "-fx-border-radius:6; -fx-background-radius:6; -fx-padding:10 12; -fx-font-size:14px; "
                + "-fx-text-fill:" + TEXT_DARK() + "; -fx-prompt-text-fill:" + MUTED() + ";";
    }

    public static String LABEL_STYLE() {
        return "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + TEXT_DARK() + ";";
    }



    public static void repaintInlineStyles(javafx.scene.Node node) {
        if (node == null) return;
        if (node instanceof javafx.scene.control.TextInputControl
                || node instanceof javafx.scene.control.ComboBoxBase
                || node instanceof javafx.scene.control.DatePicker) {
            node.setStyle(FIELD_STYLE());
        }
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                repaintInlineStyles(child);
            }
        }
    }

    public static HBox topBar(Stage stage, String username, Runnable onBack) {
        return topBar(stage, username, onBack, null);
    }


    public static HBox topBar(Stage stage, String username, Runnable onBack, Runnable onRebuild) {
        HBox bar = new HBox();
        bar.setPadding(new Insets(0, 30, 0, 30));
        bar.setPrefHeight(70);
        bar.setAlignment(Pos.CENTER);
        bar.setStyle("-fx-background-color:" + SURFACE() + "; -fx-border-color:" + BORDER() + "; -fx-border-width:0 0 1 0;");

        Label appName = new Label("OneDesk");
        appName.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:" + ACCENT + ";");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userBadge = new Label("User: " + username);
        userBadge.setStyle("-fx-font-size:14px; -fx-text-fill:" + TEXT_BODY() + ";");

        Button modeBtn = new Button(ui.common.Theme.isDark() ? "☀" : "🌙");
        modeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill:" + TEXT_BODY()
                + "; -fx-font-size:16px; -fx-cursor:hand; -fx-padding:6 10;");
        modeBtn.setOnAction(e -> {
            ui.common.Theme.toggleDark();
            if (onRebuild != null) {
                // Full rebuild — every theme-dependent token is freshly evaluated.
                onRebuild.run();
            } else {
                // Fallback: best-effort in-place repaint.
                if (stage.getScene() != null) {
                    ui.common.Theme.apply(stage.getScene());
                    repaintInlineStyles(stage.getScene().getRoot());
                }
                modeBtn.setText(ui.common.Theme.isDark() ? "☀" : "🌙");
            }
        });

        Button backBtn = outlineBtn("← Back to Dashboard");
        backBtn.setOnAction(e -> onBack.run());

        bar.getChildren().addAll(appName, spacer, userBadge, gap(16), modeBtn, gap(8), backBtn);
        return bar;
    }

    public static VBox createFormCard(GridPane grid, Button submitBtn, String title) {
        Label formTitle = new Label(title);
        formTitle.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + TEXT_DARK() + ";");

        VBox card = new VBox(24, formTitle, grid, submitBtn);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color:" + SURFACE() + "; -fx-border-color:" + BORDER() + "; -fx-border-radius:12; -fx-background-radius:12;");
        card.setEffect(new DropShadow(10, 0, 4, Color.rgb(0, 0, 0, 0.05)));
        card.setMaxWidth(550);

        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(30));
        wrapper.setStyle("-fx-background-color:" + BG() + ";");
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    public static Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle(LABEL_STYLE());
        l.setPrefWidth(160); l.setMinWidth(160);
        return l;
    }

    public static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(FIELD_STYLE());
        return tf;
    }

    public static ComboBox<String> styledCombo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    public static Button solidBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; " +
                "-fx-font-weight:bold; -fx-font-size:14px; -fx-padding:10 24; " +
                "-fx-background-radius:6; -fx-cursor:hand;");
        return b;
    }

    public static Button outlineBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:transparent; -fx-text-fill:" + TEXT_BODY() + "; " +
                "-fx-font-size:14px; -fx-padding:8 18; -fx-border-radius:6; " +
                "-fx-border-color:" + BORDER() + "; -fx-border-width:1; -fx-cursor:hand;");
        return b;
    }

    public static void addRow(GridPane grid, int row, String labelText, javafx.scene.Node control) {
        Label l = fieldLabel(labelText);
        if (control instanceof Control c) {
            c.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(c, Priority.ALWAYS);
        }
        grid.add(l, 0, row); grid.add(control, 1, row);
    }

    public static Region gap(double size) {
        Region r = new Region(); r.setPrefWidth(size); r.setPrefHeight(size); return r;
    }
}