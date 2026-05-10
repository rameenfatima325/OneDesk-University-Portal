package ui.common;

import business.shared.RequestStatus;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;



public final class Theme {

    private Theme() {}

    private static boolean darkMode = false;
    private static final List<Consumer<Boolean>> listeners = new ArrayList<>();

    public static boolean isDark() { return darkMode; }

    public static void setDark(boolean dark) {
        if (darkMode == dark) return;
        darkMode = dark;
        for (Consumer<Boolean> l : new ArrayList<>(listeners)) {
            try { l.accept(darkMode); } catch (Exception ignored) {}
        }
    }

    public static void toggleDark() { setDark(!darkMode); }

    public static void onModeChange(Consumer<Boolean> listener) {
        listeners.add(listener);
    }

    public static void apply(Scene scene) {
        URL css = Theme.class.getResource("/ui/common/styles.css");
        if (css == null) css = Theme.class.getResource("styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("[Theme] styles.css not found on classpath. "
                    + "Make sure src/ui/common is marked as a Resources root in IntelliJ.");
        }
        applyModeClass(scene);
        onModeChange(d -> applyModeClass(scene));
    }

    private static void applyModeClass(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;
        Region root = (scene.getRoot() instanceof Region) ? (Region) scene.getRoot() : null;
        if (root == null) return;
        root.getStyleClass().remove("dark-mode");
        root.getStyleClass().remove("light-mode");
        root.getStyleClass().add(darkMode ? "dark-mode" : "light-mode");
    }


    private static Image cachedLogo;

    public static Image logo() {
        if (cachedLogo != null) return cachedLogo;
        try {
            URL u = Theme.class.getResource("/ui/common/onedesk_logo.png");
            if (u == null) u = Theme.class.getResource("onedesk_logo.png");
            if (u != null) cachedLogo = new Image(u.toExternalForm());
        } catch (Exception e) {
            System.err.println("[Theme] failed to load logo: " + e.getMessage());
        }
        return cachedLogo;
    }

    public static void applyWindowIcon(Stage stage) {
        Image img = logo();
        if (img != null && stage.getIcons().stream().noneMatch(i -> i == img)) {
            stage.getIcons().add(img);
        }
    }


    public static Label statusPill(RequestStatus s) {
        Label l = new Label(s == null ? "—" : s.name().replace('_', ' '));
        l.getStyleClass().add("status-pill");
        if (s != null) {
            switch (s) {
                case PENDING:   l.getStyleClass().add("status-pending");  break;
                case IN_REVIEW: l.getStyleClass().add("status-review");   break;
                case RESOLVED:  l.getStyleClass().add("status-resolved"); break;
                case REJECTED:  l.getStyleClass().add("status-rejected"); break;
            }
        }
        return l;
    }
}