package ui.document;

import business.academic.AuthController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import ui.common.Theme;


public class LoginView extends Application {

    private final AuthController authController = new AuthController();

    @Override
    public void start(Stage stage) {
        Region hero = buildHeroPanel();
        Region form = buildFormPanel(stage);

        HBox root = new HBox(hero, form);
        HBox.setHgrow(hero, Priority.ALWAYS);
        HBox.setHgrow(form, Priority.ALWAYS);
        hero.prefWidthProperty().bind(root.widthProperty().multiply(0.55));
        form.prefWidthProperty().bind(root.widthProperty().multiply(0.45));
        root.getStyleClass().add("login-root");

        Scene scene = new Scene(root, 1100, 720);
        Theme.apply(scene);
        Theme.applyWindowIcon(stage);

        stage.setTitle("OneDesk — Sign In");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.show();
    }

    private Region buildHeroPanel() {
        StackPane hero = new StackPane();
        hero.getStyleClass().add("login-hero");

        ImageView logo = new ImageView();
        Image img = Theme.logo();
        if (img != null) {
            logo.setImage(img);
            logo.setPreserveRatio(true);
            logo.setFitWidth(420);
            logo.setSmooth(true);
            hero.widthProperty().addListener((obs, oldW, newW) -> {
                double target = Math.max(280, Math.min(newW.doubleValue() * 0.72, 520));
                logo.setFitWidth(target);
            });
        } else {
            Label fallback = new Label("OneDesk");
            fallback.getStyleClass().add("login-hero-title");
            hero.getChildren().add(fallback);
            return hero;
        }

        StackPane.setAlignment(logo, Pos.CENTER);
        hero.getChildren().add(logo);

        Label foot = new Label("© 2026 FAST-NUCES  •  OneDesk CSB Project");
        foot.getStyleClass().add("login-hero-footer");
        StackPane.setAlignment(foot, Pos.BOTTOM_LEFT);
        StackPane.setMargin(foot, new Insets(0, 0, 28, 36));
        hero.getChildren().add(foot);

        return hero;
    }

    private Region buildFormPanel(Stage stage) {
        VBox panel = new VBox();
        panel.getStyleClass().add("login-form-panel");
        panel.setPadding(new Insets(24, 40, 32, 40));
        panel.setAlignment(Pos.CENTER);

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        Button modeBtn = new Button(Theme.isDark() ? "☀  Light" : "🌙  Dark");
        modeBtn.getStyleClass().add("mode-toggle");
        modeBtn.setOnAction(e -> Theme.toggleDark());
        Theme.onModeChange(d -> modeBtn.setText(d ? "☀  Light" : "🌙  Dark"));
        topBar.getChildren().add(modeBtn);

        VBox card = new VBox(18);
        card.getStyleClass().add("login-card");
        card.setPadding(new Insets(36, 36, 32, 36));
        card.setMaxWidth(420);

        Label title    = new Label("Welcome back");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Sign in to your OneDesk account");
        subtitle.getStyleClass().add("login-subtitle");

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("login-field-label");
        TextField userFld = new TextField();
        userFld.setPromptText("e.g.  zahra   or   admin");
        userFld.getStyleClass().add("login-field");

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("login-field-label");
        PasswordField passFld = new PasswordField();
        passFld.setPromptText("Enter your password");
        passFld.getStyleClass().add("login-field");

        Label errLbl = new Label();
        errLbl.getStyleClass().add("login-error");
        errLbl.setWrapText(true);
        errLbl.setMinHeight(16);

        Button signIn = new Button("Sign In");
        signIn.getStyleClass().add("login-primary-btn");
        signIn.setMaxWidth(Double.MAX_VALUE);

        Label hint = new Label("Demo:  zahra / zahra123  •  aizah / aizah123  •  rameen / rameen123  •  admin / admin123");
        hint.getStyleClass().add("login-hint");
        hint.setWrapText(true);
        hint.setTextAlignment(TextAlignment.CENTER);
        hint.setMaxWidth(360);

        Runnable doLogin = () -> {
            String u = userFld.getText() == null ? "" : userFld.getText().trim();
            String p = passFld.getText();
            if (u.isEmpty() || p == null || p.isEmpty()) {
                errLbl.setText("⚠  Please enter both username and password.");
                return;
            }
            String role = authController.login(u, p);
            if (role == null) {
                errLbl.setText("⚠  Invalid username or password.");
            } else if ("ADMIN".equals(role)) {
                errLbl.setText("");
                new AdminDashboard(u).show(stage);
            } else if ("STUDENT".equals(role)) {
                errLbl.setText("");
                new StudentDashboard(u).show(stage);
            }
        };
        signIn.setOnAction(e -> doLogin.run());
        passFld.setOnAction(e -> doLogin.run());
        userFld.setOnAction(e -> passFld.requestFocus());

        VBox userGroup = new VBox(6, userLbl, userFld);
        VBox passGroup = new VBox(6, passLbl, passFld);
        card.getChildren().addAll(title, subtitle, userGroup, passGroup, errLbl, signIn);

        Region topSpacer = new Region();
        Region midSpacer = new Region();
        Region botSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.SOMETIMES);
        VBox.setVgrow(botSpacer, Priority.ALWAYS);

        panel.getChildren().addAll(topBar, topSpacer, card, midSpacer, hint, botSpacer);
        VBox.setMargin(card, new Insets(24, 0, 16, 0));
        VBox.setMargin(hint, new Insets(0, 0, 8, 0));
        return panel;
    }

    public static void main(String[] args) { launch(args); }
}